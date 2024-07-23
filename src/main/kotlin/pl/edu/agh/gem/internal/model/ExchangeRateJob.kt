package pl.edu.agh.gem.internal.model

import pl.edu.agh.gem.internal.job.ExchangeRateJobState
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.STARTING
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID.randomUUID

data class ExchangeRateJob(
    val id: String = randomUUID().toString(),
    val currencyFrom: String,
    val currencyTo: String,
    val exchangeRate: BigDecimal? = null,
    val state: ExchangeRateJobState = STARTING,
    val forDate: Instant,
    val nextProcessAt: Instant = Instant.now(),
    val retry: Long = 0,
)

fun ExchangeRateJob.addExchangeRate(exchangeRate: BigDecimal): ExchangeRateJob {
    return this.copy(
        exchangeRate = exchangeRate,
    )
}
