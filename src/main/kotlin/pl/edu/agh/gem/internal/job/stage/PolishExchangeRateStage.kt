package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.NBPClient
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.SAVING
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.model.addExchangeRate

@Component
class PolishExchangeRateStage(
    private val nBPClient: NBPClient,
) : ProcessingStage() {
    override fun process(exchangeRateJob: ExchangeRateJob): StageResult {
        val exchangeRateFromCurrency = nBPClient.getPolishExchangeRate(
            exchangeRateJob.currencyFrom,
            exchangeRateJob.forDate,
        )

        return nextStage(exchangeRateJob.addExchangeRate(exchangeRateFromCurrency.exchangeRate), SAVING)
    }
}
