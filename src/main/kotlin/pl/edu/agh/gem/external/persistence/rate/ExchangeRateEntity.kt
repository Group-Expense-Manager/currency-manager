package pl.edu.agh.gem.external.persistence.rate

import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.model.ExchangeRate
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

@Document("exchange-rates")
data class ExchangeRateEntity(
    val currencyFrom: String,
    val currencyTo: String,
    val rate: BigDecimal,
    val createdAt: Instant,
    val forDate: Instant,
    val validTo: Instant,
)

fun ExchangeRateEntity.toDomain(clock: Clock): ExchangeRate {
    return ExchangeRate(
        currencyFrom = currencyFrom,
        currencyTo = currencyTo,
        exchangeRate = rate,
        createdAt = createdAt,
        forDate = LocalDate.ofInstant(forDate, clock.zone),
        validTo = LocalDate.ofInstant(validTo, clock.zone),
    )
}

fun ExchangeRate.toEntity(clock: Clock): ExchangeRateEntity {
    return ExchangeRateEntity(
        currencyFrom = currencyFrom,
        currencyTo = currencyTo,
        rate = exchangeRate,
        createdAt = createdAt,
        forDate = forDate.atStartOfDay(clock.zone).toInstant(),
        validTo = validTo.atStartOfDay(clock.zone).toInstant(),
    )
}
