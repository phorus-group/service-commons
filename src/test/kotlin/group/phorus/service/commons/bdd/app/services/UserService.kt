package group.phorus.service.commons.bdd.app.services

import group.phorus.service.commons.bdd.app.dtos.UserDTO
import group.phorus.service.commons.bdd.app.model.User
import group.phorus.service.commons.bdd.app.repositories.UserRepository
import group.phorus.service.commons.service.CrudService

abstract class UserService(
    userRepository: UserRepository,
): CrudService<User, UserDTO>(User::class, userRepository)
