package com.github.maiconhellmann.demo.config.swagger

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.web.bind.annotation.RequestMethod
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.builders.ResponseMessageBuilder
import springfox.documentation.schema.ModelRef
import springfox.documentation.service.*
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.ApiKeyVehicle
import springfox.documentation.swagger.web.SecurityConfiguration
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.*


@Configuration
@EnableSwagger2
@PropertySource("classpath:application.properties")
class SwaggerConfig {

    @Value("\${security.jwt.client-id}")
    private val clientId: String? = null

    @Value("\${security.jwt.client-secret}")
    private val clientSecret: String = "whysp09"

    @Value("\${swagger.ui.oauth2.token.url}")
    private val authLink: String? = "http://localhost:8080/app/api/oauth/token"

    @Value("\${swagger.controller.package}")
    private val controllerPackage: String? = null

    @Bean
    fun api(): Docket {

        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(controllerPackage))
                .paths(PathSelectors.any())
                .build().securitySchemes(Collections.singletonList(securitySchema()))
                .securityContexts(Collections.singletonList(securityContext())).pathMapping("/api")
                .useDefaultResponseMessages(false).apiInfo(apiInfo()).globalResponseMessage(RequestMethod.GET, defaultResponses())
                .globalResponseMessage(RequestMethod.POST, defaultResponses())


    }

    private fun defaultResponses(): MutableList<ResponseMessage> {
        val list = java.util.ArrayList<ResponseMessage>()
        list.add(ResponseMessageBuilder().code(500).message("500 message")
                .responseModel(ModelRef("Result")).build())
        list.add(ResponseMessageBuilder().code(401).message("Unauthorized")
                .responseModel(ModelRef("Result")).build())

        return list
    }

    private fun securitySchema(): OAuth {

        val authorizationScopeList = mutableListOf<AuthorizationScope>()
        authorizationScopeList.add(AuthorizationScope("read", "read all"))
        authorizationScopeList.add(AuthorizationScope("trust", "trust all"))
        authorizationScopeList.add(AuthorizationScope("write", "access all"))

        val grantTypes = mutableListOf<GrantType>()
        val creGrant = ResourceOwnerPasswordCredentialsGrant(authLink)

        grantTypes.add(creGrant)

        return OAuth("oauth2schema", authorizationScopeList, grantTypes)

    }

    private fun securityContext(): SecurityContext {
        return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.ant("/user/**"))
                .build()
    }

    private fun defaultAuth(): List<SecurityReference> {

        val authorizationScopes = arrayOfNulls<AuthorizationScope>(3)
        authorizationScopes[0] = AuthorizationScope("read", "read all")
        authorizationScopes[1] = AuthorizationScope("trust", "trust all")
        authorizationScopes[2] = AuthorizationScope("write", "write all")

        return Collections.singletonList(SecurityReference("oauth2schema", authorizationScopes))
    }

    @Bean
    fun securityInfo(): SecurityConfiguration {
        return SecurityConfiguration(clientId, clientSecret, "", "", "", ApiKeyVehicle.HEADER, "", " ")
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder().title("Kotlin springboot").description("")
                .termsOfServiceUrl("https://github.com/maiconhellmann/kotlin-springboot")
                .contact(Contact("Maicon Hellmann", "https://www.linkedin.com/in/maiconhellmann", "maiconhellmann@gmail.com"))
                .license("Open Source").licenseUrl("https://github.com/maiconhellmann/kotlin-springboot").version("1.0.0").build()
    }
}