package pl.edu.agh.gem.integration.controler

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.assertion.shouldBody
import pl.edu.agh.gem.assertion.shouldHaveErrors
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.external.dto.ExternalAvailableCurrenciesResponse
import pl.edu.agh.gem.external.dto.ExternalExchangeRateResponse
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.internal.persistence.MissingExchangeRateException
import pl.edu.agh.gem.util.createExchangeRate

class ExternalCurrencyControllerIT(
    private val service: ServiceTestClient,
) : BaseIntegrationSpec({
    should("get available currencies successfully") {
        // when
        val response = service.getExternalAvailableCurrencies()

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<ExternalAvailableCurrenciesResponse> {
            currencies.map { it.code }.toSet() shouldContainExactly setOf("USD", "EUR", "PLN")
        }
    }

    should("get exchange rate successfully") {
        // when
        val exchangeRate = createExchangeRate(
            currencyTo = "PLN",
            currencyFrom = "USD",
            rate = "3.75",
        )
        val response = service.getExternalExchangeRate(
            currencyTo = exchangeRate.currencyTo,
            currencyFrom = exchangeRate.currencyFrom,
            date = exchangeRate.createdAt,
        )

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<ExternalExchangeRateResponse> {
            currencyFrom shouldBe exchangeRate.currencyFrom
            currencyTo shouldBe exchangeRate.currencyTo
            rate shouldBe exchangeRate.exchangeRate
            createdAt shouldBe exchangeRate.createdAt
        }
    }

    should("get exchange rate successfully when date is null") {
        // when
        val exchangeRate = createExchangeRate(
            currencyTo = "PLN",
            currencyFrom = "USD",
            rate = "3.75",
        )
        val response = service.getExternalExchangeRate(
            currencyTo = exchangeRate.currencyTo,
            currencyFrom = exchangeRate.currencyFrom,
            date = null,
        )

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<ExternalExchangeRateResponse> {
            currencyFrom shouldBe exchangeRate.currencyFrom
            currencyTo shouldBe exchangeRate.currencyTo
            rate shouldBe exchangeRate.exchangeRate
            createdAt.shouldNotBeNull()
        }
    }

    should("response with NOT_FOUND when exchange rate not found") {
        // when
        val exchangeRate = createExchangeRate(
            currencyTo = "PLN",
            currencyFrom = "CZK",
            rate = "1.0",
        )
        val response = service.getExternalExchangeRate(
            currencyTo = exchangeRate.currencyTo,
            currencyFrom = exchangeRate.currencyFrom,
            date = null,
        )

        // then
        response shouldHaveHttpStatus NOT_FOUND
        response shouldHaveErrors {
            errors shouldHaveSize 1
            errors.first().code shouldBe MissingExchangeRateException::class.simpleName
        }
    }
},)
