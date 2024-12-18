package pl.edu.agh.gem.external.persistence.job

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.config.ExchangeRateJobProcessorProperties
import pl.edu.agh.gem.internal.model.ExchangeRateJob
import pl.edu.agh.gem.internal.persistence.ExchangeRateJobRepository
import pl.edu.agh.gem.internal.persistence.MissingExchangeRateJobException
import pl.edu.agh.gem.metrics.MeteredRepository
import java.time.Clock
import java.time.Duration

@Repository
@MeteredRepository
class MongoExchangeRateJobRepository(
    private val mongoOperations: MongoOperations,
    private val exchangeRateJobProcessorProperties: ExchangeRateJobProcessorProperties,
    private val clock: Clock,
) : ExchangeRateJobRepository {
    override fun save(exchangeRateJob: ExchangeRateJob): ExchangeRateJob {
        return mongoOperations.save(exchangeRateJob.toEntity(clock)).toDomain(clock)
    }

    override fun findJobToProcessAndLock(): ExchangeRateJob? {
        val query = Query.query(Criteria.where(ExchangeRateJobEntity::nextProcessAt.name).lte(clock.instant()))
        val update = Update()
            .set(ExchangeRateJobEntity::nextProcessAt.name, clock.instant().plus(exchangeRateJobProcessorProperties.lockTime))
        val options = FindAndModifyOptions.options().returnNew(false).upsert(false)
        return mongoOperations.findAndModify(query, update, options, ExchangeRateJobEntity::class.java)?.toDomain(clock)
    }

    override fun updateNextProcessAtAndRetry(exchangeRateJob: ExchangeRateJob): ExchangeRateJob {
        val query = Query.query(Criteria.where(ExchangeRateJobEntity::id.name).isEqualTo(exchangeRateJob.id))
        val update = Update()
            .set(ExchangeRateJobEntity::nextProcessAt.name, clock.instant().plus(getDelay(exchangeRateJob.retry)))
            .set(ExchangeRateJobEntity::retry.name, exchangeRateJob.retry + 1)
        val options = FindAndModifyOptions.options().returnNew(true).upsert(false)
        mongoOperations.findAll(ExchangeRateJobEntity::class.java)
        return mongoOperations.findAndModify(query, update, options, ExchangeRateJobEntity::class.java)?.toDomain(clock)
            ?: throw MissingExchangeRateJobException(exchangeRateJob)
    }

    override fun remove(exchangeRateJob: ExchangeRateJob) {
        val query = Query.query(Criteria.where(ExchangeRateJobEntity::id.name).isEqualTo(exchangeRateJob.id))
        mongoOperations.remove(query, ExchangeRateJobEntity::class.java)
    }

    override fun findById(id: String): ExchangeRateJob? {
        return mongoOperations.findById(id, ExchangeRateJobEntity::class.java)?.toDomain(clock)
    }

    private fun getDelay(retry: Long): Duration {
        return exchangeRateJobProcessorProperties.retryDelays.getOrNull(retry.toInt()) ?: exchangeRateJobProcessorProperties.retryDelays.last()
    }
}
