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
    val rates: List<NBPExchangeRate>,
)

data class NBPExchangeRate(
    val no: String,
    val effectiveDate: LocalDate,
    val mid: BigDecimal,
)

fun NBPExchangeResponse.toExchangeRate(clock: Clock, validDuration: Duration) = ExchangeRate(
    currencyFrom = code,
    currencyTo = "PLN",
    exchangeRate = rates.first().mid,
    createdAt = clock.instant(),
    forDate = rates.first().effectiveDate,
    validTo = rates.first().effectiveDate.plusDays(validDuration.toDays()),
)
