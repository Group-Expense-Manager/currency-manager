package pl.edu.agh.gem.external.persistence.rate

import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.ExchangeRate
import pl.edu.agh.gem.internal.persistence.ExchangeRateRepository

@Repository
class MongoExchangeRateRepository(
        private val mongoOperations: MongoOperations,
): ExchangeRateRepository {
    override fun save(exchangeRate: ExchangeRate): ExchangeRate {
        return mongoOperations.save(exchangeRate.toEntity()).toDomain()
    }

    override fun getValidExchangeRate(currencyFrom: String, currencyTo: String): ExchangeRate {
        TODO("Not yet implemented")
    }
}
