package pl.edu.agh.gem.internal.plan

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.take
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.config.ExchangeRatePlanProcessorProperties
import pl.edu.agh.gem.internal.persistence.ExchangeRatePlanRepository
import pl.edu.agh.gem.internal.plans.ExchangeRatePlanFinder
import pl.edu.agh.gem.util.TestThreadExecutor
import pl.edu.agh.gem.util.createExchangeRatePlan

class ExchangeRatePlanFinderTest : ShouldSpec({
    val exchangeRatePlanRepository = mock<ExchangeRatePlanRepository>()
    val exchangeRatePlanProperties = mock<ExchangeRatePlanProcessorProperties>()
    val exchangeRatePlanFinder = ExchangeRatePlanFinder(
        TestThreadExecutor(),
        exchangeRatePlanProperties,
        exchangeRatePlanRepository,
    )

    should("emit exchange rate plan") {
        // given
        val exchangeRatePlan = createExchangeRatePlan()
        whenever(exchangeRatePlanRepository.findReadyAndDelay()).thenReturn(exchangeRatePlan)

        // when
        val result = exchangeRatePlanFinder.findJobToProcess()

        // then
        result.take(1).collect {
            it shouldBe exchangeRatePlan
        }
    }
},)
