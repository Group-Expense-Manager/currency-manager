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
import java.time.LocalDate

@Component
@Lazy
class ServiceTestClient(applicationContext: WebApplicationContext) {
    private val webClient =
        bindToApplicationContext(applicationContext)
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

    fun getInternalExchangeRate(
        currencyFrom: String,
        currencyTo: String,
        date: LocalDate?,
    ): ResponseSpec {
        return webClient.get()
            .uri(getExchangeRateUri(EXTERNAL, currencyFrom, currencyTo, date))
            .headers { it.withAppAcceptType() }
            .exchange()
    }

    fun getExternalExchangeRate(
        currencyFrom: String,
        currencyTo: String,
        date: LocalDate?,
    ): ResponseSpec {
        return webClient.get()
            .uri(getExchangeRateUri(INTERNAL, currencyFrom, currencyTo, date))
            .headers { it.withAppAcceptType() }
            .exchange()
    }

    private fun getExchangeRateUri(
        type: String,
        currencyFrom: String,
        currencyTo: String,
        date: LocalDate?,
    ): URI {
        return date?.let {
            URI("$type/currencies/from/$currencyFrom/to/$currencyTo/?date=$date")
        } ?: URI("$type/currencies/from/$currencyFrom/to/$currencyTo/")
    }
}
