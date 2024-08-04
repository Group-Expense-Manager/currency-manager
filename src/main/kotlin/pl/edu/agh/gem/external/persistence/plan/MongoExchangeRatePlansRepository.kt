package pl.edu.agh.gem.external.persistence.plan

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.config.ExchangeRatePlanProcessorProperties
import pl.edu.agh.gem.internal.model.ExchangeRatePlan
import pl.edu.agh.gem.internal.persistence.ExchangePlanNotFoundException
import pl.edu.agh.gem.internal.persistence.ExchangeRatePlanRepository
import java.time.Clock

@Repository
class MongoExchangeRatePlansRepository(
    private val mongoOperations: MongoOperations,
    private val clock: Clock,
    private val exchangeRatePlanProcessorProperties: ExchangeRatePlanProcessorProperties,
) : ExchangeRatePlanRepository {

    override fun insert(exchangeRatePlan: ExchangeRatePlan): ExchangeRatePlan {
        return mongoOperations.insert(exchangeRatePlan.toEntity(clock)).toDomain(clock)
    }

    override fun findReadyAndDelay(): ExchangeRatePlan? {
        val options = FindAndModifyOptions().returnNew(false).upsert(false)
        val update = Update()
            .set(ExchangeRatePlanEntity::nextProcessAt.name, clock.instant().plus(exchangeRatePlanProcessorProperties.lockTime))
        val query = Query.query(Criteria.where(ExchangeRatePlanEntity::nextProcessAt.name).lte(clock.instant()))
        return mongoOperations.findAndModify(query, update, options, ExchangeRatePlanEntity::class.java)?.toDomain(clock)
    }

    override fun delete(currencyFrom: String, currencyTo: String) {
        val query = Query.query(Criteria.where(ExchangeRatePlanEntity::id.name).isEqualTo(CompositeKey(currencyFrom, currencyTo)))
        mongoOperations.remove(query, ExchangeRatePlanEntity::class.java)
    }

    override fun retry(exchangeRatePlan: ExchangeRatePlan) {
        val query = Query.query(
            Criteria.where(ExchangeRatePlanEntity::id.name).isEqualTo(CompositeKey(exchangeRatePlan.currencyFrom, exchangeRatePlan.currencyTo)),
        )
        val update = Update()
            .set(ExchangeRatePlanEntity::nextProcessAt.name, clock.instant().plus(exchangeRatePlanProcessorProperties.retryDelay))
        mongoOperations.updateFirst(query, update, ExchangeRatePlanEntity::class.java)
    }

    override fun setNextTime(exchangeRatePlan: ExchangeRatePlan): ExchangeRatePlan {
        val options = FindAndModifyOptions().returnNew(true).upsert(false)
        val query = Query.query(
            Criteria.where(ExchangeRatePlanEntity::id.name).isEqualTo(CompositeKey(exchangeRatePlan.currencyFrom, exchangeRatePlan.currencyTo)),
        )
        val nextTimeDate = exchangeRatePlan.forDate
            .atStartOfDay(clock.zone)
            .toInstant()
            .plus(exchangeRatePlanProcessorProperties.nextTimeFromMidnight)
        val update = Update()
            .set(ExchangeRatePlanEntity::nextProcessAt.name, nextTimeDate)
            .set(ExchangeRatePlanEntity::forDate.name, nextTimeDate)
        return mongoOperations
            .findAndModify(query, update, options, ExchangeRatePlanEntity::class.java)
            ?.toDomain(clock)
            ?: throw ExchangePlanNotFoundException(exchangeRatePlan)
    }

    override fun deleteNotAllowed(currencyPairs: List<Pair<String, String>>) {
        val query = Query.query(Criteria.where(ExchangeRatePlanEntity::id.name).nin(currencyPairs.map { CompositeKey(it.first, it.second) }))
        mongoOperations.remove(query, ExchangeRatePlanEntity::class.java)
    }

    override fun get(currencyFrom: String, currencyTo: String): ExchangeRatePlan? {
        val query = Query.query(Criteria.where(ExchangeRatePlanEntity::id.name).isEqualTo(CompositeKey(currencyFrom, currencyTo)))
        return mongoOperations.findOne(query, ExchangeRatePlanEntity::class.java)?.toDomain(clock)
    }
}
