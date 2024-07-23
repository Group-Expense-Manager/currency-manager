package pl.edu.agh.gem.external.persistence.rate

import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.ExchangeRate
import pl.edu.agh.gem.internal.persistence.ExchangeRateRepository
import pl.edu.agh.gem.internal.persistence.MissingExchangeRateException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

@Repository
class MongoExchangeRateRepository(
    private val mongoOperations: MongoOperations,
    private val clock: Clock,
) : ExchangeRateRepository {
    override fun save(exchangeRate: ExchangeRate): ExchangeRate {
        return mongoOperations.save(exchangeRate.toEntity()).toDomain()
    }

    override fun getExchangeRate(currencyFrom: String, currencyTo: String, date: Instant): ExchangeRate {
        val query = Query.query(
            Criteria.where(ExchangeRateEntity::currencyFrom.name)
                .isEqualTo(currencyFrom)
                .and(ExchangeRateEntity::currencyTo.name)
                .isEqualTo(currencyTo)
                .and(ExchangeRateEntity::forDate.name)
                .isEqualTo(LocalDate.ofInstant(date, clock.zone).atStartOfDay(clock.zone).toInstant()),
        )
        return mongoOperations.findOne(query, ExchangeRateEntity::class.java)?.toDomain()
            ?: throw MissingExchangeRateException(currencyTo, currencyFrom, date)
    }
}
