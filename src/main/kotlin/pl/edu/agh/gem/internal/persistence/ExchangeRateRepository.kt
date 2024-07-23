package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.ExchangeRate
import java.time.Instant

interface ExchangeRateRepository {
    fun save(exchangeRate: ExchangeRate): ExchangeRate
    fun getExchangeRate(currencyFrom: String, currencyTo: String, date: Instant): ExchangeRate
}

class MissingExchangeRateException(currencyTo: String, currencyFrom: String, date: Instant) :
    RuntimeException("Exchange rate not found for $currencyFrom -> $currencyTo at $date")
