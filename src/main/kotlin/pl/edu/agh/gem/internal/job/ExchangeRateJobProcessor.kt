package pl.edu.agh.gem.internal.job

import mu.KotlinLogging
import org.springframework.stereotype.Service
import pl.edu.agh.gem.config.ExchangeRateJobProcessorProperties
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.persistence.ExchangeRateJobRepository

@Service
class ExchangeRateJobProcessor(
    private val currencyExchangeJobSelector: CurrencyExchangeJobSelector,
    private val currencyExchangeJobRepository: ExchangeRateJobRepository,
) {
    fun processExchangeRateJob(exchangeRateJob: ExchangeRateJob) {
        while ( true ) {
            when(val nextState = currencyExchangeJobSelector.select(exchangeRateJob.state).process(exchangeRateJob)) {
                is NextStage -> handleNextStage(nextState)
                is StageSuccess -> return handleStateSuccess(exchangeRateJob)
                is StageFailure -> return handleStateFailure(exchangeRateJob)
                is StageRetry -> return handleStateRetry(exchangeRateJob)
            }
            }
        }
    private fun handleStateSuccess(exchangeRateJob: ExchangeRateJob) {
        log.info { "Success on processed state: ${exchangeRateJob.state} for ${exchangeRateJob.currencyFrom} -> ${exchangeRateJob.currencyTo}" }
        currencyExchangeJobRepository.remove(exchangeRateJob)
    }

    private fun handleStateFailure(exchangeRateJob: ExchangeRateJob) {
        log.error { "Failure occurred on ${exchangeRateJob.currencyFrom} -> ${exchangeRateJob.currencyTo}" }
        currencyExchangeJobRepository.remove(exchangeRateJob)
    }

    private fun handleStateRetry(exchangeRateJob: ExchangeRateJob) {
        log.warn { "Retry for ${exchangeRateJob.currencyFrom} -> ${exchangeRateJob.currencyTo}" }
        currencyExchangeJobRepository.updateNextProcessAtAndRetry(exchangeRateJob)
    }
    
    private fun handleNextStage(nextStage: NextStage){
        currencyExchangeJobRepository.save(nextStage.exchangeRateJob.copy(
            state = nextStage.newState
        ))
    }
    
    companion object {
        private val log = KotlinLogging.logger {}
    }
}
