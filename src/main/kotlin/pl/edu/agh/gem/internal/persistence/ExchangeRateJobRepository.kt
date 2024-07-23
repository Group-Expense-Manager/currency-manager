package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.ExchangeRateJob

interface ExchangeRateJobRepository {
    fun save(exchangeRateJob: ExchangeRateJob): ExchangeRateJob
    fun findJobToProcessAndLock(): ExchangeRateJob?
    fun updateNextProcessAtAndRetry(exchangeRateJob: ExchangeRateJob): ExchangeRateJob
    fun remove(exchangeRateJob: ExchangeRateJob)
}

class MissingExchangeRateJobException : RuntimeException("No exchange rate job found")
