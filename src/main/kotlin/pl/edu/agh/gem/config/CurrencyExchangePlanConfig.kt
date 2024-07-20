package pl.edu.agh.gem.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.edu.agh.gem.threads.ExecutorConfig
import pl.edu.agh.gem.threads.ExecutorFactory
import java.util.concurrent.Executor
import java.time.Duration

@Configuration
class CurrencyExchangePlanConfig {
    
    @Bean
    @ConditionalOnProperty(prefix = Companion.CURRENCY_EXCHANGE_PROCESSOR_PREFIX, name = ["enabled"], havingValue = "true")
    fun currencyExchangePlanConsumer(
        consumerExecutor: Executor,
        currencyExchangeJobFinder: CurrencyExchangeJobFinder,
        currencyExchangeJobManager: CurrencyExchangeJobManager,
    ) = CurrencyExchangeProcessor {
        val currencyExchangeJobProcessor = 
    }
    
    @Bean
    fun 
    
    @Bean
    fun planConsumerExecutor(
        executorFactory: ExecutorFactory,
        settings: CurrencyExchangeJobExecutorProperties,
    ): Executor {
        val config = ExecutorConfig(
            corePoolSize = settings.corePoolSize,
            maxPoolSize = settings.maxPoolSize,
            taskQueueSize = settings.queueCapacity,
            threadPoolName = CONSUMER_POOL,
        )
        return executorFactory.createExecutor(config)
    }
    
    @Bean
    fun planProducerExecutor(
        executorFactory: ExecutorFactory,
        settings: CurrencyExchangeJobProducerProperties,
    ): Executor {
        val config = ExecutorConfig(
            corePoolSize = settings.corePoolSize,
            maxPoolSize = settings.maxPoolSize,
            taskQueueSize = settings.queueCapacity,
            threadPoolName = PRODUCER_POOL,
        )
        return executorFactory.createExecutor(config)
    }
    
    companion object {
        private const val CONSUMER_POOL = "currency-exchange-plan-consumer-pool"
        private const val PRODUCER_POOL = "currency-exchange-plan-producer-pool"
        private const val CURRENCY_EXCHANGE_PROCESSOR_PREFIX = "currency-exchange-plan-processor"
    }
}

@ConfigurationProperties("currency-exchange-plan-executor")
data class CurrencyExchangePlanExecutorProperties(
    val corePoolSize: Int,
    val maxPoolSize: Int,
    val queueCapacity: Int,
)

@ConfigurationProperties("currency-exchange-plan-producer")
data class CurrencyExchangePlanProducerProperties(
    val corePoolSize: Int,
    val maxPoolSize: Int,
    val queueCapacity: Int,
)

@ConfigurationProperties("currency-exchange-plan-processor")
data class ExchangeRatePlanProcessorProperties(
    val lockTime: Duration,
    val emptyCandidateDelay: Duration,
    val retryDelay: Duration,
    val nextTimeFromMidnight: Duration,
)


