package pl.edu.agh.gem.internal.model

import java.math.BigDecimal
import java.time.Instant

data class ExchangeRate(
    val currencyFrom: String,
    val currencyTo: String,
    val rate: BigDecimal,
    val createAt: Instant,
)
