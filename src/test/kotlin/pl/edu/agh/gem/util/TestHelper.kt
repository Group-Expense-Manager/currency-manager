package pl.edu.agh.gem.util

import pl.edu.agh.gem.internal.model.ExchangeRate
import java.time.Instant
import java.time.Instant.parse

fun createExchangeRate(
    currencyFrom: String = "USD",
    currencyTo: String = "EUR",
    rate: String = "1.0",
    createAt: Instant = parse("2023-01-01T00:00:00.00Z"),
) = ExchangeRate(
    currencyFrom = currencyFrom,
    currencyTo = currencyTo,
    rate = rate.toBigDecimal(),
    createAt = createAt,
)
