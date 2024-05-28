package pl.edu.agh.gem.external.controller

import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.external.dto.ExternalAvailableCurrenciesResponse
import pl.edu.agh.gem.external.dto.toExternalAvailableCurrenciesResponse
import pl.edu.agh.gem.internal.service.CurrencyService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.paths.Paths.EXTERNAL

@RestController
@RequestMapping("$EXTERNAL/currencies")
class ExternalCurrencyController(
    private val currencyService: CurrencyService,
) {

    @GetMapping(produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun getAvailableCurrencies(): ExternalAvailableCurrenciesResponse {
        return currencyService.getAvailableCurrencies().toExternalAvailableCurrenciesResponse()
    }
}
