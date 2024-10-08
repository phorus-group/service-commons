package group.phorus.service.commons.bdd

import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [TestApp::class])
@CucumberContextConfiguration
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CucumberSpringConfiguration

@SpringBootApplication(scanBasePackages = ["group.phorus"])
class TestApp