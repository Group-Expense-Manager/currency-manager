package pl.edu.agh.gem.external.dto

import pl.edu.agh.gem.internal.model.ExchangeRate
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class ExternalExchangeRateResponse(
    val currencyFrom: String,
    val currencyTo: String,
    val rate: BigDecimal,
    val createdAt: Instant,
    val forDate: LocalDate,
    val validTo: LocalDate,
)

fun ExchangeRate.toExternalExchangeRateResponse() = ExternalExchangeRateResponse(
    currencyFrom = currencyFrom,
    currencyTo = currencyTo,
    rate = exchangeRate,
    createdAt = createdAt,
    forDate = forDate,
    validTo = validTo,
)
