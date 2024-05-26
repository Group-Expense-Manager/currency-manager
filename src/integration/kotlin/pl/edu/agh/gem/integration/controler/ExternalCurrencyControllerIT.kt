package pl.edu.agh.gem.integration.controler

import io.kotest.matchers.collections.shouldContainExactly
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.assertion.shouldBody
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.external.dto.ExternalAvailableCurrenciesResponse
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient

class ExternalCurrencyControllerIT(
    private val service: ServiceTestClient,
) : BaseIntegrationSpec({
    should("get available currencies successfully") {
        // when
        val response = service.getExternalAvailableCurrencies()

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<ExternalAvailableCurrenciesResponse> {
            currencies.map { it.code }.toSet() shouldContainExactly setOf("USD", "EUR", "PLN")
        }
    }
},)
