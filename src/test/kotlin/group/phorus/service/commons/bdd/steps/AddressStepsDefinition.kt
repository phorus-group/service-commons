package group.phorus.service.commons.bdd.steps

import group.phorus.mapper.mapping.extensions.mapTo
import group.phorus.mapper.mapping.extensions.updateFrom
import group.phorus.service.commons.bdd.app.dtos.AddressDTO
import group.phorus.service.commons.bdd.app.dtos.AddressResponse
import group.phorus.service.commons.bdd.app.model.Address
import group.phorus.service.commons.bdd.app.repositories.AddressRepository
import group.phorus.service.commons.bdd.app.repositories.UserRepository
import group.phorus.test.commons.bdd.BaseRequestScenarioScope
import group.phorus.test.commons.bdd.BaseResponseScenarioScope
import group.phorus.test.commons.bdd.BaseScenarioScope
import group.phorus.test.commons.bdd.RestResponsePage
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.returnResult
import java.util.*
import kotlin.jvm.optionals.getOrNull


class AddressStepsDefinition(
    @Autowired private val baseScenarioScope: BaseScenarioScope,
    @Autowired private val requestScenarioScope: BaseRequestScenarioScope,
    @Autowired private val responseScenarioScope: BaseResponseScenarioScope,
    @Autowired private val addressRepository: AddressRepository,
    @Autowired private val userRepository: UserRepository,
) {
    @Given("the caller has the given Address:")
    fun `the caller has the given Address`(data: DataTable) {
        val address = data.asMaps().first().let {
            AddressDTO(
                address = it["address"],
                userID = (baseScenarioScope.objects["userID"] as String).let { id -> UUID.fromString(id) },
            )
        }

        requestScenarioScope.request = address
    }

    @Given("the given Address exists:")
    fun `the given Address exists`(data: DataTable) {
        val userID = (baseScenarioScope.objects["userID"] as String).let { id -> UUID.fromString(id) }
        val user = userRepository.findById(userID).get()

        val address = data.asMaps().first().let {
            Address(
                address = it["address"],
                user = user,
            )
        }

        baseScenarioScope.objects["addressResponse"] = addressRepository.saveAndFlush(address).mapTo<AddressResponse>()!!
        baseScenarioScope.objects["addressID"] = (baseScenarioScope.objects["addressResponse"] as AddressResponse).id!!.toString()
    }


    @Then("the new Address was created")
    fun `the new Address was created`() {
        val addressID = responseScenarioScope.responseSpec!!
            .returnResult<Void>().responseHeaders
            .location!!.path
            .replace("/address/", "")
            .let { UUID.fromString(it) }

        val newAddress = addressRepository.findById(addressID).getOrNull()?.mapTo<AddressDTO>()

        assertEquals(requestScenarioScope.request as AddressDTO, newAddress)
    }

    @Then("the updated Address is found in the database")
    fun `the updated address is found in the database`() {
        val oldAddress = baseScenarioScope.objects["addressResponse"] as AddressResponse

        val updatedAddress = addressRepository.findById(oldAddress.id!!).getOrNull()?.mapTo<AddressResponse>()
        assertNotEquals(oldAddress, updatedAddress)

        val expectedAddress = oldAddress.updateFrom(requestScenarioScope.request!!)
        assertEquals(expectedAddress, updatedAddress)
    }

    @Then("the Address was removed from the database")
    fun `the Address was removed from the database`() {
        val addressID = (baseScenarioScope.objects["addressID"] as String).let { UUID.fromString(it) }

        val address = addressRepository.findById(addressID).getOrNull()

        assertNull(address)
    }

    @Then("the service returns the Address")
    fun `the service returns the address`() {
        val addressResponse = responseScenarioScope.responseSpec!!
            .expectBody<AddressResponse>().returnResult().responseBody!!

        assertEquals(baseScenarioScope.objects["addressResponse"] as AddressResponse, addressResponse)
    }

    @Then("the service returns a page with the matching Addresses")
    fun `the service returns a page with the matching Addresses`() {
        val page = responseScenarioScope.responseSpec!!
            .expectBody<RestResponsePage<AddressResponse>>()
            .returnResult().responseBody!!

        assertTrue(!page.isEmpty)
        assertEquals(1, page.totalPages)
        assertEquals(1, page.totalElements)
        assertEquals(1, page.content.size)

        assertEquals(baseScenarioScope.objects["addressResponse"] as AddressResponse, page.content.first())
    }
}