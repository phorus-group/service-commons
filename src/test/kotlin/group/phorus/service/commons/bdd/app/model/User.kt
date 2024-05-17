package group.phorus.service.commons.bdd.app.model

import group.phorus.service.commons.model.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    var name: String? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.REMOVE])
    var addresses: MutableSet<Address> = mutableSetOf(),
) : BaseEntity()

