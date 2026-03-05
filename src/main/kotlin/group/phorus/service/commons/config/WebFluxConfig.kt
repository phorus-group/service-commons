package group.phorus.service.commons.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.webflux.autoconfigure.WebFluxRegistrations
import org.springframework.context.annotation.Bean
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.reactive.result.method.RequestMappingInfo
import java.lang.reflect.Method

@AutoConfiguration
class WebFluxConfig : WebFluxConfigurer {
    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(ReactivePageableHandlerMethodArgumentResolver())
    }

    @Bean
    fun webFluxRegistrations(): WebFluxRegistrations = object : WebFluxRegistrations {
        override fun getRequestMappingHandlerMapping(): RequestMappingHandlerMapping =
            object : RequestMappingHandlerMapping() {
                override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? {
                    if (method.isBridge || method.isSynthetic) return null
                    return super.getMappingForMethod(method, handlerType)
                }
            }
    }
}
