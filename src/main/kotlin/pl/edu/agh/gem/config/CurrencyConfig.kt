package pl.edu.agh.gem.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
class CurrencyConfig

@ConfigurationProperties(prefix = "available-currencies")
data class AvailableCurrenciesProperties(
    val codes: List<String>,
)
