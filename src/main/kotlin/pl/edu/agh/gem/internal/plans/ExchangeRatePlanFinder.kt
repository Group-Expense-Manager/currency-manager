package pl.edu.agh.gem.internal.plans

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import pl.edu.agh.gem.config.ExchangeRatePlanProcessorProperties
import pl.edu.agh.gem.internal.model.ExchangeRatePlan
import pl.edu.agh.gem.internal.persistence.ExchangeRatePlanRepository
import java.util.concurrent.Executor

class ExchangeRatePlanFinder(
    private val producerExecutor: Executor,
    private val exchangeRatePlanProcessorProperties: ExchangeRatePlanProcessorProperties,
    private val exchangeRatePlanRepository: ExchangeRatePlanRepository,
) {
    fun findJobToProcess() = flow {
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
            log.error("Error while finding currency exchange job to process", e)
            return null
        }
    }

    private suspend fun waitOnEmpty(exchangeRatePlan: ExchangeRatePlan?) {
        if (exchangeRatePlan == null) {
            log.debug("No exchange rate job to process. Waiting for new job")
            delay(exchangeRatePlanProcessorProperties.emptyCandidateDelay)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
