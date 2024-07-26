package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
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
import java.math.MathContext.DECIMAL128

class ExchangeRateStageTest : ShouldSpec({
    val nBPClient = mock<NBPClient>()
    val exchangeRateStage = spy(ExchangeRateStage(nBPClient))

    should("successfully process exchange rates") {
        // given
        val exchangeRateJob = createExchangeRateJob()
        val exchangeRateFrom = createExchangeRate(
            currencyFrom = exchangeRateJob.currencyFrom,
            currencyTo = "PLN",
            rate = "4.0".toBigDecimal(),
        )
        val exchangeRateTo = createExchangeRate(
            currencyFrom = "PLN",
            currencyTo = exchangeRateJob.currencyTo,
            rate = "0.3".toBigDecimal(),
        )

        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyFrom, exchangeRateJob.forDate))
            .thenReturn(exchangeRateFrom)
        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyTo, exchangeRateJob.forDate))
            .thenReturn(exchangeRateTo)

        // when
        exchangeRateStage.process(exchangeRateJob)

        // then
        val expectedExchangeRate = exchangeRateFrom.exchangeRate.divide(exchangeRateTo.exchangeRate, DECIMAL128)
        val expectedJob = exchangeRateJob.addExchangeRate(expectedExchangeRate)
        verify(nBPClient).getPolishExchangeRate(exchangeRateJob.currencyFrom, exchangeRateJob.forDate)
        verify(nBPClient).getPolishExchangeRate(exchangeRateJob.currencyTo, exchangeRateJob.forDate)
        verify(exchangeRateStage).nextStage(
            expectedJob,
            SAVING,
        )
    }

    should("retry on RetryableNBPClientException") {
        // given
        val exchangeRateJob = createExchangeRateJob()
        val exchangeRateTo = createExchangeRate(
            currencyFrom = "PLN",
            currencyTo = exchangeRateJob.currencyTo,
            rate = "0.3".toBigDecimal(),
        )

        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyFrom, exchangeRateJob.forDate))
            .thenThrow(RetryableNBPClientException("Retryable exception"))
        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyTo, exchangeRateJob.forDate))
            .thenReturn(exchangeRateTo)

        // when
        exchangeRateStage.process(exchangeRateJob)

        // then
        verify(nBPClient).getPolishExchangeRate(exchangeRateJob.currencyFrom, exchangeRateJob.forDate)
        verify(exchangeRateStage).retry()
    }

    should("fail on NBPClientException") {
        // given
        val exchangeRateJob = createExchangeRateJob()
        val exchangeRateTo = createExchangeRate(
            currencyFrom = "PLN",
            currencyTo = exchangeRateJob.currencyTo,
            rate = "0.3".toBigDecimal(),
        )

        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyFrom, exchangeRateJob.forDate))
            .thenThrow(NBPClientException("Exception"))
        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyTo, exchangeRateJob.forDate))
            .thenReturn(exchangeRateTo)

        // when
        exchangeRateStage.process(exchangeRateJob)

        // then
        verify(nBPClient).getPolishExchangeRate(exchangeRateJob.currencyFrom, exchangeRateJob.forDate)
        verify(exchangeRateStage).failure()
    }
},)
