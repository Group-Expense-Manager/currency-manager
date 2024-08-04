package pl.edu.agh.gem.internal.plan

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.STARTING
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.persistence.ExchangeRateJobRepository
import pl.edu.agh.gem.internal.persistence.ExchangeRatePlanRepository
import pl.edu.agh.gem.internal.plans.ExchangeRatePlanProcessor
import pl.edu.agh.gem.util.createExchangeRatePlan
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ExchangeRatePlanProcessorTest : ShouldSpec({

    val exchangeRateJobRepository = mock<ExchangeRateJobRepository>()
    val exchangeRatePlanRepository = mock<ExchangeRatePlanRepository>()
    val clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00Z"), ZoneId.of("UTC"))
    val exchangeRatePlanProcessor = ExchangeRatePlanProcessor(
        exchangeRateJobRepository,
        exchangeRatePlanRepository,
        clock,
    )

    should("process exchange rate plan correctly") {
        // given
        val exchangeRatePlan = createExchangeRatePlan()
        val captor = argumentCaptor<ExchangeRateJob>()

        // when
        exchangeRatePlanProcessor.process(exchangeRatePlan)

        // then
        verify(exchangeRateJobRepository).save(captor.capture())
        val capturedExchangeRateJob = captor.firstValue
        with(capturedExchangeRateJob) {
            currencyFrom shouldBe exchangeRatePlan.currencyFrom
            currencyTo shouldBe exchangeRatePlan.currencyTo
            forDate shouldBe exchangeRatePlan.forDate
            nextProcessAt shouldBe clock.instant()
            state shouldBe STARTING
            retry shouldBe 0
        }
        verify(exchangeRatePlanRepository).setNextTime(exchangeRatePlan)
    }
},)
