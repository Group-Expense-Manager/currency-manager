package pl.edu.agh.gem.internal.plans

import org.springframework.stereotype.Service
import pl.edu.agh.gem.external.persistence.plan.MongoExchangeRatePlansRepository
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.model.ExchangeRatePlan
import pl.edu.agh.gem.internal.persistence.ExchangeRateJobRepository

@Service
class ExchangeRatePlanProcessor(
    private val exchangeRateJobRepository: ExchangeRateJobRepository,
    private val exchangeRatePlansRepository: MongoExchangeRatePlansRepository,
) {
    fun process(exchangeRatePlan: ExchangeRatePlan) {
        exchangeRateJobRepository.save(
                ExchangeRateJob(
                        currencyFrom = exchangeRatePlan.currencyFrom,
                        currencyTo = exchangeRatePlan.currencyTo,
                        forDate = exchangeRatePlan.forDate
                )
        )
        exchangeRatePlansRepository.setNextTime(exchangeRatePlan)
    }
}
