package pl.edu.agh.gem.util

import pl.edu.agh.gem.internal.job.ExchangeRateJobState
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.STARTING
import pl.edu.agh.gem.internal.model.ExchangeRate
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import java.math.BigDecimal
import java.time.Instant
import java.time.Instant.parse

fun createExchangeRate(
    currencyFrom: String = "USD",
    currencyTo: String = "EUR",
    rate: String = "1.0",
    createdAt: Instant = parse("2023-01-01T00:00:00.00Z"),
    forDate: Instant = parse("2023-01-01T00:00:00.00Z"),
    validTo: Instant = parse("2023-01-02T00:00:00.00Z"),
) = ExchangeRate(
    currencyFrom = currencyFrom,
    currencyTo = currencyTo,
    exchangeRate = rate.toBigDecimal(),
    createdAt = createdAt,
    forDate = forDate,
    validTo = validTo,
)

fun createExchangeRateJob(
    id: String = "exchange-rate-job-id",
    currencyFrom: String = "USD",
    currencyTo: String = "EUR",
    rate: BigDecimal? = null,
    state: ExchangeRateJobState = STARTING,
    forDate: Instant = parse("2023-01-01T00:00:00.00Z"),
    nextProcessAt: Instant = parse("2023-01-01T00:00:00.00Z"),
    retry: Long = 0,
) = ExchangeRateJob(
    id = id,
    currencyFrom = currencyFrom,
    currencyTo = currencyTo,
    exchangeRate = rate,
    state = state,
    forDate = forDate,
    nextProcessAt = nextProcessAt,
    retry = retry,
)
