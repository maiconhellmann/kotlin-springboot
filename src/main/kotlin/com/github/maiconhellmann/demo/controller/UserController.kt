package com.github.maiconhellmann.demo.controller

import com.github.maiconhellmann.demo.config.misc.ShaPasswordEncoder
import com.github.maiconhellmann.demo.model.User
import com.github.maiconhellmann.demo.repository.RoleRepository
import com.github.maiconhellmann.demo.repository.UserRepository
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.social.facebook.api.impl.FacebookTemplate
import org.springframework.social.twitter.api.TwitterProfile
import org.springframework.social.twitter.api.impl.TwitterTemplate
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/user")
class UserController {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Value("\${spring.social.twitter.appId}")
    lateinit var twitterId: String

    @Value("\${spring.social.twitter.appSecret}")
    lateinit var twitterSecret: String

    @Value("\${spring.security.oauth2.client.registration.google.client-id}")
    lateinit var googleClientId: String

    @GetMapping
    fun getAllUsers() = userRepository.findAll()

    @PostMapping("/signin/facebook")
    fun singninFacebook(@RequestParam("token") accessToken: String): ResponseEntity<User> {
        val facebook = FacebookTemplate(accessToken)

        if (facebook.isAuthorized) {
            val fields = arrayOf("id", "email", "first_name", "last_name", "hometown", "birthday", "address", "about", "cover")

            val userProfile = facebook.fetchObject("me", org.springframework.social.facebook.api.User::class.java, *fields)
            val email = userProfile.email

            return if (email.isNotEmpty()) {
                val password = generatePassword()

                val user = createOrUpdateUser(email, password)

                ResponseEntity.ok().body(user)
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/signin/twitter")
    fun signinTwitter(@RequestParam("consumerKey") consumerKey: String,
                      @RequestParam("consumerSecret") consumerSecret: String): ResponseEntity<User> {

        val twitterTemplate = TwitterTemplate(twitterId, twitterSecret, consumerKey, consumerSecret)

        if (twitterTemplate.isAuthorized) {
            val restTemplate = twitterTemplate.restTemplate
            val twitterProfile = restTemplate.getForObject("https://api.twitter.com/1.1/account/verify_credentials.json?include_email=true", TwitterProfile::class.java)

            return if (twitterProfile?.extraData?.containsKey("email") == true
                    && twitterProfile.extraData?.get("email")?.toString()?.isNotEmpty() == true) {

                val email = twitterProfile.extraData?.get("email").toString()
                val password = generatePassword()

                val user = createOrUpdateUser(email, password)

                ResponseEntity.ok(user)
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/signin/google")
    fun signinGoogle(@RequestParam("token") idTokenString: String): ResponseEntity<User> {

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
//            val userId = payload.subject

            // Get profile information from payload
            val email = payload.email
//            val emailVerified = java.lang.Boolean.valueOf(payload.emailVerified)
//            val name = payload["name"] as String
//            val pictureUrl = payload["picture"] as String
//            val locale = payload["locale"] as String
//            val familyName = payload["family_name"] as String
//            val givenName = payload["given_name"] as String

            return if (email.isNotEmpty()) {
                val password = generatePassword()
                val user = createOrUpdateUser(email, password)

                ResponseEntity.ok().body(user)
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    private fun generatePassword(): String {
        return UUID.randomUUID().toString()
    }

    @PostMapping("/signin/email")
    fun signinEmail(@RequestBody newUser: User): ResponseEntity<User> {

        return if (newUser.password.isEmpty() || newUser.username.isEmpty()) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } else {
            val user = createOrUpdateUser(newUser.username, newUser.password)

            ResponseEntity.ok().body(user)
        }
    }

    private fun createOrUpdateUser(email: String, password: String): User {
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
}