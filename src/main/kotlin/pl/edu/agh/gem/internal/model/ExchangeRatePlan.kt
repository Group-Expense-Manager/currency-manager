package pl.edu.agh.gem.internal.model

import java.time.Instant
import java.time.LocalDate

data class ExchangeRatePlan(
    val currencyFrom: String,
    val currencyTo: String,
    val forDate: LocalDate,
    val nextProcessAt: Instant,
)
