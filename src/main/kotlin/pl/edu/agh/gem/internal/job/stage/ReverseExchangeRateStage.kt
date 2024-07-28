package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.NBPClient
import pl.edu.agh.gem.internal.client.RetryableNBPClientException
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.SAVING
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.model.addExchangeRate
import java.math.BigDecimal.ONE
import java.math.MathContext.DECIMAL128

@Component
class ReverseExchangeRateStage(
    private val nBPClient: NBPClient,
) : ProcessingStage() {
    override fun process(exchangeRateJob: ExchangeRateJob): StageResult {
        return try {
            val exchangeRateToCurrency = nBPClient.getPolishExchangeRate(
                exchangeRateJob.currencyTo,
                exchangeRateJob.forDate,
            )
            val exchangeRate = ONE.divide(exchangeRateToCurrency.exchangeRate, DECIMAL128)
            return nextStage(exchangeRateJob.addExchangeRate(exchangeRate), SAVING)
        } catch (exception: RetryableNBPClientException) {
            retry()
        } catch (exception: Exception) {
            logger.error(exception) {
                "Unexpected exception occurred on ${exchangeRateJob.currencyFrom} -> PLN for ${exchangeRateJob.forDate}"
            }
            failure(exception)
        }
    }
}
