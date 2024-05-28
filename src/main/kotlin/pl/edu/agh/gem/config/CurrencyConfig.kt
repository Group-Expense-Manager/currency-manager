package pl.edu.agh.gem.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
class CurrencyConfig

@ConfigurationProperties(prefix = "available-currencies")
data class AvailableCurrenciesProperties(
    val codes: List<String>,
)

@ConfigurationProperties(prefix = "exchange-rates")
data class ExchangeRatesProperties(
    val rates: List<StaticExchangeRate>,
)

data class StaticExchangeRate(
    val baseCurrency: String,
    val targetCurrency: String,
    val rate: String,
)
