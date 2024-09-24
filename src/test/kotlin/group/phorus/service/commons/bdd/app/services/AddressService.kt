package group.phorus.service.commons.bdd.app.services

import group.phorus.service.commons.bdd.app.dtos.AddressDTO
import group.phorus.service.commons.bdd.app.model.Address
import group.phorus.service.commons.bdd.app.repositories.AddressRepository
import group.phorus.service.commons.service.ApplicationContextProvider
import group.phorus.service.commons.service.CrudService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

abstract class AddressService(
    addressRepository: AddressRepository,
    applicationContextProvider: ApplicationContextProvider,
): CrudService<Address, AddressDTO>(addressRepository, applicationContextProvider) {
    abstract suspend fun findAllByUserId(userId: UUID, pageable: Pageable): Page<Address>
}
