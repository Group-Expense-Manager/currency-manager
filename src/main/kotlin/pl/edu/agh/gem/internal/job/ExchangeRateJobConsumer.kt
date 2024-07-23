package pl.edu.agh.gem.internal.job

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import mu.KotlinLogging
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import java.util.concurrent.Executor

class ExchangeRateJobConsumer(
    private val exchangeRateJobFinder: ExchangeRateJobFinder,
    private val exchangeRateJobProcessor: ExchangeRateJobProcessor,
) {

    private var job: Job? = null

    fun consume(consumerExecutor: Executor) {
        job = CoroutineScope(consumerExecutor.asCoroutineDispatcher()).launch {
            exchangeRateJobFinder.findJobToProcess()
                .collect { exchangeRateJob ->
                    processWithExceptionHandling(exchangeRateJob)
                }
        }
    }

    private fun processWithExceptionHandling(exchangeRateJob: ExchangeRateJob) {
        try {
            exchangeRateJobProcessor.processExchangeRateJob(exchangeRateJob)
        } catch (e: Exception) {
            log.error(e) { "Error while processing exchange rate job: $exchangeRateJob" }
        }
    }

    fun destroy() {
        job?.also {
            log.info { "Cancelling exchange rate job consumer job" }
            it.cancel()
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
