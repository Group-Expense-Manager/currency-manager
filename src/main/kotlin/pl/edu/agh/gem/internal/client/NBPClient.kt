package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.ExchangeRate
import java.time.LocalDate

interface NBPClient {
    fun getPolishExchangeRate(currency: String,date: LocalDate) : ExchangeRate
}
