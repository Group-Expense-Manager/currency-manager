package pl.edu.agh.gem.internal.job

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.EXCHANGE_RATE
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.POLISH_EXCHANGE_RATE
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.REVERSE_POLISH_EXCHANGE_RATE
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.SAVING
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.STARTING
import pl.edu.agh.gem.internal.job.stage.ErrorStage
import pl.edu.agh.gem.internal.job.stage.ExchangeRateStage
import pl.edu.agh.gem.internal.job.stage.PolishExchangeRateStage
import pl.edu.agh.gem.internal.job.stage.ReverseExchangeRateStage
import pl.edu.agh.gem.internal.job.stage.SavingStage
import pl.edu.agh.gem.internal.job.stage.StartingStage

@Service
class CurrencyExchangeJobSelector(
    private val startingStage: StartingStage,
    private val exchangeRateStage: ExchangeRateStage,
    private val polishExchangeRateStage: PolishExchangeRateStage,
    private val reversePolishExchangeRateStage: ReverseExchangeRateStage,
    private val savingStage: SavingStage,
    private val errorStage: ErrorStage,
) {
    fun select(state: ExchangeRateJobState): ProcessingStage {
        return when (state) {
            STARTING -> startingStage
            EXCHANGE_RATE -> exchangeRateStage
            POLISH_EXCHANGE_RATE -> polishExchangeRateStage
            REVERSE_POLISH_EXCHANGE_RATE -> reversePolishExchangeRateStage
            SAVING -> savingStage
            else -> errorStage
        }
    }
}
