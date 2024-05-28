package pl.edu.agh.gem.external.dto

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.internal.model.ExchangeRate
import java.math.BigDecimal
import java.time.Instant

class ExternalExchangeRateResponseTest : ShouldSpec({

    should("correctly map ExchangeRate to ExternalExchangeRateResponse") {
        // given
        val exchangeRate = ExchangeRate(
            currencyFrom = "USD",
            currencyTo = "EUR",
            rate = BigDecimal("1.2"),
            createAt = Instant.parse("2023-01-01T00:00:00.00Z"),
        )

        // when
        val externalExchangeRateResponse = exchangeRate.toExternalExchangeRateResponse()

        // then
        externalExchangeRateResponse.also {
            it.currencyFrom shouldBe exchangeRate.currencyFrom
            it.currencyTo shouldBe exchangeRate.currencyTo
            it.rate shouldBe exchangeRate.rate
            it.createAt shouldBe exchangeRate.createAt
        }
    }
},)
