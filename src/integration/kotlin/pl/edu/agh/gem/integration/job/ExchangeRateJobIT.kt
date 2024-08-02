package pl.edu.agh.gem.integration.job

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.date.shouldNotBeBefore
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.time.delay
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubNBPExchangeRate
import pl.edu.agh.gem.internal.persistence.ExchangeRateJobRepository
import pl.edu.agh.gem.internal.persistence.ExchangeRateRepository
import pl.edu.agh.gem.internal.persistence.MissingExchangeRateException
import pl.edu.agh.gem.util.createExchangeRateJob
import pl.edu.agh.gem.util.createNBPExchangeResponse
import java.time.Clock
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class ExchangeRateJobIT(
    @SpyBean private val clock: Clock,
    private val exchangeRateJobRepository: ExchangeRateJobRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
) : BaseIntegrationSpec({

    should("process exchange rate job successfully") {
        // given
        val startedTime = testClock.instant()
        whenever(clock.instant()).thenAnswer { FIXED_TIME.plusSeconds(elapsedSeconds(startedTime)) }
        val localDate = LocalDate.ofInstant(FIXED_TIME, clock.zone)

        val firstExchangeRateResponse = createNBPExchangeResponse(
            code = "USD",
        )
        val secondExchangeRateResponse = createNBPExchangeResponse(
            code = "EUR",
        )
        val exchangeRateJob = createExchangeRateJob(
            currencyFrom = firstExchangeRateResponse.code,
            currencyTo = secondExchangeRateResponse.code,
            forDate = localDate,
            nextProcessAt = FIXED_TIME,
        )
        stubNBPExchangeRate(firstExchangeRateResponse, firstExchangeRateResponse.table, firstExchangeRateResponse.code, localDate)
        stubNBPExchangeRate(secondExchangeRateResponse, secondExchangeRateResponse.table, secondExchangeRateResponse.code, localDate)

        // when
        exchangeRateJobRepository.save(exchangeRateJob)

        // then
        waitTillExchangePlan(exchangeRateJobRepository, exchangeRateJob.id)
        val exchangeRate = exchangeRateRepository.getExchangeRate(firstExchangeRateResponse.code, secondExchangeRateResponse.code, localDate)
        exchangeRate.exchangeRate shouldBe firstExchangeRateResponse.rates.first().mid.divide(secondExchangeRateResponse.rates.first().mid)
        exchangeRate.currencyTo shouldBe secondExchangeRateResponse.code
        exchangeRate.currencyFrom shouldBe firstExchangeRateResponse.code
        exchangeRate.forDate shouldBe localDate
        exchangeRate.validTo shouldNotBeBefore localDate
        exchangeRate.createdAt.shouldNotBeNull()
    }

    should("fail exchange rate job successfully") {
        // given
        val startedTime = testClock.instant()
        whenever(clock.instant()).thenAnswer { FIXED_TIME.plusSeconds(elapsedSeconds(startedTime)) }
        val localDate = LocalDate.ofInstant(FIXED_TIME, clock.zone)
        val firstExchangeRateResponse = createNBPExchangeResponse(
            code = "USD",
        )
        val secondExchangeRateResponse = createNBPExchangeResponse(
            code = "EUR",
        )
        val exchangeRateJob = createExchangeRateJob(
            currencyFrom = firstExchangeRateResponse.code,
            currencyTo = secondExchangeRateResponse.code,
            forDate = localDate,
            nextProcessAt = FIXED_TIME,
        )

        // when
        exchangeRateJobRepository.save(exchangeRateJob)

        // then
        waitTillExchangePlan(exchangeRateJobRepository, exchangeRateJob.id)
        shouldThrow<MissingExchangeRateException> {
            exchangeRateRepository
                .getExchangeRate(firstExchangeRateResponse.code, secondExchangeRateResponse.code, localDate)
        }
    }

    should("retry exchange rate job successfully") {
        // given
        val startedTime = testClock.instant()
        whenever(clock.instant()).thenAnswer { FIXED_TIME.plusSeconds(elapsedSeconds(startedTime)) }
        val localDate = LocalDate.ofInstant(FIXED_TIME, clock.zone)

        val firstExchangeRateResponse = createNBPExchangeResponse(
            code = "USD",
        )
        val secondExchangeRateResponse = createNBPExchangeResponse(
            code = "EUR",
        )
        val exchangeRateJob = createExchangeRateJob(
            currencyFrom = firstExchangeRateResponse.code,
            currencyTo = secondExchangeRateResponse.code,
            forDate = localDate,
            nextProcessAt = FIXED_TIME,
        )
        stubNBPExchangeRate(
            firstExchangeRateResponse,
            firstExchangeRateResponse.table,
            firstExchangeRateResponse.code,
            localDate,
            INTERNAL_SERVER_ERROR,
        )
        stubNBPExchangeRate(
            secondExchangeRateResponse,
            secondExchangeRateResponse.table,
            secondExchangeRateResponse.code,
            localDate,
            INTERNAL_SERVER_ERROR,
        )

        // when
        exchangeRateJobRepository.save(exchangeRateJob)

        // then
        waitTillJobEndRetry(exchangeRateJobRepository, exchangeRateJob.id)
        shouldThrow<MissingExchangeRateException> {
            exchangeRateRepository.getExchangeRate(firstExchangeRateResponse.code, secondExchangeRateResponse.code, localDate)
        }

        // when
        stubNBPExchangeRate(firstExchangeRateResponse, firstExchangeRateResponse.table, firstExchangeRateResponse.code, localDate)
        stubNBPExchangeRate(secondExchangeRateResponse, secondExchangeRateResponse.table, secondExchangeRateResponse.code, localDate)

        // then
        waitTillExchangePlan(exchangeRateJobRepository, exchangeRateJob.id)
        val exchangeRateAfterRetry = exchangeRateRepository.getExchangeRate(
            firstExchangeRateResponse.code,
            secondExchangeRateResponse.code,
            localDate,
        )
        exchangeRateAfterRetry.exchangeRate shouldBe firstExchangeRateResponse.rates.first().mid.divide(secondExchangeRateResponse.rates.first().mid)
        exchangeRateAfterRetry.currencyTo shouldBe secondExchangeRateResponse.code
        exchangeRateAfterRetry.currencyFrom shouldBe firstExchangeRateResponse.code
        exchangeRateAfterRetry.forDate shouldBe localDate
        exchangeRateAfterRetry.validTo shouldNotBeBefore localDate
        exchangeRateAfterRetry.createdAt.shouldNotBeNull()
    }
},)

private suspend fun waitTillExchangePlan(exchangeRateJobRepository: ExchangeRateJobRepository, exchangeRateJobId: String) {
    while (true) {
        delay(1L.seconds.toJavaDuration())
        if (exchangeRateJobRepository.findById(exchangeRateJobId) == null) {
            break
        }
    }
}

private suspend fun waitTillJobEndRetry(exchangeRateJobRepository: ExchangeRateJobRepository, exchangeRateJobId: String) {
    while (true) {
        delay(1L.seconds.toJavaDuration())
        val exchangeRateJob = exchangeRateJobRepository.findById(exchangeRateJobId)
        if (exchangeRateJob != null && exchangeRateJob.retry != 0L) {
            break
        }
    }
}
