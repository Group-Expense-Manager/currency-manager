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

    fun failure(exception: Exception): StageResult {
        return StageFailure(exception)
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

data class StageFailure(
    val exception: Exception,
) : StageResult()

data object StageRetry : StageResult()
