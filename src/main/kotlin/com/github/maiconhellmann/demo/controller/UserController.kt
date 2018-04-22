package com.github.maiconhellmann.demo.controller

import com.github.maiconhellmann.demo.config.misc.ShaPasswordEncoder
import com.github.maiconhellmann.demo.repository.RoleRepository
import com.github.maiconhellmann.demo.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.social.facebook.api.User
import org.springframework.social.facebook.api.impl.FacebookTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping("/user")
class UserController {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @GetMapping
    fun getAllUsers() = userRepository.findAll()


    @GetMapping("/facebook/login")
    fun loginFacebook(@RequestParam("token") accessToken: String): com.github.maiconhellmann.demo.model.User {
        val facebook = FacebookTemplate(accessToken)

        val fields = arrayOf("id", "email", "first_name", "last_name", "hometown", "birthday", "address", "about", "cover")

        val userProfile = facebook.fetchObject("me", User::class.java, *fields)

        //Image
        facebook.userOperations().userProfileImage

        //User info
        userProfile.id
        userProfile.lastName
        userProfile.firstName
        userProfile.email
        userProfile.birthday


        val password = UUID.randomUUID().toString()
        //TODO only the correct role
        val roles = roleRepository.findAll().toSet().toMutableList()

        var user = userRepository.findByUsername(userProfile.email)

        user = if (user != null) {
            user.copy(password = ShaPasswordEncoder().encode(password),
                    socialPassword = password,
                    roles = roles)
        } else {
            com.github.maiconhellmann.demo.model.User(
                    username = userProfile.email,
                    password = ShaPasswordEncoder().encode(password),
                    roles = roles
            )
        }

        user = userRepository.save(user)

        return user.copy(socialPassword = password)
    }
}