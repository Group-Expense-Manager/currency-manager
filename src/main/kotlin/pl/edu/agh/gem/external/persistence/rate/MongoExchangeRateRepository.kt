package pl.edu.agh.gem.external.persistence.rate

import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.ExchangeRate
import pl.edu.agh.gem.internal.persistence.ExchangeRateRepository
import pl.edu.agh.gem.internal.persistence.MissingExchangeRateException
import pl.edu.agh.gem.metrics.MeteredRepository
import java.time.Clock
import java.time.LocalDate

@Repository
@MeteredRepository
open class MongoExchangeRateRepository(
    private val mongoOperations: MongoOperations,
    private val clock: Clock,
) : ExchangeRateRepository {
    override fun save(exchangeRate: ExchangeRate): ExchangeRate {
        return mongoOperations.save(exchangeRate.toEntity(clock)).toDomain(clock)
    }

    override fun getExchangeRate(
        currencyFrom: String,
        currencyTo: String,
        date: LocalDate,
    ): ExchangeRate {
        val query =
            Query.query(
                Criteria.where(ExchangeRateEntity::currencyFrom.name)
                    .isEqualTo(currencyFrom)
                    .and(ExchangeRateEntity::currencyTo.name)
                    .isEqualTo(currencyTo)
                    .and(ExchangeRateEntity::forDate.name)
                    .lte(date.atStartOfDay(clock.zone).toInstant())
                    .and(ExchangeRateEntity::validTo.name)
                    .gte(date.atStartOfDay(clock.zone).toInstant()),
            )
        return mongoOperations.findOne(query, ExchangeRateEntity::class.java)?.toDomain(clock)
            ?: throw MissingExchangeRateException(currencyTo, currencyFrom, date)
    }
}
