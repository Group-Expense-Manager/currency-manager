package pl.edu.agh.gem.integration.ability

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_JSON
import pl.edu.agh.gem.integration.environment.ProjectConfig.wiremock
import pl.edu.agh.gem.util.objectMapper
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

private fun createExchangeRateUri(table: String, currency: String, date: LocalDate) =
    "/api/exchangerates/rates/$table/$currency/${date.format(ISO_LOCAL_DATE)}/"

fun stubNBPExchangeRate(body: Any?, table: String, currency: String, date: LocalDate, statusCode: HttpStatusCode = OK) {
    wiremock.stubFor(
        get(urlMatching(createExchangeRateUri(table, currency, date)))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withHeader("Content-Type", APPLICATION_JSON.toString())
                    .withBody(
                        objectMapper().writeValueAsString(body),
                    ),

            ),
    )
}
