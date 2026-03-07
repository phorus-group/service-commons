package group.phorus.service.commons.bdd.app.controllers

import group.phorus.service.commons.bdd.app.dtos.ProductDTO
import group.phorus.service.commons.bdd.app.dtos.ProductResponse
import group.phorus.service.commons.bdd.app.model.Product
import group.phorus.service.commons.controller.SimpleCrudController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/product")
class ProductController : SimpleCrudController<Product, ProductDTO, ProductResponse>()
