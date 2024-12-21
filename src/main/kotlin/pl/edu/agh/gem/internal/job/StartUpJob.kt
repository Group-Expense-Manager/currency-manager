package pl.edu.agh.gem.internal.job

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import pl.edu.agh.gem.config.AvailableCurrenciesProperties
import pl.edu.agh.gem.internal.model.ExchangeRatePlan
import pl.edu.agh.gem.internal.persistence.ExchangeRatePlanRepository
import java.time.Clock
import java.time.LocalDate

@Component
class StartUpJob(
    private val availableCurrenciesProperties: AvailableCurrenciesProperties,
    private val exchangeRatePlanRepository: ExchangeRatePlanRepository,
    private val clock: Clock,
) : InitializingBean {
    override fun afterPropertiesSet() {
        val allowedCurrencyPairs = getAllPairsOfCurrencies()
        deleteAllNotAllowedPlans(allowedCurrencyPairs)
        insertAllAllowedPlans(allowedCurrencyPairs)
    }

    private fun insertAllAllowedPlans(allowedCurrencyPairs: List<Pair<String, String>>) {
        allowedCurrencyPairs.forEach {
            if (exchangeRatePlanRepository.get(it.first, it.second) == null) {
                exchangeRatePlanRepository.insert(
                    ExchangeRatePlan(
                        currencyFrom = it.first,
                        currencyTo = it.second,
                        forDate = LocalDate.now(clock),
                        nextProcessAt = clock.instant(),
                    ),
                )
            }
        }
    }

    private fun deleteAllNotAllowedPlans(allowedCurrencyPairs: List<Pair<String, String>>) {
        exchangeRatePlanRepository.deleteNotAllowed(allowedCurrencyPairs)
    }

    private fun getAllPairsOfCurrencies(): List<Pair<String, String>> {
        return availableCurrenciesProperties.codes
            .flatMap { availableCurrenciesProperties.codes.map { other -> it to other } }
            .filterNot { it.first == it.second }
    }
}
