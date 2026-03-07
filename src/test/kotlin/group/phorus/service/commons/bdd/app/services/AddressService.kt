package group.phorus.service.commons.bdd.app.services

import group.phorus.service.commons.bdd.app.dtos.AddressDTO
import group.phorus.service.commons.bdd.app.dtos.AddressResponse
import group.phorus.service.commons.bdd.app.model.Address
import group.phorus.service.commons.service.SimpleCrudService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

abstract class AddressService : SimpleCrudService<Address, AddressDTO, AddressResponse>() {
    abstract suspend fun findAllByUserId(userId: UUID, pageable: Pageable): Page<Address>
}