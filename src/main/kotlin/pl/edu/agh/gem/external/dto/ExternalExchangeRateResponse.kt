package pl.edu.agh.gem.external.dto

import pl.edu.agh.gem.internal.model.ExchangeRate
import java.math.BigDecimal
import java.time.Instant

data class ExternalExchangeRateResponse(
    val currencyFrom: String,
    val currencyTo: String,
    val rate: BigDecimal,
    val createAt: Instant,
)

fun ExchangeRate.toExternalExchangeRateResponse() = ExternalExchangeRateResponse(
    currencyFrom = currencyFrom,
    currencyTo = currencyTo,
    rate = rate,
    createAt = createAt,
)
