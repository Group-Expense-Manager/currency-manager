package pl.edu.agh.gem.internal.model

import pl.edu.agh.gem.internal.job.ExchangeRateJobState
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.STARTING
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID.randomUUID

data class ExchangeRateJob(
    val id: String = randomUUID().toString(),
    val currencyFrom: String,
    val currencyTo: String,
    val exchangeRate: BigDecimal? = null,
    val state: ExchangeRateJobState = STARTING,
    val forDate: LocalDate,
    val nextProcessAt: Instant,
    val retry: Long = 0,
)

fun ExchangeRateJob.addExchangeRate(exchangeRate: BigDecimal): ExchangeRateJob {
    return this.copy(
        exchangeRate = exchangeRate,
    )
}
