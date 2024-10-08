package group.phorus.service.commons.bdd.app.controllers

import group.phorus.service.commons.bdd.app.dtos.UserDTO
import group.phorus.service.commons.bdd.app.dtos.UserResponse
import group.phorus.service.commons.bdd.app.model.User
import group.phorus.service.commons.controller.CrudController
import group.phorus.service.commons.service.CrudService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    userService: CrudService<User, UserDTO>,
) : CrudController<User, UserDTO, UserResponse>(UserResponse::class, "/user", userService)