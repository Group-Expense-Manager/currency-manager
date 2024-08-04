package pl.edu.agh.gem.internal.job

enum class ExchangeRateJobState {
    STARTING,
    EXCHANGE_RATE,
    POLISH_EXCHANGE_RATE,
    REVERSE_POLISH_EXCHANGE_RATE,
    SAVING,
    ERROR,
}
