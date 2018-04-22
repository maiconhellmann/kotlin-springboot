package com.github.maiconhellmann.demo.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.social.UserIdSource
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer
import org.springframework.social.config.annotation.EnableSocial
import org.springframework.social.config.annotation.SocialConfigurer
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.ConnectionRepository
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository
import org.springframework.social.connect.web.ConnectController
import org.springframework.social.facebook.connect.FacebookConnectionFactory
import javax.sql.DataSource


@Configuration
@EnableSocial
class SocialConfig : SocialConfigurer {
    @Autowired
    lateinit var dataSource: DataSource

    override fun getUsersConnectionRepository(connectionFactoryLocator: ConnectionFactoryLocator?): UsersConnectionRepository {
        return JdbcUsersConnectionRepository(
                dataSource,
                connectionFactoryLocator,
                Encryptors.noOpText()
        )
    }

    override fun getUserIdSource(): UserIdSource {
        return UserIdSource {
            "teste"
        }
    }

    override fun addConnectionFactories(cfConfig: ConnectionFactoryConfigurer, env: Environment) {
        cfConfig.addConnectionFactory(FacebookConnectionFactory(
                env.getProperty("facebook.clientId"),
                env.getProperty("facebook.clientSecret")))
    }

    @Bean
    fun connectController(connectionFactoryLocator: ConnectionFactoryLocator, connectionRepository: ConnectionRepository): ConnectController {
        return ConnectController(connectionFactoryLocator, connectionRepository)
    }
}