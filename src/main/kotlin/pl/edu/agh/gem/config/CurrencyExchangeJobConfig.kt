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
class CurrencyExchangeJobConfig {
    
    @Bean
    @ConditionalOnProperty(prefix = Companion.CURRENCY_EXCHANGE_PROCESSOR_PREFIX, name = ["enabled"], havingValue = "true")
    fun currencyExchangeConsumer(
        consumerExecutor: Executor,
        currencyExchangeJobFinder: CurrencyExchangeJobFinder,
        currencyExchangeJobManager: CurrencyExchangeJobManager,
    ) = CurrencyExchangeProcessor {
        val currencyExchangeJobProcessor = 
    }
    
    @Bean
    fun 
    
    @Bean
    fun jobConsumerExecutor(
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
    fun jobProducerExecutor(
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
        private const val CONSUMER_POOL = "currency-exchange-job-consumer-pool"
        private const val PRODUCER_POOL = "currency-exchange-job-producer-pool"
        private const val CURRENCY_EXCHANGE_PROCESSOR_PREFIX = "currency-exchange-job-processor"
    }
}

@ConfigurationProperties("currency-exchange-job-executor")
data class CurrencyExchangeJobExecutorProperties(
    val corePoolSize: Int,
    val maxPoolSize: Int,
    val queueCapacity: Int,
)

@ConfigurationProperties("currency-exchange-job-producer")
data class CurrencyExchangeJobProducerProperties(
    val corePoolSize: Int,
    val maxPoolSize: Int,
    val queueCapacity: Int,
)

@ConfigurationProperties("currency-exchange-job-processor")
data class ExchangeRateJobProcessorProperties(
    val lockTime: Duration,
    val emptyCandidateDelay: Duration,
    val retryDelays: List<Duration>,
)

