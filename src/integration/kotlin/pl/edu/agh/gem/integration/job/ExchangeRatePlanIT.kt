package pl.edu.agh.gem.integration.job

import com.mongodb.client.MongoClient
import io.kotest.matchers.date.shouldNotBeBefore
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.time.delay
import pl.edu.agh.gem.config.ExchangeRatePlanProcessorProperties
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubNBPExchangeRate
import pl.edu.agh.gem.integration.environment.ProjectConfig.DATABASE_NAME
import pl.edu.agh.gem.internal.persistence.ExchangeRatePlanRepository
import pl.edu.agh.gem.internal.persistence.ExchangeRateRepository
import pl.edu.agh.gem.util.createExchangeRatePlan
import pl.edu.agh.gem.util.createNBPExchangeResponse
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class ExchangeRatePlanIT(
    private val clock: Clock,
    private val exchangeRatePlanRepository: ExchangeRatePlanRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val mongoClient: MongoClient,
    private val exchangeRatePlanProcessorProperties: ExchangeRatePlanProcessorProperties,
) : BaseIntegrationSpec(
    {
        should("process exchange plan job successfully") { // given
            val localDate = LocalDate.ofInstant(FIXED_TIME, clock.zone)

            val firstExchangeRateResponse = createNBPExchangeResponse(
                code = "USD",
            )
            val secondExchangeRateResponse = createNBPExchangeResponse(
                code = "EUR",
            )
            val exchangeRateJob = createExchangeRatePlan(
                currencyFrom = firstExchangeRateResponse.code,
                currencyTo = secondExchangeRateResponse.code,
                forDate = FIXED_TIME,
                nextProcessAt = FIXED_TIME,
            )
            stubNBPExchangeRate(
                firstExchangeRateResponse,
                firstExchangeRateResponse.table,
                firstExchangeRateResponse.code,
                localDate,
            )
            stubNBPExchangeRate(
                secondExchangeRateResponse,
                secondExchangeRateResponse.table,
                secondExchangeRateResponse.code,
                localDate,
            )

            // when
            exchangeRatePlanRepository.insert(exchangeRateJob)

            // then
            waitTillJobEnd(exchangeRatePlanRepository, firstExchangeRateResponse.code, secondExchangeRateResponse.code, FIXED_TIME)
            waitTillJobEnd(mongoClient)
            val exchangeRate = exchangeRateRepository.getExchangeRate(
                firstExchangeRateResponse.code,
                secondExchangeRateResponse.code,
                FIXED_TIME,
            )
            exchangeRate.exchangeRate shouldBe firstExchangeRateResponse.rates.first().mid.divide(secondExchangeRateResponse.rates.first().mid)
            exchangeRate.currencyTo shouldBe secondExchangeRateResponse.code
            exchangeRate.currencyFrom shouldBe firstExchangeRateResponse.code
            exchangeRate.forDate shouldBe FIXED_TIME
            exchangeRate.validTo shouldNotBeBefore FIXED_TIME
            exchangeRate.createdAt.shouldNotBeNull()
            val exchangeRatePlan = exchangeRatePlanRepository.get(firstExchangeRateResponse.code, secondExchangeRateResponse.code)
            exchangeRatePlan.shouldNotBeNull()
            exchangeRatePlan.currencyFrom shouldBe firstExchangeRateResponse.code
            exchangeRatePlan.currencyTo shouldBe secondExchangeRateResponse.code
            exchangeRatePlan.forDate shouldBe FIXED_TIME.plus(exchangeRatePlanProcessorProperties.nextTimeFromMidnight)
        }
    },
)

private suspend fun waitTillJobEnd(exchangeRatePlanRepository: ExchangeRatePlanRepository, currencyFrom: String, currencyTo: String, date: Instant) {
    while (exchangeRatePlanRepository.get(currencyFrom, currencyTo)?.forDate?.isAfter(date) != true) {
        delay(1L.seconds.toJavaDuration())
    }
}

private suspend fun waitTillJobEnd(mongoClient: MongoClient) {
    while (mongoClient.getDatabase(DATABASE_NAME).getCollection("Jobs").countDocuments() > 0) {
        delay(1L.seconds.toJavaDuration())
    }
}
