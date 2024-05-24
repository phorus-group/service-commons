package group.phorus.service.commons.bdd.app.services.impl

import group.phorus.service.commons.bdd.app.model.Address
import group.phorus.service.commons.bdd.app.repositories.AddressRepository
import group.phorus.service.commons.bdd.app.services.AddressService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class AddressServiceImpl(
    private val addressRepository: AddressRepository,
    applicationContext: ApplicationContext,
) : AddressService(addressRepository, applicationContext) {
    override suspend fun findAllByUserID(userID: UUID, pageable: Pageable): Page<Address> =
        withContext(Dispatchers.IO) {
            addressRepository.findAllByUserId(userID, pageable)
        }
}
