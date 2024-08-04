package pl.edu.agh.gem.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "available-currencies")
data class AvailableCurrenciesProperties(
    val codes: List<String>,
)

@ConfigurationProperties(prefix = "exchange-rates")
data class ExchangeRatesProperties(
    val validDuration: Duration,
)
