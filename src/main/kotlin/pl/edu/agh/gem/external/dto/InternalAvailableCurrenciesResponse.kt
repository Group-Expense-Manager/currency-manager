package pl.edu.agh.gem.external.dto

import pl.edu.agh.gem.internal.model.Currency

data class InternalAvailableCurrenciesResponse(
    val currencies: List<InternalCurrencyDto>,
)

data class InternalCurrencyDto(
    val code: String,
)

fun List<Currency>.toInternalAvailableCurrenciesResponse() = InternalAvailableCurrenciesResponse(
    currencies = map { InternalCurrencyDto(it.code) },
)
