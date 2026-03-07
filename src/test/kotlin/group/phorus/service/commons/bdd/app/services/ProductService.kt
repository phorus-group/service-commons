package group.phorus.service.commons.bdd.app.services

import group.phorus.service.commons.bdd.app.dtos.ProductDTO
import group.phorus.service.commons.bdd.app.dtos.ProductResponse
import group.phorus.service.commons.bdd.app.model.Product
import group.phorus.service.commons.service.SimpleCrudService
import org.springframework.stereotype.Service
import java.util.*

@Service
class ProductService : SimpleCrudService<Product, ProductDTO, ProductResponse>() {
    override suspend fun create(dto: ProductDTO): UUID {
        dto.name = dto.name?.uppercase()
        return super.create(dto)
    }
}
