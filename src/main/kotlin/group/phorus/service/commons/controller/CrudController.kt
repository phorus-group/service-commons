package group.phorus.service.commons.controller

import group.phorus.mapper.OriginalEntity
import group.phorus.mapper.mapping.mapTo
import group.phorus.service.commons.dtos.validationGroups.Create
import group.phorus.service.commons.dtos.validationGroups.Update
import group.phorus.service.commons.model.BaseEntity
import group.phorus.service.commons.service.CrudService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

abstract class CrudController<ENTITY: BaseEntity, DTO: Any, RESPONSE: Any>(
    private val responseClass: KClass<RESPONSE>,
    private val basePath: String,
    private val service: CrudService<ENTITY, DTO>,
) {
    @Suppress("UNCHECKED_CAST")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    suspend fun findById(
        @PathVariable
        id: UUID,
    ): RESPONSE =
        service.findById(id).let {
            mapTo(
                originalEntity = OriginalEntity(it, it::class.starProjectedType),
                targetType = responseClass.createType()
            ) as RESPONSE
        }

    @PostMapping
    suspend fun create(
        @Validated(Create::class)
        @RequestBody
        dto: DTO,
    ): ResponseEntity<Void> =
        service.create(dto)
            .let { ResponseEntity.created(URI.create("${basePath}/$it")).build() }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun update(
        @PathVariable
        id: UUID,

        @Validated(Update::class)
        @RequestBody
        dto: DTO,
    ) {
        service.update(id, dto)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: UUID) {
        service.delete(id)
    }
}