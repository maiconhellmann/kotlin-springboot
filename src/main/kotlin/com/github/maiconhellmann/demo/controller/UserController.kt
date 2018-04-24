package com.github.maiconhellmann.demo.controller

import com.github.maiconhellmann.demo.config.misc.ShaPasswordEncoder
import com.github.maiconhellmann.demo.repository.RoleRepository
import com.github.maiconhellmann.demo.repository.UserRepository
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
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

    @Value("\${spring.security.oauth2.client.registration.google.client-id}")
    lateinit var googleClientId: String

    @GetMapping("/signin/facebook")
    fun loginFacebook(@RequestParam("token") accessToken: String): ResponseEntity<com.github.maiconhellmann.demo.model.User> {
        val facebook = FacebookTemplate(accessToken)

        if (facebook.isAuthorized) {
            val fields = arrayOf("id", "email", "first_name", "last_name", "hometown", "birthday", "address", "about", "cover")

            val userProfile = facebook.fetchObject("me", User::class.java, *fields)
            val email = userProfile.email

            return if (email.isNotEmpty()) {
                val password = UUID.randomUUID().toString()

                val user = createOrUpdateUser(email, password)

                ResponseEntity.ok().body(user.copy(socialTokenSecret = password))
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @GetMapping("/signin/twitter")
    fun loginTwitter(@RequestParam("consumerKey") consumerKey: String,
                     @RequestParam("consumerSecret") consumerSecret: String): ResponseEntity<com.github.maiconhellmann.demo.model.User> {

        val twitterTemplate = TwitterTemplate(twitterId, twitterSecret, consumerKey, consumerSecret)

        if (twitterTemplate.isAuthorized) {
            val restTemplate = twitterTemplate.restTemplate
            val twitterProfile = restTemplate.getForObject("https://api.twitter.com/1.1/account/verify_credentials.json?include_email=true", TwitterProfile::class.java)

            return if (twitterProfile?.extraData?.containsKey("email") == true
                    && twitterProfile.extraData?.get("email")?.toString()?.isNotEmpty() == true) {

                val email = twitterProfile.extraData?.get("email").toString()
                val password = UUID.randomUUID().toString()

                val user = createOrUpdateUser(email, password)

                ResponseEntity.ok(user.copy(socialTokenSecret = password))
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    private fun createOrUpdateUser(email: String, password: String): com.github.maiconhellmann.demo.model.User {
        val roles = roleRepository.findAll().toSet().toMutableList()

        var user = userRepository.findByUsername(email)

        user = if (user != null) {
            user.copy(password = ShaPasswordEncoder().encode(password),
                    socialTokenSecret = password,
                    roles = roles)
        } else {
            com.github.maiconhellmann.demo.model.User(
                    username = email,
                    password = ShaPasswordEncoder().encode(password),
                    roles = roles
            )
        }

        return userRepository.save(user)
    }

    @GetMapping("/signin/google")
    fun loginGoogle(@RequestParam("token") idTokenString: String): ResponseEntity<com.github.maiconhellmann.demo.model.User> {

        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance())
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(googleClientId))
                .setIssuer("https://accounts.google.com")
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build()


        val idToken = verifier.verify(idTokenString)

        if (idToken != null) {
            val payload = idToken.payload

            // Print user identifier
            val userId = payload.subject

            // Get profile information from payload
            val email = payload.email
//            val emailVerified = java.lang.Boolean.valueOf(payload.emailVerified)
//            val name = payload["name"] as String
//            val pictureUrl = payload["picture"] as String
//            val locale = payload["locale"] as String
//            val familyName = payload["family_name"] as String
//            val givenName = payload["given_name"] as String

            return if (email.isNotEmpty()) {
                val password = UUID.randomUUID().toString()
                val user = createOrUpdateUser(email, password)

                ResponseEntity.ok().body(user.copy(socialTokenSecret = password))
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

    }
}