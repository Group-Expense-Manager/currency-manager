package pl.edu.agh.gem.external.client

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.config.NBPProperties
import pl.edu.agh.gem.external.dto.NBPExchangeResponse
import pl.edu.agh.gem.external.dto.toExchangeRate
import pl.edu.agh.gem.internal.client.NBPClient
import pl.edu.agh.gem.internal.model.ExchangeRate
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

@Component
class RestNBPClient(
    @Qualifier("NBPRestTemplate") private val restTemplate: RestTemplate,
    private val nBPProperties: NBPProperties,
    private val clock: Clock,
) : NBPClient {
    override fun getPolishExchangeRate(currency: String, date: LocalDate): ExchangeRate {
        return try {
            restTemplate.exchange(
                    resolveExchangeRateUrl(currency,date),
                    GET,
                    HttpEntity<Any>(HttpHeaders()),
                    NBPExchangeResponse::class.java,
            ).body?.toExchangeRate(clock,nBPProperties.exchangeRateValidTime) ?: throw RuntimeException("No response body")
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to retrieve attachmentId" }
            throw RuntimeException("No response body")
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to retrieve attachmentId" }
            throw RuntimeException("No response body")
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to retrieve attachmentId" }
            throw RuntimeException("No response body")
        }
    }

    private fun resolveExchangeRateUrl(currency: String, date: LocalDate) =
            "${nBPProperties.url}/api/exchangerates/rates/C/$currency/${date.format(ISO_LOCAL_DATE)}/"

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
