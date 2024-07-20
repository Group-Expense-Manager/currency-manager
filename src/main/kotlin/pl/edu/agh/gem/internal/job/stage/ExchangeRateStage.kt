package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.NBPClient
import pl.edu.agh.gem.internal.job.ExchangeRateJobState
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.model.addExchangeRate
import java.time.Clock
import java.time.LocalDate

@Component
class ExchangeRateStage(
        private val nBPClient: NBPClient,
        private val clock: Clock,
) : ProcessingStage() {
    override fun process(exchangeRateJob: ExchangeRateJob): StageResult {
        val exchangeRateFromCurrency = nBPClient.getPolishExchangeRate(exchangeRateJob.currencyFrom, LocalDate.ofInstant(exchangeRateJob.forDate,clock.zone))
        val exchangeRateToCurrency = nBPClient.getPolishExchangeRate(exchangeRateJob.currencyTo, LocalDate.ofInstant(exchangeRateJob.forDate,clock.zone))
        val exchangeRate = exchangeRateFromCurrency.exchangeRate.divide(exchangeRateToCurrency.exchangeRate)
        
        return nextStage(exchangeRateJob.addExchangeRate(exchangeRate), ExchangeRateJobState.SAVING)
    }
}
