package com.github.maiconhellmann.demo.service.auth

import com.github.maiconhellmann.demo.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.*

@Component
class AppUserDetailsService : UserDetailsService {
    @Autowired
    lateinit var userRepository: UserRepository

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(s: String): UserDetails {
        val user = userRepository.findByUsername(s)
                ?: throw UsernameNotFoundException(String.format("The username %s doesn't exist", s))

        val authorities = ArrayList<GrantedAuthority>()
        user.roles.forEach { role -> authorities.add(SimpleGrantedAuthority(role.name)) }

        return org.springframework.security.core.userdetails.User(user.username, user.password, authorities)
    }
}
