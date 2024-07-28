package pl.edu.agh.gem.integration.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.mock.mockito.SpyBean
import pl.edu.agh.gem.config.ExchangeRatePlanProcessorProperties
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.internal.persistence.ExchangePlanNotFoundException
import pl.edu.agh.gem.internal.persistence.ExchangeRatePlanRepository
import pl.edu.agh.gem.util.createExchangeRatePlan
import java.time.Clock
import java.time.LocalDate

class ExchangeRatePlansRepositoryIT(
    @SpyBean private val clock: Clock,
    private val mongoExchangeRatePlansRepository: ExchangeRatePlanRepository,
    private val exchangeRatePlanProcessorProperties: ExchangeRatePlanProcessorProperties,
) : BaseIntegrationSpec({

    should("insert and get exchange rate plan by currency pair") {
        // given
        val exchangeRatePlan = createExchangeRatePlan(currencyFrom = "USD", currencyTo = "PLN", forDate = FIXED_TIME)

        // when
        val savedPlan = mongoExchangeRatePlansRepository.insert(exchangeRatePlan)

        // then
        savedPlan shouldBe exchangeRatePlan
        val foundPlan = mongoExchangeRatePlansRepository.get("USD", "PLN")
        foundPlan shouldBe exchangeRatePlan
    }

    should("find and delay next process time for a ready exchange rate plan") {
        // given
        val exchangeRatePlan = createExchangeRatePlan(currencyFrom = "USD", currencyTo = "PLN", nextProcessAt = FIXED_TIME)
        mongoExchangeRatePlansRepository.insert(exchangeRatePlan)

        // when
        val planToProcess = mongoExchangeRatePlansRepository.findReadyAndDelay()

        // then
        planToProcess.shouldNotBeNull()
        val foundPlan = mongoExchangeRatePlansRepository.get("USD", "PLN")
        foundPlan.shouldNotBeNull()
        foundPlan.currencyTo shouldBe exchangeRatePlan.currencyTo
        foundPlan.currencyFrom shouldBe exchangeRatePlan.currencyFrom
        foundPlan.forDate shouldBe exchangeRatePlan.forDate
        foundPlan.nextProcessAt shouldBe FIXED_TIME.plus(exchangeRatePlanProcessorProperties.lockTime)
    }

    should("delete exchange rate plan by currency pair") {
        // given
        val exchangeRatePlan = createExchangeRatePlan(currencyFrom = "USD", currencyTo = "PLN")
        mongoExchangeRatePlansRepository.insert(exchangeRatePlan)

        // when
        mongoExchangeRatePlansRepository.delete("USD", "PLN")

        // then
        val foundPlan = mongoExchangeRatePlansRepository.get("USD", "PLN")
        foundPlan.shouldBeNull()
    }

    should("retry exchange rate plan") {
        // given
        val exchangeRatePlan = createExchangeRatePlan(currencyFrom = "USD", currencyTo = "PLN", nextProcessAt = FIXED_TIME)
        mongoExchangeRatePlansRepository.insert(exchangeRatePlan)

        // when
        mongoExchangeRatePlansRepository.retry(exchangeRatePlan)

        // then
        val updatedPlan = mongoExchangeRatePlansRepository.get("USD", "PLN")
        updatedPlan.shouldNotBeNull()
        updatedPlan.currencyTo shouldBe exchangeRatePlan.currencyTo
        updatedPlan.currencyFrom shouldBe exchangeRatePlan.currencyFrom
        updatedPlan.forDate shouldBe exchangeRatePlan.forDate
        updatedPlan.nextProcessAt shouldBe FIXED_TIME.plus(exchangeRatePlanProcessorProperties.retryDelay)
    }

    should("set next processing time for exchange rate plan") {
        // given
        val exchangeRatePlan = createExchangeRatePlan(
            currencyFrom = "USD",
            currencyTo = "PLN",
            forDate = FIXED_TIME,
            nextProcessAt = FIXED_TIME,
        )
        mongoExchangeRatePlansRepository.insert(exchangeRatePlan)
        val nextTimeDate = LocalDate.ofInstant(FIXED_TIME, clock.zone)
            .atStartOfDay(clock.zone)
            .toInstant()
            .plus(exchangeRatePlanProcessorProperties.nextTimeFromMidnight)

        // when
        val updatedPlan = mongoExchangeRatePlansRepository.setNextTime(exchangeRatePlan)

        // then
        updatedPlan.shouldNotBeNull()
        updatedPlan.nextProcessAt shouldBe nextTimeDate
        updatedPlan.forDate shouldBe nextTimeDate
    }

    should("delete exchange rate plans that are not allowed") {
        // given
        val allowedPlan = createExchangeRatePlan(currencyFrom = "USD", currencyTo = "PLN")
        val disallowedPlan = createExchangeRatePlan(currencyFrom = "EUR", currencyTo = "PLN")
        mongoExchangeRatePlansRepository.insert(allowedPlan)
        mongoExchangeRatePlansRepository.insert(disallowedPlan)

        // when
        mongoExchangeRatePlansRepository.deleteNotAllowed(listOf(Pair("USD", "PLN")))

        // then
        val foundAllowedPlan = mongoExchangeRatePlansRepository.get("USD", "PLN")
        val foundDisallowedPlan = mongoExchangeRatePlansRepository.get("EUR", "PLN")
        foundDisallowedPlan.shouldBeNull()
        foundAllowedPlan.shouldNotBeNull()
    }

    should("throw ExchangePlanNotFoundException when setting next processing time for a non-existing plan") {
        // given
        val nonExistingPlan = createExchangeRatePlan(currencyFrom = "USD", currencyTo = "PLN", forDate = FIXED_TIME)

        // when & then
        shouldThrow<ExchangePlanNotFoundException> {
            mongoExchangeRatePlansRepository.setNextTime(nonExistingPlan)
        }
    }

    should("return null when no ready exchange rate plan is found") {
        // given
        val exchangeRatePlan = createExchangeRatePlan(nextProcessAt = FIXED_TIME.plusSeconds(3600))
        mongoExchangeRatePlansRepository.insert(exchangeRatePlan)

        // when
        val planToProcess = mongoExchangeRatePlansRepository.findReadyAndDelay()

        // then
        planToProcess.shouldBeNull()
    }
},)
