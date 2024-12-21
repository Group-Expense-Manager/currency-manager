package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubNBPExchangeRate
import pl.edu.agh.gem.internal.client.IncorrectCurrencyException
import pl.edu.agh.gem.internal.client.MissingBodyFromNBPException
import pl.edu.agh.gem.internal.client.NBPClient
import pl.edu.agh.gem.internal.client.NBPClientException
import pl.edu.agh.gem.internal.client.RetryableNBPClientException
import pl.edu.agh.gem.util.createNBPExchangeRate
import pl.edu.agh.gem.util.createNBPExchangeResponse
import java.time.Clock
import java.time.LocalDate

class RestNBPClientIT(
    private val nBPClient: NBPClient,
    private val clock: Clock,
) : BaseIntegrationSpec({

        should("get exchange rate for table A currency") {
            // given
            val date = FIXED_TIME
            val localDate = LocalDate.ofInstant(date, clock.zone)
            val exchangeRateResponse =
                createNBPExchangeResponse(
                    table = "A",
                    currency = "american dollar",
                    code = "USD",
                    rates =
                        listOf(
                            createNBPExchangeRate(
                                mid = "4.20".toBigDecimal(),
                                effectiveDate = localDate,
                            ),
                        ),
                )

            stubNBPExchangeRate(exchangeRateResponse, exchangeRateResponse.table, exchangeRateResponse.code, localDate)

            // when
            val result = nBPClient.getPolishExchangeRate(exchangeRateResponse.code, localDate)

            // then
            result.forDate shouldBe localDate
            result.currencyFrom shouldBe exchangeRateResponse.code
            result.exchangeRate shouldBe exchangeRateResponse.rates.first().mid
        }

        should("get exchange rate for table B currency") {
            // given
            val date = FIXED_WEDNESDAY
            val localDate = LocalDate.ofInstant(date, clock.zone)
            val exchangeRateResponse =
                createNBPExchangeResponse(
                    table = "B",
                    currency = "bolivian boliviano",
                    code = "BOB",
                    rates =
                        listOf(
                            createNBPExchangeRate(
                                mid = "4.20".toBigDecimal(),
                                effectiveDate = localDate,
                            ),
                        ),
                )

            stubNBPExchangeRate(exchangeRateResponse, exchangeRateResponse.table, exchangeRateResponse.code, localDate)

            // when
            val result = nBPClient.getPolishExchangeRate(exchangeRateResponse.code, localDate)

            // then
            result.forDate shouldBe localDate
            result.currencyFrom shouldBe exchangeRateResponse.code
            result.exchangeRate shouldBe exchangeRateResponse.rates.first().mid
        }

        should("throw IncorrectCurrencyException for invalid currency") {
            // given
            val invalidCurrency = "INVALID"
            val date = FIXED_TIME
            val localDate = LocalDate.ofInstant(date, clock.zone)

            // when & then
            shouldThrow<IncorrectCurrencyException> {
                nBPClient.getPolishExchangeRate(invalidCurrency, localDate)
            }
        }

        should("throw NBPClientException for client error") {
            // given
            val date = FIXED_TIME
            val currency = "USD"
            val localDate = LocalDate.ofInstant(date, clock.zone)
            stubNBPExchangeRate(null, "A", currency, localDate, BAD_REQUEST)

            // when & then
            shouldThrow<NBPClientException> {
                nBPClient.getPolishExchangeRate(currency, localDate)
            }
        }

        should("throw RetryableNBPClientException for server error") {
            // given
            val date = FIXED_TIME
            val currency = "USD"
            val localDate = LocalDate.ofInstant(date, clock.zone)
            stubNBPExchangeRate(null, "A", currency, localDate, INTERNAL_SERVER_ERROR)

            // when & then
            shouldThrow<RetryableNBPClientException> {
                nBPClient.getPolishExchangeRate(currency, localDate)
            }
        }

        should("throw MissingBodyFromNBPException for missing response body") {
            // given
            val date = FIXED_TIME
            val currency = "USD"
            val localDate = LocalDate.ofInstant(date, clock.zone)
            stubNBPExchangeRate(null, "A", currency, localDate)

            // when & then
            shouldThrow<MissingBodyFromNBPException> {
                nBPClient.getPolishExchangeRate(currency, localDate)
            }
        }
    })
