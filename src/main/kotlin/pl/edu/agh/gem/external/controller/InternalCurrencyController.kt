package pl.edu.agh.gem.external.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.external.dto.InternalAvailableCurrenciesResponse
import pl.edu.agh.gem.external.dto.toInternalAvailableCurrenciesResponse
import pl.edu.agh.gem.internal.service.CurrencyService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.paths.Paths.INTERNAL

@RestController
@RequestMapping("$INTERNAL/currencies")
class InternalCurrencyController(
    private val currencyService: CurrencyService,
) {

    @GetMapping(produces = [APPLICATION_JSON_INTERNAL_VER_1])
    fun getAvailableCurrencies(): InternalAvailableCurrenciesResponse {
        return currencyService.getAvailableCurrencies().toInternalAvailableCurrenciesResponse()
    }
}
