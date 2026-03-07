package group.phorus.service.commons.bdd.app.model

import group.phorus.service.commons.model.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false)
    var name: String? = null,
) : BaseEntity()
