package com.github.maiconhellmann.demo.config.swagger

import com.google.common.base.Predicates
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2


@Configuration
@EnableSwagger2
class SwaggerConfig {

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
//                .paths(PathSelectors.any())
//                .paths(PathSelectors.ant("/article"))
                .paths(Predicates.or(
                        PathSelectors.ant("/oauth/token/**"),
                        PathSelectors.ant("/article/**"),
                        PathSelectors.ant("/user/**")
                ))
                .build()
    }
}