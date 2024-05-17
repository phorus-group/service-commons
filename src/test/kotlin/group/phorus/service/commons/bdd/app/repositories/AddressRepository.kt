package group.phorus.service.commons.bdd.app.repositories

import group.phorus.service.commons.bdd.app.model.Address
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AddressRepository: JpaRepository<Address, UUID>{
    fun findAllByUserId(userID: UUID, pageable: Pageable): Page<Address>
}
