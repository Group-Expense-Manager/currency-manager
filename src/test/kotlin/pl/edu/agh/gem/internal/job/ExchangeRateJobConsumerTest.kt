package pl.edu.agh.gem.internal.job

import io.kotest.core.spec.style.ShouldSpec
import kotlinx.coroutines.flow.flowOf
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.util.createExchangeRateJob

class ExchangeRateJobConsumerTest : ShouldSpec({
    val exchangeRateJobFinder = mock<ExchangeRateJobFinder>()
    val exchangeRateJobProcessor = mock<ExchangeRateJobProcessor>()
    val exchangeRateJobConsumer = ExchangeRateJobConsumer(
        exchangeRateJobFinder,
        exchangeRateJobProcessor,
    )

    should("successfully process exchange rate job") {
        // given
        val exchangeRateJob = createExchangeRateJob()
        whenever(exchangeRateJobFinder.findJobToProcess()).thenReturn(flowOf(exchangeRateJob))

        // when
        exchangeRateJobConsumer.consume(CurrentThreadExecutor())

        // then
        verify(exchangeRateJobProcessor).processExchangeRateJob(exchangeRateJob)
    }
})
