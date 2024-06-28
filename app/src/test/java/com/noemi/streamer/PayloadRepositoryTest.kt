package com.noemi.streamer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.noemi.streamer.model.PayloadData
import com.noemi.streamer.repository.PayloadRepository
import com.noemi.streamer.repository.PayloadRepositoryImpl
import com.noemi.streamer.room.PayloadDAO
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class PayloadRepositoryTest {

    private val payloadDAO: PayloadDAO = mockk()

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: PayloadRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = PayloadRepositoryImpl(payloadDAO = payloadDAO)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `delete payload should be successful`() = runTest {
        val payload = mockk<PayloadData>()
        val id = 12L
        coEvery { payload.id } returns id
        coEvery { payloadDAO.deletePayload(id) } just runs

        repository.deletePayloadData(id)

        coVerify { payloadDAO.deletePayload(id) }
    }

    @Test
    fun `observe payloads returns a flow list of payloads`() = runTest {
        val payload = mockk<PayloadData>()

        coEvery { payloadDAO.observePayloads() } returns flowOf(listOf(payload))

        repository.observePayloads().test {
            assertThat(awaitItem()).isEqualTo(listOf(payload))
            cancelAndConsumeRemainingEvents()
        }

        coVerify { payloadDAO.observePayloads() }
    }

    @Test
    fun `insert payload should be successful`() = runTest {
        val payload = mockk<PayloadData>()

        coEvery { payloadDAO.insertPayload(payload) } just runs

        repository.savePayload(payload)

        coVerify { payloadDAO.insertPayload(payload) }
    }

    @Test
    fun `clear database should be successful`() = runTest {

        coEvery { payloadDAO.deleteAll()} just runs

        repository.clearDataBase()

        coVerify { payloadDAO.deleteAll() }
    }
}