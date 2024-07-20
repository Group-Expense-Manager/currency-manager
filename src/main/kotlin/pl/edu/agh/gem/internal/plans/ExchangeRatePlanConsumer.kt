package pl.edu.agh.gem.internal.plans

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import pl.edu.agh.gem.internal.model.ExchangeRatePlan

class ExchangeRatePlanConsumer(
    private val exchangeRatePlanFinder: ExchangeRatePlanFinder,
    private val exchangeRatePlanProcessor: ExchangeRatePlanProcessor,
) {
    
    private var job: Job? = null
    
    fun consume(scope: CoroutineScope) {
        job = scope.launch { 
            exchangeRatePlanFinder.findJobToProcess()
                .collect { exchangeRatePlan ->
                    processWithExceptionHandling(exchangeRatePlan)
                }
        }
    }
    
    private fun processWithExceptionHandling(exchangeRatePlan: ExchangeRatePlan) {
        try {
            exchangeRatePlanProcessor.process(exchangeRatePlan)
        } catch (e: Exception) {
            log.error(e) { "Error while processing exchange rate plan: $exchangeRatePlan" }
        }
    }
    
    fun destroy() {
        job?.let { 
            log.info { "Cancelling exchange rate plan consumer job" }
            it.cancel()
        }
    }
    
    companion object {
        private val log = KotlinLogging.logger {}
    }
}
