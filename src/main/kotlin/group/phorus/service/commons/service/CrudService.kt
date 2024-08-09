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
import org.springframework.context.ApplicationContext
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaField

abstract class CrudService<ENTITY: BaseEntity, DTO: Any>(
    private val entityClass: KClass<ENTITY>,
    private val repository: JpaRepository<ENTITY, UUID>,
    applicationContext: ApplicationContext,
) {
    private val repositories = applicationContext.getBeansOfType(JpaRepository::class.java).map { (_, value) ->
        // Get the interface that is extending this JpaRepository to avoid type erasure
        val inter = (value.javaClass.genericInterfaces.first() as Class<*>)

        // Get the entity class from the first value parameter of the delete function
        val entityClass = inter.kotlin.members.first { it.name == "delete" }
            .valueParameters.first().type.classifier as KClass<*>

        entityClass to value
    }.toMap()

    open suspend fun findById(id: UUID): ENTITY =
        withContext(Dispatchers.IO) {
            repository.findById(id)
        }.orElseThrow { NotFound("${entityClass.simpleName} with id $id not found.") }

    @Suppress("UNCHECKED_CAST")
    open suspend fun create(dto: DTO): UUID {
        val entity = mapTo(
            originalEntity = OriginalEntity(dto, dto::class.starProjectedType),
            targetType = entityClass.createType(),
            functionMappings = getFunctionMappings(dto),
        ) as ENTITY

        return withContext(Dispatchers.IO) {
            repository.save(entity)
        }.id
    }

    @Suppress("UNCHECKED_CAST")
    open suspend fun update(id: UUID, dto: DTO) {
        val entity = findById(id)
        val updatedEntity = mapTo(
            originalEntity = OriginalEntity(dto, dto::class.starProjectedType),
            targetType = entityClass.createType(),
            baseEntity = entity to UpdateOption.IGNORE_NULLS,
            useSettersOnly = true,
            functionMappings = getFunctionMappings(dto),
        ) as ENTITY

        withContext(Dispatchers.IO) {
            repository.save(updatedEntity)
        }
    }

    open suspend fun delete(id: UUID) {
        findById(id).also {
            repository.delete(it)
        }
    }


    private fun getFunctionMappings(dto: DTO): FunctionMappings {
        val mappings = mutableMapOf<String, Pair<String, Pair<KClass<*>, String>>>()

        // Find fields in the DTO containing a MapTo annotation
        dto::class.memberProperties.forEach { property ->
            val annotations = property.javaField?.annotations?.filterIsInstance<MapTo>()
            annotations?.forEach { annotation ->
                // Extract the KClass of the target fields and save the DTO sourceField, entity targetField, and target KClass
                val targetField = annotation.field
                val function = annotation.function
                val entity = entityClass.memberProperties.first { it.name == targetField }.returnType.classifier as KClass<*>

                mappings[property.name] = targetField to (entity to function)
            }
        }

        return mappings.mapNotNull { (sourceField, target) ->
            val (targetField, targetEntity) = target
            val (targetKClass, function) = targetEntity

            @Suppress("UNCHECKED_CAST")
            val repository = repositories[targetKClass]?.let {
                it as JpaRepository<*, Any>
            } ?: return@mapNotNull null
            val repositoryFunction = repository::class.memberFunctions.firstOrNull {
                it.name == function
            } ?: return@mapNotNull null

            val fetchRelation : (Any) -> Any = { value ->
                val response = repositoryFunction.call(repository, value) as Optional<*>
                response.orElseThrow {
                    NotFound("${targetField.replaceFirstChar { it.uppercase() }} with $sourceField $value not found.")
                }
            }

            sourceField to (fetchRelation to (targetField to MappingFallback.NULL_OR_THROW))
        }.toMap()
    }
}

@Target(AnnotationTarget.FIELD)
annotation class MapTo(
    val field: String,
    val function: String = "findById",
)