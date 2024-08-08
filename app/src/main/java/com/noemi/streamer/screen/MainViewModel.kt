package com.noemi.streamer.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noemi.streamer.service.PayloadService
import com.noemi.streamer.model.Event
import com.noemi.streamer.model.EventType
import com.noemi.streamer.model.PayloadData
import com.noemi.streamer.repository.PayloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PayloadRepository,
    private val payloadService: PayloadService
) : ViewModel() {

    private var _payloadsState = MutableStateFlow(emptyList<PayloadData>())
    val payloadsState = _payloadsState.asStateFlow()

    private var _loadingState = MutableStateFlow(false)
    val loadingState = _loadingState.asStateFlow()

    private var _errorState = MutableStateFlow("")
    val errorState = _errorState.asStateFlow()

    private var _networkState = MutableStateFlow(false)
    val networkState = _networkState.asStateFlow()

    var searchTerm by mutableStateOf("")

    fun publishPayloads() {
        viewModelScope.launch {
            repository.observePayloads().collect { payloads ->
                val sortedPayload = payloads.sortedByDescending { it.id }
                _payloadsState.emit(sortedPayload).also {
                    Timber.d("Publish payloads - ${payloads.size}")
                }
            }
        }
    }

    fun fetchPublicTimelines(query: String) {
        viewModelScope.launch {

            _loadingState.emit(true)
            _payloadsState.emit(emptyList())

            repository.clearDataBase()

            payloadService.observePayloads(query)
                .catch {
                    _errorState.emit(it.message ?: "Error while fetching events")
                    _loadingState.emit(false)
                }
                .collectLatest {
                    handleEventResponse(it)
                    Timber.d("Event data: $it")
                }
        }
    }

    fun reFetchPublicTimelines() {
        viewModelScope.launch {
            _errorState.emit("")

            payloadService.observePayloads(searchTerm)
                .catch {
                    _errorState.emit(it.message ?: "Error while fetching events")
                }
                .collectLatest {
                    handleEventResponse(it)
                    Timber.d("Event data: $it")
                }
        }
    }

    fun handleEventResponse(event: Event) {
        viewModelScope.launch {

            _loadingState.emit(false)

            when (event.type == EventType.DELETE) {
                true -> repository.observePayloads().collect { payloads ->
                    payloads.find { payload -> payload.id == event.id }?.let { payload ->
                        repository.deletePayloadData(payload.id)
                        Timber.d("On Event Received! Stream payload -: $payload")
                    }
                }

                false -> event.payload?.let { payload ->
                    repository.savePayload(payload)
                    Timber.d("On Event Received! Stream payload -: $payload")
                }
            }
        }
    }

    fun onSearchTermChanged(query: String) {
        searchTerm = query
    }

    fun onNetworkStateChanged(isActive: Boolean) {
        viewModelScope.launch {
            _networkState.emit(isActive)
        }
    }
}