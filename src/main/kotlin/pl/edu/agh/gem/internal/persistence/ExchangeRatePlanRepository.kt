package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.ExchangeRatePlan

interface ExchangeRatePlanRepository {
    fun insert(exchangeRatePlan: ExchangeRatePlan): ExchangeRatePlan
    fun findReadyAndDelay(): ExchangeRatePlan?
    fun delete(currencyFrom: String, currencyTo: String)
    fun retry(exchangeRatePlan: ExchangeRatePlan)
    fun setNextTime(exchangeRatePlan: ExchangeRatePlan): ExchangeRatePlan
    fun deleteNotAllowed(currencyPairs: List<Pair<String, String>>)
    fun get(currencyFrom: String, currencyTo: String): ExchangeRatePlan?
}

class ExchangePlanNotFoundException(exchangeRatePlan: ExchangeRatePlan) : RuntimeException("ExchangeRatePlan not found: $exchangeRatePlan")
