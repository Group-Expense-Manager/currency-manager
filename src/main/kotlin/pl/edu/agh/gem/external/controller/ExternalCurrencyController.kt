package pl.edu.agh.gem.external.controller

import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.external.dto.ExternalAvailableCurrenciesResponse
import pl.edu.agh.gem.external.dto.ExternalExchangeRateResponse
import pl.edu.agh.gem.external.dto.toExternalAvailableCurrenciesResponse
import pl.edu.agh.gem.external.dto.toExternalExchangeRateResponse
import pl.edu.agh.gem.internal.service.CurrencyService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import java.time.Instant
import java.time.Instant.now

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

    @GetMapping("/from/{currencyFrom}/to/{currencyTo}/", produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun getExchangeRates(
        @PathVariable currencyFrom: String,
        @PathVariable currencyTo: String,
        @RequestParam date: Instant?,
    ): ExternalExchangeRateResponse {
        return currencyService.getExchangeRate(currencyFrom, currencyTo, date ?: now()).toExternalExchangeRateResponse()
    }
}
