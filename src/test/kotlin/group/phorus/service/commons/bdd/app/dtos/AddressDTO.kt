package group.phorus.service.commons.bdd.app.dtos

import group.phorus.mapper.mapping.MapFrom
import group.phorus.service.commons.dtos.validationGroups.Create
import group.phorus.service.commons.service.MapTo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*

data class AddressDTO (
    @field:NotBlank(groups = [Create::class], message = "Cannot be blank")
    var address: String? = null,

    @MapFrom(["user/id"])
    @MapTo("user")
    @field:NotNull(groups = [Create::class], message = "Cannot be null")
    var userID: UUID? = null,
)

data class AddressResponse(
    var id: UUID? = null,
    var address: String? = null,

    @MapFrom(["user/id"])
    var userID: UUID? = null,
)
