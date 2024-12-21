package pl.edu.agh.gem.integration.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.mock.mockito.SpyBean
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.internal.persistence.ExchangeRateRepository
import pl.edu.agh.gem.internal.persistence.MissingExchangeRateException
import pl.edu.agh.gem.util.createExchangeRate
import java.time.Clock
import java.time.LocalDate

class ExchangeRateRepositoryIT(
    @SpyBean private val clock: Clock,
    private val exchangeRateRepository: ExchangeRateRepository,
) : BaseIntegrationSpec({

        should("save and find exchange rate by currency pair and date") {
            // given
            val localDate = LocalDate.ofInstant(FIXED_TIME, clock.zone)
            val exchangeRate = createExchangeRate(currencyFrom = "USD", currencyTo = "PLN", forDate = localDate)

            // when
            exchangeRateRepository.save(exchangeRate)

            // then
            val foundRate = exchangeRateRepository.getExchangeRate("USD", "PLN", localDate)

            // then
            foundRate shouldBe exchangeRate
        }

        should("throw MissingExchangeRateException when exchange rate not found") {
            // given
            val currencyFrom = "USD"
            val currencyTo = "PLN"
            val localDate = LocalDate.ofInstant(FIXED_TIME, clock.zone)

            // when & then
            shouldThrow<MissingExchangeRateException> {
                exchangeRateRepository.getExchangeRate(currencyFrom, currencyTo, localDate)
            }
        }

        should("return null when trying to find exchange rate with non-matching date") {
            // given
            val localDate = LocalDate.ofInstant(FIXED_TIME, clock.zone)
            val exchangeRate = createExchangeRate(currencyFrom = "USD", currencyTo = "PLN", forDate = localDate)
            exchangeRateRepository.save(exchangeRate)
            val nonMatchingDate = localDate.minusDays(10)

            // when & then
            shouldThrow<MissingExchangeRateException> {
                exchangeRateRepository.getExchangeRate("USD", "PLN", nonMatchingDate)
            }
        }

        should("find exchange rate within valid date range") {
            // given
            val localDate = LocalDate.ofInstant(FIXED_TIME, clock.zone)
            val exchangeRate =
                createExchangeRate(
                    currencyFrom = "USD",
                    currencyTo = "PLN",
                    forDate = localDate.minusDays(10),
                    validTo = localDate.plusDays(10),
                )
            exchangeRateRepository.save(exchangeRate)

            // when
            val foundRate = exchangeRateRepository.getExchangeRate("USD", "PLN", localDate)

            // then
            foundRate shouldBe exchangeRate
        }
    })
