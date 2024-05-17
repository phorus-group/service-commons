package group.phorus.service.commons.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME

/**
 * Default properties.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
open class PostProcessorConfig : EnvironmentPostProcessor {
    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication?
    ) {
        environment.propertySources.addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
            MapPropertySource("prefixed", mapOf(
                "server.shutdown" to "graceful",
                "spring.jackson.default-property-inclusion" to "NON_NULL",
            ))
        );
    }
}