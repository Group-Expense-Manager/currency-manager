package pl.edu.agh.gem.external.persistence.rate

import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.model.ExchangeRate
import java.math.BigDecimal
import java.time.Instant

@Document("ExchangeRates")
data class ExchangeRateEntity(
    val currencyFrom: String,
    val currencyTo: String,
    val rate: BigDecimal,
    val createdAt: Instant,
    val forDate: Instant,
    val validTo: Instant,
)

fun ExchangeRateEntity.toDomain(): ExchangeRate {
    return ExchangeRate(
        currencyFrom = currencyFrom,
        currencyTo = currencyTo,
        exchangeRate = rate,
        createdAt = createdAt,
        forDate = forDate,
        validTo = validTo,
    )
}

fun ExchangeRate.toEntity(): ExchangeRateEntity {
    return ExchangeRateEntity(
        currencyFrom = currencyFrom,
        currencyTo = currencyTo,
        rate = exchangeRate,
        createdAt = createdAt,
        forDate = forDate,
        validTo = validTo,
    )
}
