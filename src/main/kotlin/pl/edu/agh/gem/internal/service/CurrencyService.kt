package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.config.AvailableCurrenciesProperties
import pl.edu.agh.gem.config.ExchangeRatesProperties
import pl.edu.agh.gem.internal.model.Currency
import pl.edu.agh.gem.internal.model.ExchangeRate
import java.math.BigDecimal
import java.time.Instant

@Service
class CurrencyService(
    private val availableCurrenciesProperties: AvailableCurrenciesProperties,
    private val exchangeRatesProperties: ExchangeRatesProperties,
) {
    fun getAvailableCurrencies(): List<Currency> {
        return availableCurrenciesProperties.codes.map { Currency(it) }
    }

    fun getExchangeRate(currencyFrom: String, currencyTo: String, date: Instant): ExchangeRate {
        exchangeRatesProperties.rates.find { it.baseCurrency == currencyFrom && it.targetCurrency == currencyTo }?.let {
            return ExchangeRate(it.baseCurrency, it.targetCurrency, BigDecimal(it.rate), date)
        } ?: throw MissingExchangeRateException(currencyTo, currencyFrom, date)
    }
}

class MissingExchangeRateException(currencyTo: String, currencyFrom: String, date: Instant) :
    RuntimeException("Exchange rate not found for $currencyFrom -> $currencyTo at $date")
