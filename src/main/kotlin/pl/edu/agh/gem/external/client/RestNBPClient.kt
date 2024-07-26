package pl.edu.agh.gem.external.client

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.config.ExchangeRatesProperties
import pl.edu.agh.gem.config.NBPProperties
import pl.edu.agh.gem.external.dto.NBPExchangeResponse
import pl.edu.agh.gem.external.dto.toExchangeRate
import pl.edu.agh.gem.internal.client.ExchangeRateTable
import pl.edu.agh.gem.internal.client.ExchangeRateTable.A
import pl.edu.agh.gem.internal.client.ExchangeRateTable.B
import pl.edu.agh.gem.internal.client.IncorrectCurrencyException
import pl.edu.agh.gem.internal.client.MissingBodyFromNBPException
import pl.edu.agh.gem.internal.client.NBPClient
import pl.edu.agh.gem.internal.client.NBPClientException
import pl.edu.agh.gem.internal.client.RetryableNBPClientException
import pl.edu.agh.gem.internal.model.ExchangeRate
import java.time.Clock
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.temporal.TemporalAdjusters.previousOrSame

@Component
class RestNBPClient(
    @Qualifier("NBPRestTemplate") private val restTemplate: RestTemplate,
    private val nBPProperties: NBPProperties,
    private val exchangeRatesProperties: ExchangeRatesProperties,
    private val clock: Clock,
) : NBPClient {
    override fun getPolishExchangeRate(currency: String, date: Instant): ExchangeRate {
        val table = getTable(currency)
        val result: ResponseEntity<NBPExchangeResponse>

        try {
            result = restTemplate.exchange(
                resolveExchangeRateUrl(table, currency, getAdjustedDate(table, date)),
                GET,
                HttpEntity<Any>(HttpHeaders()),
                NBPExchangeResponse::class.java
            )
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to get exchange rate for $currency on date $date" }
            throw NBPClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to get exchange rate for $currency on date $date" }
            throw RetryableNBPClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to get exchange rate for $currency on date $date" }
            throw NBPClientException(ex.message)
        }

        return result.body?.toExchangeRate(clock, exchangeRatesProperties.validDuration)
            ?: throw MissingBodyFromNBPException()
    }

    private fun getTable(currency: String) =
        nBPProperties.table.filterValues { it.currencies.contains(currency) }.keys.firstOrNull() ?: throw IncorrectCurrencyException(currency)

    private fun getAdjustedDate(table: ExchangeRateTable, date: Instant): LocalDate {
        val dateInLocalDate = LocalDate.ofInstant(date, clock.zone)
        return when (table) {
            A -> adjustToWeekday(dateInLocalDate)
            B -> adjustToLastWednesday(dateInLocalDate)
        }
    }

    private fun adjustToWeekday(date: LocalDate): LocalDate {
        return when (date.dayOfWeek) {
            SATURDAY -> date.minusDays(1)
            SUNDAY -> date.minusDays(2)
            else -> date
        }
    }

    fun adjustToLastWednesday(date: LocalDate): LocalDate {
        return date.with(previousOrSame(WEDNESDAY))
    }

    private fun resolveExchangeRateUrl(table: ExchangeRateTable, currency: String, date: LocalDate) =
        "${nBPProperties.url}/api/exchangerates/rates/$table/$currency/${date.format(ISO_LOCAL_DATE)}/"

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
