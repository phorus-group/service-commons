package group.phorus.service.commons.bdd.app.dtos

import group.phorus.service.commons.dtos.validationGroups.Create
import jakarta.validation.constraints.NotBlank
import java.util.*

data class UserDTO (
    @field:NotBlank(groups = [Create::class], message = "Cannot be blank")
    var name: String? = null,

    @field:NotBlank(groups = [Create::class], message = "Cannot be blank")
    var surname: String? = null,
)

data class UserResponse(
    var id: UUID? = null,
    var name: String? = null,
    var surname: String? = null,
)
