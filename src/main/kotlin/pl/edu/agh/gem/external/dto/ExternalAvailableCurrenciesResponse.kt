package pl.edu.agh.gem.external.dto

import pl.edu.agh.gem.internal.model.Currency

data class ExternalAvailableCurrenciesResponse(
    val currencies: List<ExternalCurrencyDto>,
)

data class ExternalCurrencyDto(
    val code: String,
)

fun List<Currency>.toExternalAvailableCurrenciesResponse() =
    ExternalAvailableCurrenciesResponse(
        currencies = map { ExternalCurrencyDto(it.code) },
    )
