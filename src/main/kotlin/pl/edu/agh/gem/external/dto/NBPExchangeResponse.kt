package pl.edu.agh.gem.external.dto

import pl.edu.agh.gem.internal.model.ExchangeRate
import java.math.BigDecimal
import java.time.Clock
import java.time.Duration
import java.time.LocalDate

data class NBPExchangeResponse(
    val table: String,
    val currency: String,
    val code: String,
    val rates: List<NBPExchangeRate>
)

data class NBPExchangeRate(
    val no: String,
    val effectiveDate: LocalDate,
    val bid: Double,
    val ask: Double
)

fun NBPExchangeResponse.toExchangeRate(clock: Clock,validDuration: Duration) = ExchangeRate(
    currencyFrom = code,
    currencyTo = "PLN",
    exchangeRate = BigDecimal(rates.first().bid).plus(BigDecimal(rates.first().ask)).divide(BigDecimal(2)),
    createdAt = rates.first().effectiveDate.atStartOfDay(clock.zone).toInstant(),
    validTo = rates.first().effectiveDate.plus(validDuration).atStartOfDay(clock.zone).toInstant()
)
