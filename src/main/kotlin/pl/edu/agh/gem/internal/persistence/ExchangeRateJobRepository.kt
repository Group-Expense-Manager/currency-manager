package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.ExchangeRateJob
import java.time.Duration
import java.time.Instant

interface ExchangeRateJobRepository {
    fun save(exchangeRateJob: ExchangeRateJob): ExchangeRateJob
    fun findJobToProcessAndLock(): ExchangeRateJob?
    fun updateNextProcessAtAndRetry(exchangeRateJob: ExchangeRateJob): ExchangeRateJob
    fun remove(exchangeRateJob: ExchangeRateJob)
}
