package pl.edu.agh.gem.internal.job

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.STARTING
import pl.edu.agh.gem.internal.persistence.ExchangeRateJobRepository
import pl.edu.agh.gem.util.createExchangeRateJob

class ExchangeRateJobProcessorTest : ShouldSpec({
    val currencyExchangeJobSelector = mock<CurrencyExchangeJobSelector>()
    val exchangeRateJobRepository = mock<ExchangeRateJobRepository>()
    val exchangeRateJobProcessor =
        ExchangeRateJobProcessor(
            currencyExchangeJobSelector,
            exchangeRateJobRepository,
        )

    should("handle NextStage state transition") {
        // given
        val exchangeRateJob = createExchangeRateJob()
        val nextStage =
            NextStage(
                exchangeRateJob.copy(state = STARTING),
                newState = STARTING,
            )
        val stateProcessor =
            mock<ProcessingStage> {
                on { process(exchangeRateJob) } doReturn nextStage
            }
        whenever(currencyExchangeJobSelector.select(any())).thenReturn(stateProcessor)

        // when
        exchangeRateJobProcessor.processExchangeRateJob(exchangeRateJob)

        // then
        verify(exchangeRateJobRepository).save(nextStage.exchangeRateJob.copy(state = nextStage.newState))
    }

    should("handle StageSuccess state transition") {
        // given
        val exchangeRateJob = createExchangeRateJob()
        val stateProcessor =
            mock<ProcessingStage> {
                on { process(exchangeRateJob) } doReturn StageSuccess
            }
        whenever(currencyExchangeJobSelector.select(any())).thenReturn(stateProcessor)

        // when
        exchangeRateJobProcessor.processExchangeRateJob(exchangeRateJob)

        // then
        verify(exchangeRateJobRepository).remove(exchangeRateJob)
    }

    should("handle StageFailure state transition") {
        // given
        val exchangeRateJob = createExchangeRateJob()
        val stateProcessor =
            mock<ProcessingStage> {
                on { process(exchangeRateJob) } doReturn StageFailure(Exception())
            }
        whenever(currencyExchangeJobSelector.select(any())).thenReturn(stateProcessor)

        // when
        exchangeRateJobProcessor.processExchangeRateJob(exchangeRateJob)

        // then
        verify(exchangeRateJobRepository).remove(exchangeRateJob)
    }

    should("handle StageRetry state transition") {
        // given
        val exchangeRateJob = createExchangeRateJob()
        val stateProcessor =
            mock<ProcessingStage> {
                on { process(exchangeRateJob) } doReturn StageRetry
            }
        whenever(currencyExchangeJobSelector.select(any())).thenReturn(stateProcessor)

        // when
        exchangeRateJobProcessor.processExchangeRateJob(exchangeRateJob)

        // then
        verify(exchangeRateJobRepository).updateNextProcessAtAndRetry(exchangeRateJob)
    }
})
