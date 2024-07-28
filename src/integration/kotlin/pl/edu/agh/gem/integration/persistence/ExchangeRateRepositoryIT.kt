package pl.edu.agh.gem.integration.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.mock.mockito.SpyBean
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.internal.persistence.ExchangeRateRepository
import pl.edu.agh.gem.internal.persistence.MissingExchangeRateException
import pl.edu.agh.gem.util.createExchangeRate
import java.time.Clock

class ExchangeRateRepositoryIT(
    @SpyBean private val clock: Clock,
    private val exchangeRateRepository: ExchangeRateRepository,
) : BaseIntegrationSpec({

    should("save and find exchange rate by currency pair and date") {
        // given
        val exchangeRate = createExchangeRate(currencyFrom = "USD", currencyTo = "PLN", forDate = FIXED_TIME)

        // when
        exchangeRateRepository.save(exchangeRate)

        // then
        val foundRate = exchangeRateRepository.getExchangeRate("USD", "PLN", FIXED_TIME)

        // then
        foundRate shouldBe exchangeRate
    }

    should("throw MissingExchangeRateException when exchange rate not found") {
        // given
        val currencyFrom = "USD"
        val currencyTo = "PLN"
        val date = FIXED_TIME

        // when & then
        shouldThrow<MissingExchangeRateException> {
            exchangeRateRepository.getExchangeRate(currencyFrom, currencyTo, date)
        }
    }

    should("return null when trying to find exchange rate with non-matching date") {
        // given
        val exchangeRate = createExchangeRate(currencyFrom = "USD", currencyTo = "PLN", forDate = FIXED_TIME)
        exchangeRateRepository.save(exchangeRate)
        val nonMatchingDate = FIXED_TIME.minusSeconds(3600)

        // when & then
        shouldThrow<MissingExchangeRateException> {
            exchangeRateRepository.getExchangeRate("USD", "PLN", nonMatchingDate)
        }
    }

    should("find exchange rate within valid date range") {
        // given
        val exchangeRate = createExchangeRate(
            currencyFrom = "USD",
            currencyTo = "PLN",
            forDate = FIXED_TIME.minusSeconds(3600),
            validTo = FIXED_TIME.plusSeconds(3600),
        )
        exchangeRateRepository.save(exchangeRate)

        // when
        val foundRate = exchangeRateRepository.getExchangeRate("USD", "PLN", FIXED_TIME)

        // then
        foundRate shouldBe exchangeRate
    }
},)
