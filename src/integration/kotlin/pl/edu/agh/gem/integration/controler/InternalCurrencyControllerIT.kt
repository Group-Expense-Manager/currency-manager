package pl.edu.agh.gem.integration.controler

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.assertion.shouldBody
import pl.edu.agh.gem.assertion.shouldHaveErrors
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.external.dto.InternalAvailableCurrenciesResponse
import pl.edu.agh.gem.external.dto.InternalExchangeRateResponse
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.internal.persistence.ExchangeRateRepository
import pl.edu.agh.gem.internal.persistence.MissingExchangeRateException
import pl.edu.agh.gem.util.createExchangeRate
import java.time.Clock
import java.time.temporal.ChronoUnit.DAYS

class InternalCurrencyControllerIT(
    private val service: ServiceTestClient,
    private val exchangeRateRepository: ExchangeRateRepository,
) : BaseIntegrationSpec({

    should("get available currencies successfully") {
        // when
        val response = service.getInternalAvailableCurrencies()

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<InternalAvailableCurrenciesResponse> {
            currencies.map { it.code }.toSet() shouldContainExactly setOf("USD", "EUR", "PLN")
        }
    }

    should("get exchange rate successfully") {
        // given
        val exchangeRate = createExchangeRate(
            currencyTo = "PLN",
            currencyFrom = "USD",
            rate = "3.75".toBigDecimal(),
            forDate = FIXED_TIME,
            createdAt = FIXED_TIME,
            validTo = FIXED_TIME.plus(1, DAYS),
        )
        exchangeRateRepository.save(exchangeRate)

        // when
        val response = service.getInternalExchangeRate(
            currencyTo = exchangeRate.currencyTo,
            currencyFrom = exchangeRate.currencyFrom,
            date = exchangeRate.forDate,
        )

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<InternalExchangeRateResponse> {
            currencyFrom shouldBe exchangeRate.currencyFrom
            currencyTo shouldBe exchangeRate.currencyTo
            rate shouldBe exchangeRate.exchangeRate
            createdAt shouldBe exchangeRate.createdAt
        }
    }

    should("get exchange rate successfully when date is null") {
        // given
        val exchangeRate = createExchangeRate(
            currencyTo = "PLN",
            currencyFrom = "USD",
            rate = "3.75".toBigDecimal(),
            forDate = FIXED_TIME,
            createdAt = FIXED_TIME,
            validTo = FIXED_TIME.plus(1, DAYS),
        )
        exchangeRateRepository.save(exchangeRate)

        // when
        val response = service.getInternalExchangeRate(
            currencyTo = exchangeRate.currencyTo,
            currencyFrom = exchangeRate.currencyFrom,
            date = null,
        )

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<InternalExchangeRateResponse> {
            currencyFrom shouldBe exchangeRate.currencyFrom
            currencyTo shouldBe exchangeRate.currencyTo
            rate shouldBe exchangeRate.exchangeRate
            createdAt.shouldNotBeNull()
        }
    }

    should("response with NOT_FOUND when exchange rate not found") {
        // given
        val exchangeRate = createExchangeRate(
            currencyTo = "PLN",
            currencyFrom = "USD",
            rate = "3.75".toBigDecimal(),
            forDate = FIXED_TIME,
            createdAt = FIXED_TIME,
            validTo = FIXED_TIME.plus(1, DAYS),
        )

        // when
        val response = service.getInternalExchangeRate(
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
