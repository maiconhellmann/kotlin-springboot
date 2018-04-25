package com.github.maiconhellmann.demo.service.user

import com.github.maiconhellmann.demo.config.misc.ShaPasswordEncoder
import com.github.maiconhellmann.demo.model.User
import com.github.maiconhellmann.demo.repository.RoleRepository
import com.github.maiconhellmann.demo.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserService {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var roleRepository: RoleRepository


    fun findAllUsers() = userRepository.findAll()

    fun createOrUpdateUser(email: String, password: String): User {
        val roles = roleRepository.findAll().toSet().toMutableList()

        var user = userRepository.findByUsername(email)

        user = if (user != null) {
            user.copy(password = ShaPasswordEncoder().encode(password),
                    roles = roles)
        } else {
            User(
                    username = email,
                    password = ShaPasswordEncoder().encode(password),
                    roles = roles
            )
        }

        return userRepository.save(user)
    }
    fun generatePassword(): String {
        return UUID.randomUUID().toString()
    }
}