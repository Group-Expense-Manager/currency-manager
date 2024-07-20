package pl.edu.agh.gem.internal.job

import pl.edu.agh.gem.internal.model.ExchangeRateJob

abstract class ProcessingStage {
    
    abstract fun process(exchangeRateJob:ExchangeRateJob): StageResult
    
    fun nextStage(exchangeRateJob:ExchangeRateJob, nextState: ExchangeRateJobState): StageResult {
        return NextStage(exchangeRateJob,nextState)
    }
}

sealed class StageResult

data class NextStage(
    val exchangeRateJob: ExchangeRateJob,
    val newState: ExchangeRateJobState
): StageResult()

data object StageSuccess: StageResult()

data object StageFailure: StageResult()

data object StageRetry: StageResult()
