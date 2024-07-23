package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.config.AvailableCurrenciesProperties
import pl.edu.agh.gem.internal.model.Currency
import pl.edu.agh.gem.internal.model.ExchangeRate
import pl.edu.agh.gem.internal.persistence.ExchangeRateRepository
import java.time.Instant

@Service
class CurrencyService(
    private val availableCurrenciesProperties: AvailableCurrenciesProperties,
    private val exchangeRateRepository: ExchangeRateRepository,
) {
    fun getAvailableCurrencies(): List<Currency> {
        return availableCurrenciesProperties.codes.map { Currency(it) }
    }

    fun getExchangeRate(currencyFrom: String, currencyTo: String, date: Instant): ExchangeRate {
        return exchangeRateRepository.getExchangeRate(currencyFrom, currencyTo, date)
    }
}
