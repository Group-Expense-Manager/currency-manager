package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.EXCHANGE_RATE
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.POLISH_EXCHANGE_RATE
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.REVERSE_POLISH_EXCHANGE_RATE
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.ExchangeRateJob

@Component
class StartingStage : ProcessingStage() {
    override fun process(exchangeRateJob: ExchangeRateJob): StageResult {
        return when {
            exchangeRateJob.currencyFrom == "PLN" -> nextStage(exchangeRateJob, REVERSE_POLISH_EXCHANGE_RATE)
            exchangeRateJob.currencyTo == "PLN" -> nextStage(exchangeRateJob, POLISH_EXCHANGE_RATE)
            else -> nextStage(exchangeRateJob, EXCHANGE_RATE)
        }
    }
}
