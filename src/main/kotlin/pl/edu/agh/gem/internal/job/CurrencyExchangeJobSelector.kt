package pl.edu.agh.gem.internal.job

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.job.stage.ErrorStage
import pl.edu.agh.gem.internal.job.stage.ExchangeRateStage
import pl.edu.agh.gem.internal.job.stage.PolishExchangeRateStage
import pl.edu.agh.gem.internal.job.stage.ReverseExchangeRateStage
import pl.edu.agh.gem.internal.job.stage.StartingStage

@Service
class CurrencyExchangeJobSelector(
    private val startingStage: StartingStage,
    private val exchangeRateStage: ExchangeRateStage,
    private val polishExchangeRateStage: PolishExchangeRateStage,
    private val reversePolishExchangeRateStage: ReverseExchangeRateStage,
    private val errorStage: ErrorStage
) {
    fun select(state: ExchangeRateJobState): ProcessingStage {
        return when ( state ) {
            ExchangeRateJobState.STARTING -> startingStage
            ExchangeRateJobState.EXCHANGE_RATE -> exchangeRateStage
            ExchangeRateJobState.POLISH_EXCHANGE_RATE -> polishExchangeRateStage
            ExchangeRateJobState.REVERSE_POLISH_EXCHANGE_RATE -> reversePolishExchangeRateStage
            else -> errorStage
        }
    }
}
