package com.github.maiconhellmann.demo.model

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*


@Entity
@Table(name = "role")
class Role {
    @get:Id
    @get:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var name: String? = null

    @get:ManyToMany(mappedBy = "roles")
    @JsonIgnore
    var users: Set<User>? = null
}