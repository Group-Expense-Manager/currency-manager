package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.ExchangeRate
import java.time.LocalDate

interface ExchangeRateRepository {
    fun save(exchangeRate: ExchangeRate): ExchangeRate

    fun getExchangeRate(
        currencyFrom: String,
        currencyTo: String,
        date: LocalDate,
    ): ExchangeRate
}

class MissingExchangeRateException(currencyTo: String, currencyFrom: String, date: LocalDate) :
    RuntimeException("Exchange rate not found for $currencyFrom -> $currencyTo at $date")
