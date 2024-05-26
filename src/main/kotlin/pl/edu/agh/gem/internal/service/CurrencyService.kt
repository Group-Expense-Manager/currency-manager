package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.config.AvailableCurrenciesProperties
import pl.edu.agh.gem.internal.model.Currency

@Service
class CurrencyService(
    private val availableCurrenciesProperties: AvailableCurrenciesProperties,
) {
    fun getAvailableCurrencies(): List<Currency> {
        return availableCurrenciesProperties.codes.map { Currency(it) }
    }
}
