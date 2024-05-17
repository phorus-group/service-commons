package group.phorus.service.commons.bdd.app.services.impl

import group.phorus.service.commons.bdd.app.model.Address
import group.phorus.service.commons.bdd.app.repositories.AddressRepository
import group.phorus.service.commons.bdd.app.repositories.UserRepository
import group.phorus.service.commons.bdd.app.services.AddressService
import group.phorus.service.commons.service.Mapping
import group.phorus.service.commons.service.Mappings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
@Mappings(
    Mapping(
        sourceField = "userID",
        targetField = "user",
        repository = UserRepository::class,
    )
)
class AddressServiceImpl(
    private val addressRepository: AddressRepository,
    userRepository: Map<String, UserRepository>,
) : AddressService(addressRepository, userRepository) {
    override suspend fun findAllByUserID(userID: UUID, pageable: Pageable): Page<Address> =
        withContext(Dispatchers.IO) {
            addressRepository.findAllByUserId(userID, pageable)
        }
}
