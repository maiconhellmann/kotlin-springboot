package com.github.maiconhellmann.demo.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import javax.annotation.PostConstruct
import javax.sql.DataSource

@Configuration
@EnableResourceServer
class ResourceServerConfig : ResourceServerConfigurerAdapter() {
    @Autowired
    private val tokenServices: ResourceServerTokenServices? = null

    @Autowired
    lateinit var dataSource: DataSource

    @Autowired
    lateinit var webApplicationContext: ApplicationContext

    @Value("\${security.jwt.resource-ids}")
    private val resourceIds: String? = null

    @Throws(Exception::class)
    override fun configure(resources: ResourceServerSecurityConfigurer?) {
        resources!!.resourceId(resourceIds).tokenServices(tokenServices)
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {

        http.requestMatchers()
                .and()
                .authorizeRequests()
                .antMatchers("/api/user/facebook/login").permitAll()
                .anyRequest().authenticated().and().httpBasic()

    }

    @PostConstruct
    fun createTables() {
//        val resource = webApplicationContext.getResource("classpath:import.sql")
//        ScriptUtils.executeSqlScript(dataSource.connection, resource)
    }
}
