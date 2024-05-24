package group.phorus.service.commons.bdd.steps

import group.phorus.mapper.mapping.extensions.mapTo
import group.phorus.mapper.mapping.extensions.updateFrom
import group.phorus.service.commons.bdd.app.dtos.UserDTO
import group.phorus.service.commons.bdd.app.dtos.UserResponse
import group.phorus.service.commons.bdd.app.model.User
import group.phorus.service.commons.bdd.app.repositories.UserRepository
import group.phorus.test.commons.bdd.BaseRequestScenarioScope
import group.phorus.test.commons.bdd.BaseResponseScenarioScope
import group.phorus.test.commons.bdd.BaseScenarioScope
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.returnResult
import java.util.*
import kotlin.jvm.optionals.getOrNull


class UserStepsDefinition(
    @Autowired private val baseScenarioScope: BaseScenarioScope,
    @Autowired private val requestScenarioScope: BaseRequestScenarioScope,
    @Autowired private val responseScenarioScope: BaseResponseScenarioScope,
    @Autowired private val userRepository: UserRepository,
) {
    @Given("the caller has the given User:")
    fun `the caller has the given User`(data: DataTable) {
        val user = data.asMaps().first().let {
            UserDTO(
                name = it["name"],
            )
        }

        requestScenarioScope.request = user
    }

    @Given("the given User exists:")
    fun `the given User exists`(data: DataTable) {
        val user = data.asMaps().first().let {
            User(
                name = it["name"],
            )
        }

        baseScenarioScope.objects["userResponse"] = userRepository.saveAndFlush(user).mapTo<UserResponse>()!!
        baseScenarioScope.objects["userId"] = (baseScenarioScope.objects["userResponse"] as UserResponse).id!!.toString()
    }


    @Then("the new User was created")
    fun `the new User was created`() {
        val userId = responseScenarioScope.responseSpec!!
            .returnResult<Void>().responseHeaders
            .location!!.path
            .replace("/user/", "")
            .let { UUID.fromString(it) }

        val newUser = userRepository.findById(userId).getOrNull()?.mapTo<UserDTO>()

        assertEquals(requestScenarioScope.request as UserDTO, newUser)
    }

    @Then("the updated User is found in the database")
    fun `the updated user is found in the database`() {
        val oldUser = baseScenarioScope.objects["userResponse"] as UserResponse

        val updatedUser = userRepository.findById(oldUser.id!!).getOrNull()?.mapTo<UserResponse>()
        assertNotEquals(oldUser, updatedUser)

        val expectedUser = oldUser.updateFrom(requestScenarioScope.request!!)
        assertEquals(expectedUser, updatedUser)
    }

    @Then("the User was removed from the database")
    fun `the User was removed from the database`() {
        val userId = (baseScenarioScope.objects["userId"] as String).let { UUID.fromString(it) }

        val user = userRepository.findById(userId).getOrNull()

        assertNull(user)
    }

    @Then("the service returns the User")
    fun `the service returns the user`() {
        val userResponse = responseScenarioScope.responseSpec!!
            .expectBody<UserResponse>().returnResult().responseBody!!

        assertEquals(baseScenarioScope.objects["userResponse"] as UserResponse, userResponse)
    }

    @Then("the service returns a message with the validation errors")
    fun `the service returns a message with the validation errors`(data: DataTable) {
        val obj = data.asMaps().first()["obj"]!!
        val field = data.asMaps().first()["field"]!!
        val rejectedValue = data.asMaps().first()["rejectedValue"]!!
        val message = data.asMaps().first()["message"]!!

        responseScenarioScope.responseSpec!!
            .expectBody()
            .jsonPath("$.apierror.validationErrors[0].obj").isEqualTo(obj)
            .jsonPath("$.apierror.validationErrors[0].field").isEqualTo(field)
            .let {
                when (rejectedValue) {
                    "null" -> it.jsonPath("$.apierror.validationErrors[0].rejectedValue").doesNotExist()
                    "blank" -> it.jsonPath("$.apierror.validationErrors[0].rejectedValue").isEqualTo("")
                    "[]" -> it.jsonPath("$.apierror.validationErrors[0].rejectedValue").isEmpty
                    else -> it.jsonPath("$.apierror.validationErrors[0].rejectedValue").isEqualTo(rejectedValue)
                }
            }
            .jsonPath("$.apierror.validationErrors[0].message").isEqualTo(message)
    }
}