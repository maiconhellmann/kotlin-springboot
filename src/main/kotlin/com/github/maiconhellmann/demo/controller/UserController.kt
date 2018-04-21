package com.github.maiconhellmann.demo.controller

import com.github.maiconhellmann.demo.repository.UserRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/user")
class UserController(private val userRepository: UserRepository) {


    @GetMapping
    fun getAllUsers() = userRepository.findAll()
}