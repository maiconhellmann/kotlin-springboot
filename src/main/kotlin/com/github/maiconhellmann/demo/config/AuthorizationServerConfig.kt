package com.github.maiconhellmann.demo.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.provider.token.TokenEnhancer
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import java.util.*

@Configuration
@EnableAuthorizationServer
class AuthorizationServerConfig : AuthorizationServerConfigurerAdapter() {

    @Value("\${security.jwt.client-id}")
    lateinit var clientId: String

    @Value("\${security.jwt.client-secret}")
    lateinit var clientSecret: String

    @Value("\${security.jwt.grant-type}")
    lateinit var grantType: String

    @Value("\${security.jwt.scope-read}")
    lateinit var scopeRead: String

    @Value("\${security.jwt.scope-write}")
    private val scopeWrite = "write"

    @Value("\${security.jwt.resource-ids}")
    lateinit var resourceIds: String

    @Value("\${token_validity_seconds}")
    var tokenValiditySeconds: Int = 2592000

    @Value("\${refresh_token_validity_seconds}")
    var refreshTokenValiditySeconds: Int = 3600

    @Value("\${server.servlet.path}")
    lateinit var servletPath: String

    @Autowired
    lateinit var tokenStore: TokenStore

    @Autowired
    lateinit var accessTokenConverter: JwtAccessTokenConverter

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Throws(Exception::class)
    override fun configure(configurer: ClientDetailsServiceConfigurer) {
        configurer
                .inMemory()
                .withClient(clientId)
                .secret(clientSecret)
                .authorizedGrantTypes(*grantType.split(",").toTypedArray())
                .scopes(scopeRead, scopeWrite)
                .resourceIds(resourceIds)
                .accessTokenValiditySeconds(tokenValiditySeconds)
                .refreshTokenValiditySeconds(refreshTokenValiditySeconds)
    }

    @Throws(Exception::class)
    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        val enhancerChain = TokenEnhancerChain()
        enhancerChain.setTokenEnhancers(Arrays.asList<TokenEnhancer>(accessTokenConverter))
        endpoints.tokenStore(tokenStore)
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST)
                .accessTokenConverter(accessTokenConverter)
                .tokenEnhancer(enhancerChain)
                .authenticationManager(authenticationManager)
                .prefix(servletPath)
    }

}
