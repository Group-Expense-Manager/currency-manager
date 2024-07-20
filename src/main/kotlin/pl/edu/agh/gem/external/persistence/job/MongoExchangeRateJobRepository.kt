package pl.edu.agh.gem.external.persistence.job

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.config.ExchangeRateJobProcessorProperties
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.persistence.ExchangeRateJobRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Repository
class MongoExchangeRateJobRepository(
    private val mongoOperations: MongoOperations,
    private val exchangeRateJobProcessorProperties: ExchangeRateJobProcessorProperties,
    private val clock: Clock
): ExchangeRateJobRepository {
    override fun save(exchangeRateJob: ExchangeRateJob): ExchangeRateJob {
        return mongoOperations.save(exchangeRateJob.toEntity()).toDomain()
    }

    override fun findJobToProcessAndLock(): ExchangeRateJob? {
        val query = Query.query(Criteria.where(ExchangeRateJobEntity::nextProcessAt.name).lte(clock.instant()))
        val update = Update().set(ExchangeRateJobEntity::nextProcessAt.name, clock.instant().plus(exchangeRateJobProcessorProperties.lockTime))
        val options = FindAndModifyOptions.options().returnNew(false).upsert(false)
        return mongoOperations.findAndModify(query, update, options, ExchangeRateJobEntity::class.java)?.toDomain()
    }

    override fun updateNextProcessAtAndRetry(exchangeRateJob: ExchangeRateJob): ExchangeRateJob {
        val query = Query.query(Criteria.where(ExchangeRateJobEntity::id.name).`is`(exchangeRateJob))
        val update = Update().set(ExchangeRateJobEntity::nextProcessAt.name, clock.instant().plus(getDelay(exchangeRateJob.retry)))
        val options = FindAndModifyOptions.options().returnNew(true).upsert(false)
        return mongoOperations.findAndModify(query, update, options, ExchangeRateJobEntity::class.java)?.toDomain()
            ?: throw IllegalStateException("ExchangeRateJob with id ${exchangeRateJob.id} not found")
    }

    override fun remove(exchangeRateJob: ExchangeRateJob) {
        val query = Query.query(Criteria.where(ExchangeRateJobEntity::id.name).`is`(exchangeRateJob.id))
        mongoOperations.remove(query, ExchangeRateJobEntity::class.java)
    }

    private fun getDelay(retry: Long): Duration {
        return exchangeRateJobProcessorProperties.retryDelays.getOrNull(retry.toInt()) ?: exchangeRateJobProcessorProperties.retryDelays.last()
    }
    
}
