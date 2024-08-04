package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.client.NBPClient
import pl.edu.agh.gem.internal.client.NBPClientException
import pl.edu.agh.gem.internal.client.RetryableNBPClientException
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.SAVING
import pl.edu.agh.gem.internal.model.addExchangeRate
import pl.edu.agh.gem.util.createExchangeRate
import pl.edu.agh.gem.util.createExchangeRateJob
import java.math.BigDecimal.ONE
import java.math.MathContext.DECIMAL128

class ReverseExchangeRateStageTest : ShouldSpec({
    val nBPClient = mock<NBPClient>()
    val reversePolishExchangeRateStage = spy(ReversePolishExchangeRateStage(nBPClient))

    should("successfully process exchange rates") {
        // given
        val exchangeRateJob = createExchangeRateJob()
        val exchangeRateTo = createExchangeRate(
            currencyFrom = "PLN",
            currencyTo = exchangeRateJob.currencyTo,
            rate = "0.3".toBigDecimal(),
        )

        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyTo, exchangeRateJob.forDate))
            .thenReturn(exchangeRateTo)

        // when
        reversePolishExchangeRateStage.process(exchangeRateJob)

        // then
        val expectedExchangeRate = ONE.divide(exchangeRateTo.exchangeRate, DECIMAL128)
        val expectedJob = exchangeRateJob.addExchangeRate(expectedExchangeRate)
        verify(nBPClient).getPolishExchangeRate(exchangeRateJob.currencyTo, exchangeRateJob.forDate)
        verify(reversePolishExchangeRateStage).nextStage(
            expectedJob,
            SAVING,
        )
    }

    should("retry on RetryableNBPClientException") {
        // given
        val exchangeRateJob = createExchangeRateJob()

        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyTo, exchangeRateJob.forDate))
            .thenThrow(RetryableNBPClientException("Retryable exception"))

        // when
        reversePolishExchangeRateStage.process(exchangeRateJob)

        // then
        verify(reversePolishExchangeRateStage).retry()
    }

    should("fail on NBPClientException") {
        // given
        val exchangeRateJob = createExchangeRateJob()

        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyTo, exchangeRateJob.forDate))
            .thenThrow(NBPClientException("Exception"))

        // when
        reversePolishExchangeRateStage.process(exchangeRateJob)

        // then
        verify(reversePolishExchangeRateStage).failure(any())
    }
},)
