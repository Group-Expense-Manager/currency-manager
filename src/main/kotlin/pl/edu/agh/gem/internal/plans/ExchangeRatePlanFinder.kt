package pl.edu.agh.gem.internal.plans

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.time.delay
import pl.edu.agh.gem.config.ExchangeRatePlanProcessorProperties
import pl.edu.agh.gem.internal.model.ExchangeRatePlan
import pl.edu.agh.gem.internal.persistence.ExchangeRatePlanRepository
import java.util.concurrent.Executor

class ExchangeRatePlanFinder(
    private val producerExecutor: Executor,
    private val exchangeRatePlanProcessorProperties: ExchangeRatePlanProcessorProperties,
    private val exchangeRatePlanRepository: ExchangeRatePlanRepository,
) {
    fun findJobToProcess() =
        flow {
            while (currentCoroutineContext().isActive) {
                val exchangeRatePlan = findExchangeRateJob()
                exchangeRatePlan?.let {
                    emit(it)
                    log.info { "Emitted exchange rate plan : $it" }
                }
                waitOnEmpty(exchangeRatePlan)
            }
        }.flowOn(producerExecutor.asCoroutineDispatcher())

    private fun findExchangeRateJob(): ExchangeRatePlan? {
        try {
            return exchangeRatePlanRepository.findReadyAndDelay()
        } catch (e: Exception) {
            log.error(e) { "Error while finding currency exchange job to process" }
            return null
        }
    }

    private suspend fun waitOnEmpty(exchangeRatePlan: ExchangeRatePlan?) {
        if (exchangeRatePlan == null) {
            log.info { "No exchange rate job to process. Waiting for new job" }
            delay(exchangeRatePlanProcessorProperties.emptyCandidateDelay)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
