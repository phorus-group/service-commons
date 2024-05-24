package group.phorus.service.commons.bdd.app.services

import group.phorus.service.commons.bdd.app.dtos.AddressDTO
import group.phorus.service.commons.bdd.app.model.Address
import group.phorus.service.commons.bdd.app.repositories.AddressRepository
import group.phorus.service.commons.bdd.app.repositories.UserRepository
import group.phorus.service.commons.service.CrudService
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

abstract class AddressService(
    addressRepository: AddressRepository,
    applicationContext: ApplicationContext,
): CrudService<Address, AddressDTO>(Address::class, addressRepository, applicationContext) {
    abstract suspend fun findAllByUserId(userId: UUID, pageable: Pageable): Page<Address>
}
