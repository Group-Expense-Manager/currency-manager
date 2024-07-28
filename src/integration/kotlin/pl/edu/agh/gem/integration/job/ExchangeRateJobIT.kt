package pl.edu.agh.gem.integration.job

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.date.shouldNotBeBefore
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.time.delay
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
    private val clock: Clock,
    private val exchangeRateJobRepository: ExchangeRateJobRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
) : BaseIntegrationSpec({

    should("process exchange rate job successfully") {
        // given
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
            forDate = FIXED_TIME,
            nextProcessAt = FIXED_TIME,
        )
        stubNBPExchangeRate(firstExchangeRateResponse, firstExchangeRateResponse.table, firstExchangeRateResponse.code, localDate)
        stubNBPExchangeRate(secondExchangeRateResponse, secondExchangeRateResponse.table, secondExchangeRateResponse.code, localDate)

        // when
        exchangeRateJobRepository.save(exchangeRateJob)

        // then
        waitTillJobEnd(exchangeRateJobRepository, exchangeRateJob.id)
        val exchangeRate = exchangeRateRepository.getExchangeRate(firstExchangeRateResponse.code, secondExchangeRateResponse.code, FIXED_TIME)
        exchangeRate.exchangeRate shouldBe firstExchangeRateResponse.rates.first().mid.divide(secondExchangeRateResponse.rates.first().mid)
        exchangeRate.currencyTo shouldBe secondExchangeRateResponse.code
        exchangeRate.currencyFrom shouldBe firstExchangeRateResponse.code
        exchangeRate.forDate shouldBe FIXED_TIME
        exchangeRate.validTo shouldNotBeBefore FIXED_TIME
        exchangeRate.createdAt.shouldNotBeNull()
    }

    should("fail exchange rate job successfully") {
        // given
        val firstExchangeRateResponse = createNBPExchangeResponse(
            code = "USD",
        )
        val secondExchangeRateResponse = createNBPExchangeResponse(
            code = "EUR",
        )
        val exchangeRateJob = createExchangeRateJob(
            currencyFrom = firstExchangeRateResponse.code,
            currencyTo = secondExchangeRateResponse.code,
            forDate = FIXED_TIME,
            nextProcessAt = FIXED_TIME,
        )

        // when
        exchangeRateJobRepository.save(exchangeRateJob)

        // then
        waitTillJobEnd(exchangeRateJobRepository, exchangeRateJob.id)
        shouldThrow<MissingExchangeRateException> {
            exchangeRateRepository
                .getExchangeRate(firstExchangeRateResponse.code, secondExchangeRateResponse.code, FIXED_TIME)
        }
    }

    should("retry exchange rate job successfully") {
        // given
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
            forDate = FIXED_TIME,
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
            exchangeRateRepository.getExchangeRate(firstExchangeRateResponse.code, secondExchangeRateResponse.code, FIXED_TIME)
        }

        // when
        stubNBPExchangeRate(firstExchangeRateResponse, firstExchangeRateResponse.table, firstExchangeRateResponse.code, localDate)
        stubNBPExchangeRate(secondExchangeRateResponse, secondExchangeRateResponse.table, secondExchangeRateResponse.code, localDate)

        // then
        waitTillJobEnd(exchangeRateJobRepository, exchangeRateJob.id)
        val exchangeRateAfterRetry = exchangeRateRepository.getExchangeRate(
            firstExchangeRateResponse.code,
            secondExchangeRateResponse.code,
            FIXED_TIME,
        )
        exchangeRateAfterRetry.exchangeRate shouldBe firstExchangeRateResponse.rates.first().mid.divide(secondExchangeRateResponse.rates.first().mid)
        exchangeRateAfterRetry.currencyTo shouldBe secondExchangeRateResponse.code
        exchangeRateAfterRetry.currencyFrom shouldBe firstExchangeRateResponse.code
        exchangeRateAfterRetry.forDate shouldBe FIXED_TIME
        exchangeRateAfterRetry.validTo shouldNotBeBefore FIXED_TIME
        exchangeRateAfterRetry.createdAt.shouldNotBeNull()
    }
},)

private suspend fun waitTillJobEnd(exchangeRateJobRepository: ExchangeRateJobRepository, exchangeRateJobId: String) {
    while (exchangeRateJobRepository.findById(exchangeRateJobId) != null) {
        println("Waiting for job to end ${exchangeRateJobRepository.findById(exchangeRateJobId)}")
        delay(1L.seconds.toJavaDuration())
    }
}

private suspend fun waitTillJobEndRetry(exchangeRateJobRepository: ExchangeRateJobRepository, exchangeRateJobId: String) {
    while (exchangeRateJobRepository.findById(exchangeRateJobId).run { this != null && this.retry == 0L }) {
        println("Waiting for job to end retry ${exchangeRateJobRepository.findById(exchangeRateJobId)}")
        delay(1L.seconds.toJavaDuration())
    }
}
