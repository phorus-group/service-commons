package group.phorus.service.commons.bdd.app.repositories

import group.phorus.service.commons.bdd.app.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: JpaRepository<User, UUID>
