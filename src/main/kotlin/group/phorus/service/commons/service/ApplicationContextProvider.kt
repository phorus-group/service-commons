package group.phorus.service.commons.service

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.ApplicationContext

@AutoConfiguration
class ApplicationContextProvider(
    val applicationContext: ApplicationContext,
)