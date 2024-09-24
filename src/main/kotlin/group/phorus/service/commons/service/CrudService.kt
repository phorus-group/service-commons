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
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.config.*
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.core.ResolvableType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaField

private fun JpaRepository<*, *>.getRepositoryEntity(): KClass<*> {
    // Get the interface that is extending this JpaRepository to avoid type erasure
    val inter = this.javaClass.genericInterfaces.first() as Class<*>

    // Get the entity class from the first value parameter of the delete function
    return inter.kotlin.members.first { it.name == "delete" }
        .valueParameters.first().type.classifier as KClass<*>
}

@Suppress("UNCHECKED_CAST")
private fun ListableBeanFactory.getRepositoriesMap() = this.getBeansOfType(JpaRepository::class.java).map { (_, value) ->
    value.getRepositoryEntity() to value
}.toMap() as Map<KClass<*>, JpaRepository<*, UUID>>

@AutoConfiguration
class CrudServiceDependencyResolver : BeanFactoryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        // We need to iterate through every bean and find the different CrudService beans that we need to register
        val beanNames = beanFactory.beanDefinitionNames

        // Get the applicationContextProvider bean to then use it to create the new CrudService beans
        val contextProviderBean = beanFactory.getBeanNamesForType(ApplicationContextProvider::class.java).first()
            .let { beanFactory.getBeanDefinition(it) }

        val existingCrudServiceImpls = beanFactory.getBeanNamesForType(CrudService::class.java)
            .mapNotNull { beanName -> beanFactory.getBeanDefinition(beanName).beanClassName?.let { Class.forName(it).kotlin } }
            .map { clazz ->
                val entity = clazz.members.first { it.name == "findById" }
                    .returnType.classifier as KClass<*>
                val dto = clazz.members.first { it.name == "create" }
                    .valueParameters.first().type.classifier as KClass<*>

                "crudService-${entity.simpleName}-${dto.simpleName}"
            }

        for (beanName in beanNames) {
            val bean = beanFactory.getBeanDefinition(beanName)

            // Get bean class
            val clazz = bean.beanClassName?.let { Class.forName(it) } ?: continue
            val constructor = clazz.kotlin.constructors.firstOrNull() ?: continue

            // Get CrudService parameters used by the bean, if any
            val crudServiceParams = constructor.parameters.map { it.type }.filter { it.classifier == CrudService::class }

            // Create a bean for every parameter found, if needed
            crudServiceParams.forEach { crudServiceType ->
                val entity = crudServiceType.arguments.first().type!!.classifier as KClass<*>
                val dto = crudServiceType.arguments[1].type!!.classifier as KClass<*>
                val crudBeanName = "crudService-${entity.simpleName}-${dto.simpleName}"

                // Skip if the bean already exists
                if (beanFactory.containsBean(crudBeanName) || existingCrudServiceImpls.contains(crudBeanName))
                    return@forEach

                // Get the matching repository definition
                val repositoryDefinition = beanFactory.getBeanNamesForType(
                    ResolvableType.forClassWithGenerics(
                        JpaRepository::class.java,
                        entity.java,
                        UUID::class.java,
                    )
                ).first().let { beanFactory.getBeanDefinition(it) }

                // Create the new CrudService bean definition for this entity and dto
                val bd = RootBeanDefinition(CrudService::class.java, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true).apply {
                    scope = ConfigurableBeanFactory.SCOPE_PROTOTYPE
                    setTargetType(ResolvableType.forClassWithGenerics(
                        CrudService::class.java,
                        entity.java,
                        dto.java,
                    ))
                    // Set the bean definitions that need to be resolved based on the CrudService constructor
                    constructorArgumentValues = ConstructorArgumentValues().apply {
                        addIndexedArgumentValue(0, repositoryDefinition)
                        addIndexedArgumentValue(1, contextProviderBean)
                    }
                }
                // Register the new CrudService bean definition
                (beanFactory as DefaultListableBeanFactory).registerBeanDefinition(crudBeanName, bd)

                // Register the new bean as a dependency
                beanFactory.registerDependentBean(crudBeanName, beanName)
            }
        }
    }
}

open class CrudService<ENTITY: BaseEntity, DTO: Any>(
    private val repository: JpaRepository<ENTITY, UUID>,
    applicationContextProvider: ApplicationContextProvider,
) {
    private var context = applicationContextProvider.applicationContext

    private val repositories = context.getRepositoriesMap()
    private val entityClass = repository.getRepositoryEntity()

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