package pl.edu.agh.gem.external.persistence.job

import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.job.ExchangeRateJobState
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import java.time.Instant

@Document("Jobs")
data class ExchangeRateJobEntity (
    val id:String,
    val currencyFrom: String,
    val currencyTo: String,
    val exchangeRate: String?,
    val state: ExchangeRateJobState,
    val createdAt: Instant = Instant.now(),
    val nextProcessAt: Instant = Instant.now(),
    val retry:Long = 0,
)

fun ExchangeRateJobEntity.toDomain(): ExchangeRateJob {
    return ExchangeRateJob(
        id = id,
        currencyFrom = currencyFrom,
        currencyTo = currencyTo,
        exchangeRate = exchangeRate,
        state = state,
        createdAt = createdAt,
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
        createdAt = createdAt,
        nextProcessAt = nextProcessAt,
        retry = retry,
    )
}

