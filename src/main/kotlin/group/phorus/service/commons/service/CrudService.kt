package group.phorus.service.commons.service

import group.phorus.exception.handling.NotFound
import group.phorus.mapper.FunctionMappings
import group.phorus.mapper.OriginalEntity
import group.phorus.mapper.mapping.MappingFallback
import group.phorus.mapper.mapping.UpdateOption
import group.phorus.mapper.mapping.mapTo
import group.phorus.service.commons.model.BaseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

abstract class CrudService<ENTITY: BaseEntity, DTO: Any>(
    private val entityClass: KClass<ENTITY>,
    private val repository: JpaRepository<ENTITY, UUID>,
    private val providedRepositories: Map<BeanName, JpaRepository<*, UUID>> = emptyMap(),
) {
    suspend fun findById(id: UUID): ENTITY =
        withContext(Dispatchers.IO) {
            repository.findById(id)
        }.orElseThrow { NotFound("${entityClass.simpleName} with $id not found.") }

    @Suppress("UNCHECKED_CAST")
    suspend fun create(dto: DTO): UUID {
        val entity = mapTo(
            originalEntity = OriginalEntity(dto, dto::class.starProjectedType),
            targetType = entityClass.createType(),
            functionMappings = getFunctionMappings(),
        ) as ENTITY

        return withContext(Dispatchers.IO) {
            repository.save(entity)
        }.id
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun update(id: UUID, dto: DTO) {
        val entity = findById(id)
        val updatedEntity = mapTo(
            originalEntity = OriginalEntity(dto, dto::class.starProjectedType),
            targetType = entityClass.createType(),
            baseEntity = entity to UpdateOption.IGNORE_NULLS,
            useSettersOnly = true,
            functionMappings = getFunctionMappings(),
        ) as ENTITY

        withContext(Dispatchers.IO) {
            repository.save(updatedEntity)
        }
    }

    suspend fun delete(id: UUID) {
        findById(id).also {
            repository.delete(it)
        }
    }


    private fun CrudService<*, *>.getFunctionMappings(): FunctionMappings {
        val mappings = this::class.annotations.filterIsInstance<Mappings>().firstOrNull()?.value

        return mappings?.associate { mapping ->
            val fetchRelation : (UUID) -> Any = { id ->
                val beanName = mapping.repository.simpleName!!.replaceFirstChar { it.lowercase() }
                this.providedRepositories[beanName]!!.findById(id)
                    .orElseThrow {
                        NotFound("${mapping.targetField.replaceFirstChar { it.uppercase() }} with $id not found.")
                    }
            }

            mapping.sourceField to (fetchRelation to (mapping.targetField to MappingFallback.NULL_OR_THROW))
        } ?: emptyMap()
    }
}

typealias BeanName = String

annotation class Mapping(
    val sourceField: String,
    val targetField: String,
    val repository: KClass<out JpaRepository<*, *>>,
)

@Target(AnnotationTarget.CLASS)
annotation class Mappings(
    vararg val value: Mapping,
)
