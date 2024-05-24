package group.phorus.service.commons.bdd.app.services.impl

import group.phorus.service.commons.bdd.app.repositories.UserRepository
import group.phorus.service.commons.bdd.app.services.UserService
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    userRepository: UserRepository,
    applicationContext: ApplicationContext,
) : UserService(userRepository, applicationContext)