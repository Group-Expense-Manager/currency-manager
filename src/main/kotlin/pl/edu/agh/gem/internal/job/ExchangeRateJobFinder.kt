package pl.edu.agh.gem.internal.job

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.time.delay
import pl.edu.agh.gem.config.ExchangeRateJobProcessorProperties
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.persistence.ExchangeRateJobRepository
import java.util.concurrent.Executor

class ExchangeRateJobFinder(
    private val producerExecutor: Executor,
    private val exchangeRateJobProcessorProperties: ExchangeRateJobProcessorProperties,
    private val exchangeRateJobRepository: ExchangeRateJobRepository,
) {
    fun findJobToProcess() =
        flow {
            while (currentCoroutineContext().isActive) {
                val exchangeRateJob = findExchangeRateJob()
                exchangeRateJob?.let {
                    emit(it)
                    log.info { "Emitted exchange rate job : $it" }
                }
                waitOnEmpty(exchangeRateJob)
            }
        }.flowOn(producerExecutor.asCoroutineDispatcher())

    private fun findExchangeRateJob(): ExchangeRateJob? {
        try {
            return exchangeRateJobRepository.findJobToProcessAndLock()
        } catch (e: Exception) {
            log.error(e) { "Error while finding currency exchange job to process" }
            return null
        }
    }

    private suspend fun waitOnEmpty(exchangeRateJob: ExchangeRateJob?) {
        if (exchangeRateJob == null) {
            log.info { "No exchange rate job to process. Waiting for new job" }
            delay(exchangeRateJobProcessorProperties.emptyCandidateDelay)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
