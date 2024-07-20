package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.NBPClient
import pl.edu.agh.gem.internal.job.ExchangeRateJobState
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.SAVING
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.model.addExchangeRate
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.time.Clock
import java.time.LocalDate

@Component
class ReverseExchangeRateStage(
    private val nBPClient: NBPClient,
    private val clock: Clock,
) : ProcessingStage() {
    override fun process(exchangeRateJob: ExchangeRateJob): StageResult {
        val exchangeRateToCurrency = nBPClient.getPolishExchangeRate(exchangeRateJob.currencyTo, LocalDate.ofInstant(exchangeRateJob.forDate,clock.zone))
        val exchangeRate = ONE.divide(exchangeRateToCurrency.exchangeRate)
        return nextStage(exchangeRateJob.addExchangeRate(exchangeRate), SAVING)
    }
}
