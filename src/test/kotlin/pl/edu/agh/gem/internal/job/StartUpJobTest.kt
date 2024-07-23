package pl.edu.agh.gem.internal.job

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.config.AvailableCurrenciesProperties
import pl.edu.agh.gem.internal.persistence.ExchangeRatePlanRepository
import java.time.Clock
import java.time.Instant

class StartUpJobTest : ShouldSpec({

    val availableCurrenciesProperties = mock<AvailableCurrenciesProperties>()
    val exchangeRatePlanRepository = mock<ExchangeRatePlanRepository>()
    val clock = mock<Clock>()
    val startUpJob = StartUpJob(availableCurrenciesProperties, exchangeRatePlanRepository, clock)

    val currencyCodes = listOf("USD", "EUR", "PLN")
    val allowedCurrencyPairs = listOf(
        "USD" to "EUR",
        "USD" to "PLN",
        "EUR" to "USD",
        "EUR" to "PLN",
        "PLN" to "USD",
        "PLN" to "EUR",
    )

    should("insert all allowed plans if they do not exist") {
        // given
        whenever(availableCurrenciesProperties.codes).thenReturn(currencyCodes)
        whenever(exchangeRatePlanRepository.get(any(), any())).thenReturn(null)
        whenever(clock.instant()).thenReturn(Instant.now())
        whenever(clock.zone).thenReturn(Clock.systemUTC().zone)

        // when
        startUpJob.run(null)

        // then
        verify(exchangeRatePlanRepository, times(allowedCurrencyPairs.size)).insert(any())
    }

    should("delete all not allowed plans") {
        // given
        whenever(availableCurrenciesProperties.codes).thenReturn(currencyCodes)
        whenever(clock.instant()).thenReturn(Instant.now())
        whenever(clock.zone).thenReturn(Clock.systemUTC().zone)

        // when
        startUpJob.run(null)

        // then
        verify(exchangeRatePlanRepository).deleteNotAllowed(allowedCurrencyPairs)
    }
},)
