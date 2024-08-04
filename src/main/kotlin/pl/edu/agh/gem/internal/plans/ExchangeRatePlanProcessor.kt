package pl.edu.agh.gem.internal.plans

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.model.ExchangeRatePlan
import pl.edu.agh.gem.internal.persistence.ExchangeRateJobRepository
import pl.edu.agh.gem.internal.persistence.ExchangeRatePlanRepository
import java.time.Clock

@Service
class ExchangeRatePlanProcessor(
    private val exchangeRateJobRepository: ExchangeRateJobRepository,
    private val exchangeRatePlansRepository: ExchangeRatePlanRepository,
    private val clock: Clock,
) {
    fun process(exchangeRatePlan: ExchangeRatePlan) {
        exchangeRateJobRepository.save(
            ExchangeRateJob(
                currencyFrom = exchangeRatePlan.currencyFrom,
                currencyTo = exchangeRatePlan.currencyTo,
                forDate = exchangeRatePlan.forDate,
                nextProcessAt = clock.instant(),
            ),
        )
        exchangeRatePlansRepository.setNextTime(exchangeRatePlan)
    }
}
