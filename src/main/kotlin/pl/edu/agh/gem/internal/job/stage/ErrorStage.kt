package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.job.ExchangeRateJobState
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.StageFailure
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.ExchangeRateJob

@Component
class ErrorStage : ProcessingStage() {
    override fun process(exchangeRateJob: ExchangeRateJob): StageResult {
        logger.warn { "Error state reached for $exchangeRateJob" }
        return StageFailure(ErrorStateException(exchangeRateJob.state))
    }
}

class ErrorStateException(state: ExchangeRateJobState) : Exception("Error state reached from $state")
