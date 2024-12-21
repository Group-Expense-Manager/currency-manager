package pl.edu.agh.gem.integration.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.mock.mockito.SpyBean
import pl.edu.agh.gem.config.ExchangeRateJobProcessorProperties
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.internal.persistence.ExchangeRateJobRepository
import pl.edu.agh.gem.internal.persistence.MissingExchangeRateJobException
import pl.edu.agh.gem.util.createExchangeRateJob
import java.time.Clock

class ExchangeRateJobRepositoryIT(
    @SpyBean private val clock: Clock,
    private val mongoExchangeRateJobRepository: ExchangeRateJobRepository,
    private val exchangeRateJobProcessorProperties: ExchangeRateJobProcessorProperties,
) : BaseIntegrationSpec({

        should("save and find exchange rate job by id") {
            // given
            val exchangeRateJob = createExchangeRateJob()

            // when
            val savedJob = mongoExchangeRateJobRepository.save(exchangeRateJob)

            // then
            savedJob.id shouldBe exchangeRateJob.id

            // when
            val foundJob = mongoExchangeRateJobRepository.findById(exchangeRateJob.id)

            // then
            foundJob.shouldNotBeNull()
            foundJob.id shouldBe exchangeRateJob.id
        }

        should("find and lock job to process") {
            // given
            val exchangeRateJob =
                createExchangeRateJob(
                    nextProcessAt = FIXED_TIME,
                )
            mongoExchangeRateJobRepository.save(exchangeRateJob)

            // when
            mongoExchangeRateJobRepository.findJobToProcessAndLock()
            val jobToProcess = mongoExchangeRateJobRepository.findById(exchangeRateJob.id)

            // then
            jobToProcess.shouldNotBeNull()
            jobToProcess.id shouldBe exchangeRateJob.id
            jobToProcess.nextProcessAt shouldBe FIXED_TIME.plus(exchangeRateJobProcessorProperties.lockTime)
        }

        should("update nextProcessAt and retry count") {
            // given
            val exchangeRateJob =
                createExchangeRateJob(
                    nextProcessAt = FIXED_TIME,
                    retry = 0L,
                )
            mongoExchangeRateJobRepository.save(exchangeRateJob)

            // when
            val updatedJob = mongoExchangeRateJobRepository.updateNextProcessAtAndRetry(exchangeRateJob)

            // then
            updatedJob.shouldNotBeNull()
            updatedJob.nextProcessAt shouldBe FIXED_TIME.plus(exchangeRateJobProcessorProperties.retryDelays.first())
            updatedJob.retry shouldBe 1
        }

        should("remove exchange rate job") {
            // given
            val exchangeRateJob = createExchangeRateJob()
            mongoExchangeRateJobRepository.save(exchangeRateJob)

            // when
            mongoExchangeRateJobRepository.remove(exchangeRateJob)

            // then
            val foundJob = mongoExchangeRateJobRepository.findById(exchangeRateJob.id)
            foundJob.shouldBeNull()
        }

        should("throw MissingExchangeRateJobException when updating non-existing job") {
            // given
            val nonExistingJob = createExchangeRateJob(id = "non-existing-id", nextProcessAt = FIXED_TIME, retry = 0L)

            // when & then
            shouldThrow<MissingExchangeRateJobException> {
                mongoExchangeRateJobRepository.updateNextProcessAtAndRetry(nonExistingJob)
            }
        }

        should("return null when finding non-existing job by id") {
            // given
            val nonExistingJobId = "non-existing-id"

            // when
            val foundJob = mongoExchangeRateJobRepository.findById(nonExistingJobId)

            // then
            foundJob.shouldBeNull()
        }

        should("return null when no job to process is found") {
            // given
            val exchangeRateJob =
                createExchangeRateJob(
                    nextProcessAt = FIXED_TIME.plusSeconds(3600),
                )
            mongoExchangeRateJobRepository.save(exchangeRateJob)

            // when
            val jobToProcess = mongoExchangeRateJobRepository.findJobToProcessAndLock()

            // then
            jobToProcess.shouldBeNull()
        }
    })
