package pl.edu.agh.gem.util

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import pl.edu.agh.gem.external.dto.NBPExchangeRate
import pl.edu.agh.gem.external.dto.NBPExchangeResponse
import pl.edu.agh.gem.internal.job.ExchangeRateJobState
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.STARTING
import pl.edu.agh.gem.internal.model.ExchangeRate
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.model.ExchangeRatePlan
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

fun objectMapper() =
    jacksonObjectMapper().registerModules(JavaTimeModule())

fun createExchangeRate(
    currencyFrom: String = "USD",
    currencyTo: String = "EUR",
    rate: BigDecimal = "1.0".toBigDecimal(),
    createdAt: Instant = Instant.parse("2023-01-01T00:00:00.00Z"),
    forDate: LocalDate = LocalDate.parse("2023-01-01"),
    validTo: LocalDate = LocalDate.parse("2023-01-01"),
) = ExchangeRate(
    currencyFrom = currencyFrom,
    currencyTo = currencyTo,
    exchangeRate = rate,
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
    forDate: LocalDate = LocalDate.parse("2023-01-01"),
    nextProcessAt: Instant = Instant.parse("2023-01-01T00:00:00.00Z"),
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

fun createExchangeRatePlan(
    currencyFrom: String = "USD",
    currencyTo: String = "EUR",
    forDate: LocalDate = LocalDate.parse("2023-01-01"),
    nextProcessAt: Instant = Instant.parse("2023-01-01T00:00:00.00Z"),
) = ExchangeRatePlan(
    currencyFrom = currencyFrom,
    currencyTo = currencyTo,
    forDate = forDate,
    nextProcessAt = nextProcessAt,
)

fun createNBPExchangeResponse(
    table: String = "A",
    currency: String = "USD",
    code: String = "USD",
    rates: List<NBPExchangeRate> = listOf(createNBPExchangeRate()),
) = NBPExchangeResponse(
    table = table,
    currency = currency,
    code = code,
    rates = rates,
)

fun createNBPExchangeRate(
    no: String = "001/A/NBP/2024",
    effectiveDate: LocalDate = LocalDate.parse("2024-01-01"),
    mid: BigDecimal = "1.0".toBigDecimal(),
) = NBPExchangeRate(
    no = no,
    effectiveDate = effectiveDate,
    mid = mid,
)
