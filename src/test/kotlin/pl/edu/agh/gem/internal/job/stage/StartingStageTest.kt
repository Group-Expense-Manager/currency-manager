package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.EXCHANGE_RATE
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.POLISH_EXCHANGE_RATE
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.REVERSE_POLISH_EXCHANGE_RATE
import pl.edu.agh.gem.util.createExchangeRateJob

class StartingStageTest : ShouldSpec({
    val startingState = spy(StartingStage())

    should("successfully process to next stage when currencies are not PLN") {
        // given
        val exchangeRateJob = createExchangeRateJob(
            currencyFrom = "USD",
            currencyTo = "EUR",
        )

        // when
        startingState.process(exchangeRateJob)

        // then
        verify(startingState).nextStage(
            exchangeRateJob,
            EXCHANGE_RATE,
        )
    }

    should("successfully process to next stage when currencyFrom is PLN") {
        // given
        val exchangeRateJob = createExchangeRateJob(
            currencyFrom = "PLN",
            currencyTo = "EUR",
        )

        // when
        startingState.process(exchangeRateJob)

        // then
        verify(startingState).nextStage(
            exchangeRateJob,
            REVERSE_POLISH_EXCHANGE_RATE,
        )
    }

    should("successfully process to next stage when currencyTo is PLN") {
        // given
        val exchangeRateJob = createExchangeRateJob(
            currencyFrom = "USD",
            currencyTo = "PLN",
        )

        // when
        startingState.process(exchangeRateJob)

        // then
        verify(startingState).nextStage(
            exchangeRateJob,
            POLISH_EXCHANGE_RATE,
        )
    }
},)
