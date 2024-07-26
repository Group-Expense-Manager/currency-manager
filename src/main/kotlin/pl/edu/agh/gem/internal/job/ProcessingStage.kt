package pl.edu.agh.gem.internal.job

import mu.KotlinLogging
import pl.edu.agh.gem.internal.model.ExchangeRateJob

abstract class ProcessingStage {

    abstract fun process(exchangeRateJob: ExchangeRateJob): StageResult

    fun nextStage(exchangeRateJob: ExchangeRateJob, nextState: ExchangeRateJobState): StageResult {
        return NextStage(exchangeRateJob, nextState)
    }

    fun success(): StageResult {
        return StageSuccess
    }

    fun failure(): StageResult {
        return StageFailure
    }

    fun retry(): StageResult {
        return StageRetry
    }

    protected companion object {
        val logger = KotlinLogging.logger { }
    }
}

sealed class StageResult

data class NextStage(
    val exchangeRateJob: ExchangeRateJob,
    val newState: ExchangeRateJobState,
) : StageResult()

data object StageSuccess : StageResult()

data object StageFailure : StageResult()

data object StageRetry : StageResult()
