package pl.edu.agh.gem.internal.model

import java.math.BigDecimal
import java.time.Instant

data class ExchangeRate(
    val currencyFrom: String,
    val currencyTo: String,
    val exchangeRate: BigDecimal,
    val createdAt: Instant,
    val forDate: Instant,
    val validTo: Instant,
)
