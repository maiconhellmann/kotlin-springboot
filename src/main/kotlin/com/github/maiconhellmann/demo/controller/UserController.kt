package com.github.maiconhellmann.demo.controller

import com.github.maiconhellmann.demo.config.misc.ShaPasswordEncoder
import com.github.maiconhellmann.demo.repository.RoleRepository
import com.github.maiconhellmann.demo.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.social.facebook.api.User
import org.springframework.social.facebook.api.impl.FacebookTemplate
import org.springframework.social.twitter.api.TwitterProfile
import org.springframework.social.twitter.api.impl.TwitterTemplate
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

    @Value("\${spring.social.twitter.appId}")
    lateinit var twitterId: String

    @Value("\${spring.social.twitter.appSecret}")
    lateinit var twitterSecret: String


    @GetMapping("/facebook/login")
    fun loginFacebook(@RequestParam("token") accessToken: String): ResponseEntity<com.github.maiconhellmann.demo.model.User> {
        val facebook = FacebookTemplate(accessToken)

        if (facebook.isAuthorized) {
            val fields = arrayOf("id", "email", "first_name", "last_name", "hometown", "birthday", "address", "about", "cover")

            val userProfile = facebook.fetchObject("me", User::class.java, *fields)
            val email = userProfile.email

            if (email.isNotEmpty()) {
                val password = UUID.randomUUID().toString()
                val roles = roleRepository.findAll().toSet().toMutableList()

                var user = userRepository.findByUsername(email)

                user = if (user != null) {
                    user.copy(password = ShaPasswordEncoder().encode(password),
                            socialPassword = password,
                            roles = roles)
                } else {
                    com.github.maiconhellmann.demo.model.User(
                            username = email,
                            password = ShaPasswordEncoder().encode(password),
                            roles = roles
                    )
                }

                user = userRepository.save(user)

                return ResponseEntity.ok().body(user.copy(socialPassword = password))
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @GetMapping("/twitter/login")
    fun loginTwitter(@RequestParam("consumerKey") consumerKey: String,
                     @RequestParam("consumerSecret") consumerSecret: String): ResponseEntity<com.github.maiconhellmann.demo.model.User> {

        val twitterTemplate = TwitterTemplate(twitterId, twitterSecret, consumerKey, consumerSecret)

        if (twitterTemplate.isAuthorized) {
            val restTemplate = twitterTemplate.restTemplate
            val twitterProfile = restTemplate.getForObject("https://api.twitter.com/1.1/account/verify_credentials.json?include_email=true", TwitterProfile::class.java)

            if (twitterProfile?.extraData?.containsKey("email") == true
                    && twitterProfile.extraData?.get("email")?.toString()?.isNotEmpty() == true) {

                val email = twitterProfile.extraData?.get("email").toString()

                val password = UUID.randomUUID().toString()
                val roles = roleRepository.findAll().toSet().toMutableList()

                var user = userRepository.findByUsername(email)

                user = if (user != null) {
                    user.copy(password = ShaPasswordEncoder().encode(password),
                            socialPassword = password,
                            roles = roles)
                } else {
                    com.github.maiconhellmann.demo.model.User(
                            username = email,
                            password = ShaPasswordEncoder().encode(password),
                            roles = roles
                    )
                }

                user = userRepository.save(user)

                return ResponseEntity.ok().body(user.copy(socialPassword = password))
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }
}