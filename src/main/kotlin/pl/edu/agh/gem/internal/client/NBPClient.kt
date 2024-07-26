package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.ExchangeRate
import java.time.Instant

interface NBPClient {
    fun getPolishExchangeRate(currency: String, date: Instant): ExchangeRate
}

class IncorrectCurrencyException(currency: String) : RuntimeException("Currency $currency is not supported by NBP")

class MissingBodyFromNBPException : RuntimeException("No response body from NBP")

class NBPClientException(override val message: String?) : RuntimeException()

class RetryableNBPClientException(override val message: String?) : RuntimeException()