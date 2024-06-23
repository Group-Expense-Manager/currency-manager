package pl.edu.agh.gem.internal.service

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.config.AvailableCurrenciesProperties
import pl.edu.agh.gem.config.ExchangeRatesProperties
import pl.edu.agh.gem.internal.model.Currency

class CurrencyServiceTest : ShouldSpec({

    val availableCurrenciesProperties = mock<AvailableCurrenciesProperties>()
    val exchangeRatesProperties = mock<ExchangeRatesProperties>()
    val currencyService = CurrencyService(availableCurrenciesProperties, exchangeRatesProperties)

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
},)
