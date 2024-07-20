package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.ExchangeRate

interface ExchangeRateRepository {
    fun save(exchangeRate: ExchangeRate): ExchangeRate
    fun getValidExchangeRate(currencyFrom: String, currencyTo: String): ExchangeRate
}
