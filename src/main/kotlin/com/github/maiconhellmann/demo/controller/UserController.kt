package com.github.maiconhellmann.demo.controller

import com.github.maiconhellmann.demo.model.User
import com.github.maiconhellmann.demo.service.CustomLinkedinTemplate
import com.github.maiconhellmann.demo.service.user.UserService
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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
@Api(value="User Controller", description="Operations pertaining to users in the system")
@RequestMapping("/user")
class UserController {

    @Value("\${spring.social.twitter.appId}")
    lateinit var twitterId: String

    @Value("\${spring.social.twitter.appSecret}")
    lateinit var twitterSecret: String

    @Value("\${spring.security.oauth2.client.registration.google.client-id}")
    lateinit var googleClientId: String

    @Autowired
    lateinit var userService: UserService

    @GetMapping
    @ApiOperation("List all users")
    fun getAllUsers() = userService.findAllUsers()

    @PostMapping("/signin/facebook")
    @ApiOperation("Create an user with Facebook information")
    @ApiResponses(value = [
        ApiResponse(code = 403, message = "The system did not have access to all the necessary information. Probably the email is not confirmed or does not allow access."),
        ApiResponse(code = 401, message = "Access was not authorized by the source")])
    fun singninFacebook(@RequestParam("token") accessToken: String): ResponseEntity<User> {
        val facebook = FacebookTemplate(accessToken)

        if (facebook.isAuthorized) {
            val fields = arrayOf("id", "email", "first_name", "last_name", "hometown", "birthday", "address", "about", "cover")

            val userProfile = facebook.fetchObject("me", org.springframework.social.facebook.api.User::class.java, *fields)
            val email = userProfile.email

            return if (email.isNotEmpty()) {
                val password = userService.generatePassword()

                val user = userService.createOrUpdateUser(email, password)

                ResponseEntity.ok(user.copy(password = password))
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/signin/twitter")
    @ApiOperation("Create an user with Twitter information")
    @ApiResponses(value = [
        ApiResponse(code = 403, message = "The system did not have access to all the necessary information. Probably the email is not confirmed or does not allow access."),
        ApiResponse(code = 401, message = "Access was not authorized by the source")])
    fun signinTwitter(@RequestParam("consumerKey") consumerKey: String,
                      @RequestParam("consumerSecret") consumerSecret: String): ResponseEntity<User> {

        val twitterTemplate = TwitterTemplate(twitterId, twitterSecret, consumerKey, consumerSecret)

        if (twitterTemplate.isAuthorized) {
            val restTemplate = twitterTemplate.restTemplate
            val twitterProfile = restTemplate.getForObject("https://api.twitter.com/1.1/account/verify_credentials.json?include_email=true", TwitterProfile::class.java)

            return if (twitterProfile?.extraData?.containsKey("email") == true
                    && twitterProfile.extraData?.get("email")?.toString()?.isNotEmpty() == true) {

                val email = twitterProfile.extraData?.get("email").toString()
                val password = userService.generatePassword()

                val user = userService.createOrUpdateUser(email, password)

                ResponseEntity.ok(user.copy(password = password))
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/signin/google")
    @ApiOperation("Create an user with Google information")
    @ApiResponses(value = [
        ApiResponse(code = 403, message = "The system did not have access to all the necessary information. Probably the email is not confirmed or does not allow access."),
        ApiResponse(code = 401, message = "Access was not authorized by the source")])
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
                val password = userService.generatePassword()
                val user = userService.createOrUpdateUser(email, password)

                ResponseEntity.ok(user.copy(password = password))
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }


    @ApiOperation("Create an user with provided information")
    @PostMapping("/signin/email")
    @ApiResponses(value = [ApiResponse(code = 403, message = "When username or password is null")])
    fun signinEmail(@RequestBody newUser: User): ResponseEntity<User> {

        return if (newUser.password.isEmpty() || newUser.username.isEmpty()) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } else {
            val user = userService.createOrUpdateUser(newUser.username, newUser.password)

            ResponseEntity.ok().body(user.copy(password = ""))
        }
    }

    @PostMapping("/signin/linkedin")
    @ApiOperation("Create an user with linkedIn information")
    @ApiResponses(value = [
        ApiResponse(code = 403, message = "The system did not have access to all the necessary information. Probably the email is not confirmed or does not allow access."),
        ApiResponse(code = 401, message = "Access was not authorized by the source")])
    fun signinLinkedin(@RequestParam("token") accessToken: String): ResponseEntity<User> {
        val linkedin = CustomLinkedinTemplate(accessToken)

        if (linkedin.isAuthorized) {

            return if (linkedin.profileOperations().userProfile.emailAddress.isNotEmpty()) {

                val email = linkedin.profileOperations().userProfile.emailAddress
                val password = userService.generatePassword()

                val user = userService.createOrUpdateUser(email, password)

                ResponseEntity.ok(user.copy(password = password))
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

}