package com.github.maiconhellmann.demo.model

import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "user")
data class User(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = 0,

        @get: NotBlank
        var username: String = "",

        @get: NotBlank
        var password: String = "",

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(name = "user_role",
                joinColumns = [(JoinColumn(name = "user_id"))],
                inverseJoinColumns = (arrayOf(JoinColumn(name = "role_id"))))
        var roles: Set<Role> = emptySet()

)