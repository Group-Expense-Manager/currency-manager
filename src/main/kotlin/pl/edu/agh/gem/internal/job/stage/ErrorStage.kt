package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.StageFailure
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.ExchangeRateJob

@Component
class ErrorStage : ProcessingStage() {
    override fun process(exchangeRateJob: ExchangeRateJob): StageResult {
        return StageFailure
    }
}
