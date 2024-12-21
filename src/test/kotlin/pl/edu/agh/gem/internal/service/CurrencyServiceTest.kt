package pl.edu.agh.gem.internal.service

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.config.AvailableCurrenciesProperties
import pl.edu.agh.gem.internal.model.Currency
import pl.edu.agh.gem.internal.persistence.ExchangeRateRepository
import pl.edu.agh.gem.util.createExchangeRate

class CurrencyServiceTest : ShouldSpec({

    val availableCurrenciesProperties = mock<AvailableCurrenciesProperties>()
    val exchangeRateRepository = mock<ExchangeRateRepository>()
    val currencyService = CurrencyService(availableCurrenciesProperties, exchangeRateRepository)

    should("return the list of available currencies") {
        // given
        val currencyCodes = listOf("USD", "EUR", "JPY")
        whenever(availableCurrenciesProperties.codes).thenReturn(currencyCodes)

        // when
        val result = currencyService.getAvailableCurrencies()

        // then
        result shouldHaveSize 3
        result shouldContainExactly currencyCodes.map { Currency(it) }
    }

    should("return the exchange rate for given currencies and date") {
        // given
        val exchangeRate = createExchangeRate()
        whenever(
            exchangeRateRepository.getExchangeRate(
                exchangeRate.currencyFrom,
                exchangeRate.currencyTo,
                exchangeRate.forDate,
            ),
        ).thenReturn(exchangeRate)

        // when
        val result =
            currencyService.getExchangeRate(
                exchangeRate.currencyFrom,
                exchangeRate.currencyTo,
                exchangeRate.forDate,
            )

        // then
        result shouldBe exchangeRate
    }
})
