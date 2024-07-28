package pl.edu.agh.gem.external.persistence.job

import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.job.ExchangeRateJobState
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import java.math.BigDecimal
import java.time.Instant

@Document("Jobs")
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

fun ExchangeRateJobEntity.toDomain(): ExchangeRateJob {
    return ExchangeRateJob(
        id = id,
        currencyFrom = currencyFrom,
        currencyTo = currencyTo,
        exchangeRate = exchangeRate,
        state = state,
        forDate = forDate,
        nextProcessAt = nextProcessAt,
        retry = retry,
    )
}

fun ExchangeRateJob.toEntity(): ExchangeRateJobEntity {
    return ExchangeRateJobEntity(
        id = id,
        currencyFrom = currencyFrom,
        currencyTo = currencyTo,
        exchangeRate = exchangeRate,
        state = state,
        forDate = forDate,
        nextProcessAt = nextProcessAt,
        retry = retry,
    )
}
