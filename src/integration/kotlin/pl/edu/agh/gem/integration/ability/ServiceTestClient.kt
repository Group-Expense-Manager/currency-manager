package pl.edu.agh.gem.integration.ability

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.servlet.client.MockMvcWebTestClient.bindToApplicationContext
import org.springframework.web.context.WebApplicationContext
import pl.edu.agh.gem.headers.HeadersUtils.withAppAcceptType
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.paths.Paths.INTERNAL
import java.net.URI

@Component
@Lazy
class ServiceTestClient(applicationContext: WebApplicationContext) {
    private val webClient = bindToApplicationContext(applicationContext)
        .configureClient()
        .build()

    fun getInternalAvailableCurrencies(): ResponseSpec {
        return webClient.get()
            .uri(URI("$INTERNAL/currencies"))
            .headers { it.withAppAcceptType() }
            .exchange()
    }

    fun getExternalAvailableCurrencies(): ResponseSpec {
        return webClient.get()
            .uri(URI("$EXTERNAL/currencies"))
            .headers { it.withAppAcceptType() }
            .exchange()
    }
}
