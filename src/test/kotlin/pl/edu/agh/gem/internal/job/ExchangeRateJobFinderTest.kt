package pl.edu.agh.gem.internal.job

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.take
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.config.ExchangeRateJobProcessorProperties
import pl.edu.agh.gem.internal.persistence.ExchangeRateJobRepository
import pl.edu.agh.gem.util.TestThreadExecutor
import pl.edu.agh.gem.util.createExchangeRateJob

class ExchangeRateJobFinderTest : ShouldSpec({
    val exchangeRateJobRepository = mock<ExchangeRateJobRepository>()
    val exchangeRateJobProperties = mock<ExchangeRateJobProcessorProperties>()
    val exchangeRateJobFinder =
        ExchangeRateJobFinder(
            TestThreadExecutor(),
            exchangeRateJobProperties,
            exchangeRateJobRepository,
        )

    should("emit exchange rate job") {
        // given
        val exchangeRateJob = createExchangeRateJob()
        whenever(exchangeRateJobRepository.findJobToProcessAndLock()).thenReturn(exchangeRateJob)

        // when
        val result = exchangeRateJobFinder.findJobToProcess()

        // then
        result.take(1).collect {
            it shouldBe exchangeRateJob
        }
    }
})
