package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.ExchangeRateJob

interface ExchangeRateJobRepository {
    fun save(exchangeRateJob: ExchangeRateJob): ExchangeRateJob

    fun findJobToProcessAndLock(): ExchangeRateJob?

    fun updateNextProcessAtAndRetry(exchangeRateJob: ExchangeRateJob): ExchangeRateJob?

    fun remove(exchangeRateJob: ExchangeRateJob)

    fun findById(id: String): ExchangeRateJob?
}

class MissingExchangeRateJobException(exchangeRateJob: ExchangeRateJob) : RuntimeException(
    "No exchange rate job found, $exchangeRateJob",
)
