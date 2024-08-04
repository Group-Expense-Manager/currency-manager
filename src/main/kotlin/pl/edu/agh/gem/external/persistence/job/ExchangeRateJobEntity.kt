package pl.edu.agh.gem.external.persistence.job

import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.job.ExchangeRateJobState
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

@Document("jobs")
data class ExchangeRateJobEntity(
    val id: String,
    val currencyFrom: String,
    val currencyTo: String,
    val exchangeRate: BigDecimal?,
    val state: ExchangeRateJobState,
    val forDate: Instant,
    val nextProcessAt: Instant,
    val retry: Long = 0,
)

fun ExchangeRateJobEntity.toDomain(clock: Clock): ExchangeRateJob {
    return ExchangeRateJob(
        id = id,
        currencyFrom = currencyFrom,
        currencyTo = currencyTo,
        exchangeRate = exchangeRate,
        state = state,
        forDate = LocalDate.ofInstant(forDate, clock.zone),
        nextProcessAt = nextProcessAt,
        retry = retry,
    )
}

fun ExchangeRateJob.toEntity(clock: Clock): ExchangeRateJobEntity {
    return ExchangeRateJobEntity(
        id = id,
        currencyFrom = currencyFrom,
        currencyTo = currencyTo,
        exchangeRate = exchangeRate,
        state = state,
        forDate = forDate.atStartOfDay(clock.zone).toInstant(),
        nextProcessAt = nextProcessAt,
        retry = retry,
    )
}
