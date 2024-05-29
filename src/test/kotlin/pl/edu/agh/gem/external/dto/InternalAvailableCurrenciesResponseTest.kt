package pl.edu.agh.gem.external.dto

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import pl.edu.agh.gem.internal.model.Currency

class InternalAvailableCurrenciesResponseTest : ShouldSpec({

    should("correctly map List<Currency> to AvailableCurrenciesResponse") {
        // given
        val currencies = listOf(
            Currency(code = "USD"),
            Currency(code = "EUR"),
            Currency(code = "JPY"),
        )

        // when
        val availableCurrenciesResponse = currencies.toInternalAvailableCurrenciesResponse()

        // then
        availableCurrenciesResponse.also {
            it.currencies.map { currencyDto -> currencyDto.code } shouldContainExactly listOf("USD", "EUR", "JPY")
        }
    }

    should("return an empty list when List<Currency> is empty") {
        // given
        val currencies = listOf<Currency>()

        // when
        val availableCurrenciesResponse = currencies.toInternalAvailableCurrenciesResponse()

        // then
        availableCurrenciesResponse.currencies.shouldBeEmpty()
    }
},)
