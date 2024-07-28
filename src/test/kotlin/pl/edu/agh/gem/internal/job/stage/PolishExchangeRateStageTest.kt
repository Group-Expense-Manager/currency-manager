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

class PolishExchangeRateStageTest : ShouldSpec({
    val nBPClient = mock<NBPClient>()
    val polishExchangeRateStage = spy(PolishExchangeRateStage(nBPClient))

    should("successfully process exchange rates") {
        // given
        val exchangeRateJob = createExchangeRateJob()
        val exchangeRateFrom = createExchangeRate(
            currencyFrom = exchangeRateJob.currencyFrom,
            currencyTo = "PLN",
            rate = "4.0".toBigDecimal(),
        )

        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyFrom, exchangeRateJob.forDate))
            .thenReturn(exchangeRateFrom)

        // when
        polishExchangeRateStage.process(exchangeRateJob)

        // then
        val expectedExchangeRate = exchangeRateFrom.exchangeRate
        val expectedJob = exchangeRateJob.addExchangeRate(expectedExchangeRate)
        verify(nBPClient).getPolishExchangeRate(exchangeRateJob.currencyFrom, exchangeRateJob.forDate)
        verify(polishExchangeRateStage).nextStage(
            expectedJob,
            SAVING,
        )
    }

    should("retry on RetryableNBPClientException") {
        // given
        val exchangeRateJob = createExchangeRateJob()

        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyFrom, exchangeRateJob.forDate))
            .thenThrow(RetryableNBPClientException("Retryable exception"))

        // when
        polishExchangeRateStage.process(exchangeRateJob)

        // then
        verify(polishExchangeRateStage).retry()
    }

    should("fail on NBPClientException") {
        // given
        val exchangeRateJob = createExchangeRateJob()

        whenever(nBPClient.getPolishExchangeRate(exchangeRateJob.currencyFrom, exchangeRateJob.forDate))
            .thenThrow(NBPClientException("Exception"))

        // when
        polishExchangeRateStage.process(exchangeRateJob)

        // then
        verify(polishExchangeRateStage).failure(any())
    }
},)
