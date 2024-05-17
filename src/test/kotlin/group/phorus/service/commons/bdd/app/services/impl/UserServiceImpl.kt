package group.phorus.service.commons.bdd.app.services.impl

import group.phorus.service.commons.bdd.app.repositories.UserRepository
import group.phorus.service.commons.bdd.app.services.UserService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    userRepository: UserRepository,
) : UserService(userRepository)