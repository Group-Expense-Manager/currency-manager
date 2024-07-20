package pl.edu.agh.gem.external.persistence.plan

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.IndexDirection.ASCENDING
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.model.ExchangeRatePlan
import java.time.Instant

@Document("Plans")
data class ExchangeRatePlanEntity(
    @Id
    val id: CompositeKey,
    @Indexed(direction = ASCENDING, background = true)
    val nextProcessAt: Instant,
    val forDate: Instant,
)

data class CompositeKey(
        val currencyFrom: String,
        val currencyTo: String,
)

fun ExchangeRatePlanEntity.toDomain(): ExchangeRatePlan {
    return ExchangeRatePlan(
            currencyFrom = id.currencyFrom,
            currencyTo = id.currencyTo,
            nextProcessAt = nextProcessAt,
            forDate = forDate,
    )
}

fun ExchangeRatePlan.toEntity(): ExchangeRatePlanEntity {
    return ExchangeRatePlanEntity(
            id = CompositeKey(currencyFrom, currencyTo),
            nextProcessAt = nextProcessAt,
            forDate = forDate,
    )
}
