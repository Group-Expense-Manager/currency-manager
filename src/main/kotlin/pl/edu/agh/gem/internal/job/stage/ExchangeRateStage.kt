package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.NBPClient
import pl.edu.agh.gem.internal.job.ExchangeRateJobState
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.model.addExchangeRate

@Component
class ExchangeRateStage(
    private val nBPClient: NBPClient,
) : ProcessingStage() {
    override fun process(exchangeRateJob: ExchangeRateJob): StageResult {
        val exchangeRateFromCurrency = nBPClient.getPolishExchangeRate(
            exchangeRateJob.currencyFrom,
            exchangeRateJob.forDate,
        )
        val exchangeRateToCurrency = nBPClient.getPolishExchangeRate(
            exchangeRateJob.currencyTo,
            exchangeRateJob.forDate,
        )
        val exchangeRate = exchangeRateFromCurrency.exchangeRate.divide(exchangeRateToCurrency.exchangeRate)

        return nextStage(exchangeRateJob.addExchangeRate(exchangeRate), ExchangeRateJobState.SAVING)
    }
}
