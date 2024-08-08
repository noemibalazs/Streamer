package com.noemi.streamer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.noemi.streamer.service.PayloadService
import com.noemi.streamer.model.Event
import com.noemi.streamer.model.EventType
import com.noemi.streamer.model.PayloadData
import com.noemi.streamer.repository.PayloadRepository
import com.noemi.streamer.screen.MainViewModel
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class MainViewModelTest {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private val repository: PayloadRepository = mockk()
    private val payloadService: PayloadService = mockk()
    private lateinit var viewModel: MainViewModel
    private val query = "media"
    private val event: Event = mockk()
    private val payload: PayloadData = mockk()


    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = MainViewModel(repository, payloadService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test fetch public timelines should be successful`() = runBlocking {

        val job = launch {
            assertThat(viewModel.loadingState.value).isTrue()
            assertThat(viewModel.payloadsState.value.isEmpty()).isTrue()

            coEvery { repository.clearDataBase() } just runs

            viewModel.payloadsState.test {
                val result = awaitItem()

                payloadService.observePayloads(query).test {
                    val event = awaitItem()
                    assertThat(result).isEqualTo(event.payload)
                }

                cancelAndConsumeRemainingEvents()
            }

            coVerify { repository.clearDataBase() }
        }

        viewModel.fetchPublicTimelines(query)

        job.cancelAndJoin()
    }

    @Test
    fun `test fetch public timelines should throw error`() = runBlocking {

        val job = launch {
            assertThat(viewModel.loadingState.value).isTrue()
            assertThat(viewModel.payloadsState.value.isEmpty()).isTrue()

            coEvery { repository.clearDataBase() } just runs

            viewModel.payloadsState.test {
                awaitComplete()

                payloadService.observePayloads(query).test {
                    val error = awaitError()
                    assertThat(viewModel.loadingState.value).isFalse()
                    assertThat(viewModel.errorState.value).isEqualTo(error.message)
                }

                cancelAndConsumeRemainingEvents()
            }

            coVerify { repository.clearDataBase() }
        }

        viewModel.fetchPublicTimelines(query)

        job.cancelAndJoin()
    }

    @Test
    fun `test re fetch public timelines should be successful`() = runBlocking {
        viewModel.searchTerm = query

        val job = launch {
            assertThat(viewModel.errorState.value).isEqualTo("")

            viewModel.payloadsState.test {
                val result = awaitItem()

                payloadService.observePayloads(query).test {
                    val event = awaitItem()
                    assertThat(result).isEqualTo(event.payload)
                }

                cancelAndConsumeRemainingEvents()
            }
        }

        viewModel.reFetchPublicTimelines()

        job.cancelAndJoin()
    }

    @Test
    fun `test re fetch public timelines should throw error`() = runBlocking {
        viewModel.searchTerm = query

        val job = launch {
            assertThat(viewModel.errorState.value).isEqualTo("")

            viewModel.payloadsState.test {
                awaitComplete()

                payloadService.observePayloads(query).test {
                    val error = awaitError()
                    assertThat(viewModel.errorState.value).isEqualTo(error.message)
                }

                cancelAndConsumeRemainingEvents()
            }
        }

        viewModel.reFetchPublicTimelines()

        job.cancelAndJoin()
    }

    @Test
    fun `test handle event response when event is delete`() = runBlocking {
        val job = launch {

            assertThat(viewModel.loadingState.value).isFalse()
            assertThat(event.type).isEqualTo(EventType.DELETE)

            coEvery { event.type } returns EventType.DELETE

            viewModel.payloadsState.test {
                val result = awaitItem()

                repository.observePayloads().test {
                    val payloads = awaitItem()
                    assertThat(payloads).contains(payload)

                    coEvery { repository.deletePayloadData(payload.id) } just runs

                    coVerify { repository.deletePayloadData(payload.id) }

                    assertThat(result.size).isEqualTo(payloads.size.minus(1))
                }

                cancelAndConsumeRemainingEvents()
            }

            coVerify { event.type }
        }

        viewModel.handleEventResponse(event)

        job.cancelAndJoin()
    }

    @Test
    fun `test handle event response when event is update`() = runBlocking {
        val job = launch {

            assertThat(viewModel.loadingState.value).isFalse()
            assertThat(event.type).isEqualTo(EventType.UPDATE)

            coEvery { event.type } returns EventType.UPDATE

            viewModel.payloadsState.test {
                val result = awaitItem()

                coEvery { repository.savePayload(payload) } just runs

                repository.observePayloads().test {
                    val payloads = awaitItem()
                    assertThat(payloads.size).isEqualTo(result.size)
                    assertThat(result).contains(payload)
                }

                coVerify { repository.savePayload(payload) }

                cancelAndConsumeRemainingEvents()
            }

            coVerify { event.type }
        }

        viewModel.handleEventResponse(event)

        job.cancelAndJoin()
    }

    @Test
    fun `test publish payloads should be successful`() = runBlocking {
        val job = launch {

            viewModel.payloadsState.test {
                val result = awaitItem()

                repository.observePayloads().test {
                    val payloads = awaitItem()
                    assertThat(result).isEqualTo(payloads)
                }
            }
        }

        viewModel.publishPayloads()

        job.cancelAndJoin()
    }
}