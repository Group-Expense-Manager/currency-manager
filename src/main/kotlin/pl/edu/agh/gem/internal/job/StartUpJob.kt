package pl.edu.agh.gem.internal.job

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
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
    private val clock: Clock
) : ApplicationRunner {

    override fun run(arguments: ApplicationArguments?) {
        val allowedCurrencyPairs = getAllPairsOfCurrencies()
        deleteAllNotAllowedPlans(allowedCurrencyPairs)
        insertAllAllowedPlans(allowedCurrencyPairs)
    }
    
    private fun insertAllAllowedPlans(allowedCurrencyPairs: List<Pair<String, String>>){
        allowedCurrencyPairs.forEach { 
            if(exchangeRatePlanRepository.get(it.first, it.second) == null){
                exchangeRatePlanRepository.insert(ExchangeRatePlan(
                        currencyFrom = it.first, 
                        currencyTo = it.second,
                        nextProcessAt = LocalDate.now(clock).atStartOfDay(clock.zone).toInstant()
                ))
            }
        }
    }
    
    private fun deleteAllNotAllowedPlans(allowedCurrencyPairs: List<Pair<String, String>>){
        exchangeRatePlanRepository.deleteNotAllowed(allowedCurrencyPairs)
    }
    
    private fun getAllPairsOfCurrencies(): List<Pair<String, String>> {
        return availableCurrenciesProperties.codes.zip(availableCurrenciesProperties.codes)
                .filter { it.first != it.second }
    }
}
