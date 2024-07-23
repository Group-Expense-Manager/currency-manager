package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.job.ExchangeRateJobState.SUCCESS
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.ExchangeRate
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.persistence.ExchangeRateRepository
import java.time.Clock

@Component
class SavingStage(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val clock: Clock,
) : ProcessingStage() {
    override fun process(exchangeRateJob: ExchangeRateJob): StageResult {
        exchangeRateRepository.save(
            ExchangeRate(
                currencyFrom = exchangeRateJob.currencyFrom,
                currencyTo = exchangeRateJob.currencyTo,
                exchangeRate = exchangeRateJob.exchangeRate ?: throw MissingExchangeRateInSavingStageException(exchangeRateJob),
                createdAt = clock.instant(),
                validTo = exchangeRateJob.forDate,
                forDate = exchangeRateJob.forDate,
            ),
        )
        return nextStage(exchangeRateJob, SUCCESS)
    }
}

class MissingExchangeRateInSavingStageException(exchangeRateJob: ExchangeRateJob) :
    RuntimeException("No exchange rate found for ${exchangeRateJob.currencyFrom} -> ${exchangeRateJob.currencyTo} on ${exchangeRateJob.forDate}")
