package pl.edu.agh.gem.external.dto

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.internal.model.ExchangeRate
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class ExternalExchangeRateResponseTest : ShouldSpec({

    should("correctly map ExchangeRate to ExternalExchangeRateResponse") {
        // given
        val exchangeRate = ExchangeRate(
            currencyFrom = "USD",
            currencyTo = "EUR",
            exchangeRate = BigDecimal("1.2"),
            createdAt = Instant.parse("2023-01-01T00:00:00.00Z"),
            forDate = LocalDate.parse("2023-01-01"),
            validTo = LocalDate.parse("2023-01-01"),
        )

        // when
        val externalExchangeRateResponse = exchangeRate.toExternalExchangeRateResponse()

        // then
        externalExchangeRateResponse.also {
            it.currencyFrom shouldBe exchangeRate.currencyFrom
            it.currencyTo shouldBe exchangeRate.currencyTo
            it.rate shouldBe exchangeRate.exchangeRate
            it.createdAt shouldBe exchangeRate.createdAt
            it.forDate shouldBe exchangeRate.forDate
            it.validTo shouldBe exchangeRate.validTo
        }
    }
},)
