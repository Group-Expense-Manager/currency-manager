package pl.edu.agh.gem.internal.plan

import io.kotest.core.spec.style.ShouldSpec
import kotlinx.coroutines.flow.flowOf
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.plans.ExchangeRatePlanConsumer
import pl.edu.agh.gem.internal.plans.ExchangeRatePlanFinder
import pl.edu.agh.gem.internal.plans.ExchangeRatePlanProcessor
import pl.edu.agh.gem.util.TestThreadExecutor
import pl.edu.agh.gem.util.createExchangeRatePlan

class ExchangeRatePlanConsumerTest : ShouldSpec({
    val exchangeRatePlanFinder = mock<ExchangeRatePlanFinder>()
    val exchangeRatePlanProcessor = mock<ExchangeRatePlanProcessor>()
    val exchangeRatePlanConsumer = ExchangeRatePlanConsumer(
        exchangeRatePlanFinder,
        exchangeRatePlanProcessor,
    )

    should("successfully process exchange rate plan") {
        // given
        val exchangeRatePlan = createExchangeRatePlan()
        whenever(exchangeRatePlanFinder.findJobToProcess()).thenReturn(flowOf(exchangeRatePlan))

        // when
        exchangeRatePlanConsumer.consume(TestThreadExecutor())

        // then
        verify(exchangeRatePlanProcessor).process(exchangeRatePlan)
    }
},)
