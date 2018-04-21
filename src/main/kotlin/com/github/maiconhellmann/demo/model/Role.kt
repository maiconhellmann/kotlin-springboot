package com.github.maiconhellmann.demo.model

import javax.persistence.*


@Entity
@Table(name = "role")
class Role {
    @get:Id
    @get:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var name: String? = null

    @get:ManyToMany(mappedBy = "roles")
    var users: Set<User>? = null
}