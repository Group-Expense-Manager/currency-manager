package pl.edu.agh.gem.internal.model

import java.time.Instant

data class ExchangeRatePlan(
    val currencyFrom: String,
    val currencyTo: String,
    val forDate: Instant,
    val nextProcessAt: Instant,
)
