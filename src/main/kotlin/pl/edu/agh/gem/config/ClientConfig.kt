package pl.edu.agh.gem.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.internal.client.ExchangeRateTable
import java.time.Duration

@Configuration
class ClientConfig {
    @Bean
    @Qualifier("NBPRestTemplate")
    fun attachmentStoreRestTemplate(attachmentStoreProperties: NBPProperties): RestTemplate {
        return RestTemplateBuilder()
            .setConnectTimeout(attachmentStoreProperties.connectTimeout)
            .setReadTimeout(attachmentStoreProperties.readTimeout)
            .build()
    }
}

@ConfigurationProperties(prefix = "nbp")
data class NBPProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
    val table: Map<ExchangeRateTable, TableProperties>,
)

data class TableProperties(
    val validDuration: Duration,
    val currencies: Set<String>,
)
