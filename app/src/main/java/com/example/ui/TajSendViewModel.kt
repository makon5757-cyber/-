package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FileRepository
import com.example.data.HistoryRepository
import com.example.model.ConnectionPayload
import com.example.model.FileCategory
import com.example.model.HistoryItem
import com.example.model.LocalFileItem
import com.example.model.TransferProgress
import com.example.model.TransferStatus
import com.example.network.NetworkUtils
import com.example.network.TajSendClient
import com.example.network.TajSendServer
import com.example.qrcode.QRCodeGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ScreenState {
    DASHBOARD,
    SEND,
    RECEIVE,
    HISTORY,
    INSTRUCTIONS
}

class TajSendViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    val fileRepository = FileRepository(context)
    val historyRepository = HistoryRepository(context)

    private val _currentScreen = MutableStateFlow(ScreenState.DASHBOARD)
    val currentScreen = _currentScreen.asStateFlow()

    // File selection state
    private val _selectedCategory = MutableStateFlow(FileCategory.ALL)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _availableFiles = MutableStateFlow<List<LocalFileItem>>(emptyList())
    val availableFiles = _availableFiles.asStateFlow()

    private val _selectedFiles = MutableStateFlow<List<LocalFileItem>>(emptyList())
    val selectedFiles = _selectedFiles.asStateFlow()

    private val _isLoadingFiles = MutableStateFlow(false)
    val isLoadingFiles = _isLoadingFiles.asStateFlow()

    // Receiver state
    private var server: TajSendServer? = null
    private val _receiverPayload = MutableStateFlow<ConnectionPayload?>(null)
    val receiverPayload = _receiverPayload.asStateFlow()

    private val _receiverQrBitmap = MutableStateFlow<Bitmap?>(null)
    val receiverQrBitmap = _receiverQrBitmap.asStateFlow()

    private val _receiverProgress = MutableStateFlow(TransferProgress(status = TransferStatus.IDLE))
    val receiverProgress = _receiverProgress.asStateFlow()

    // Sender state
    private var client: TajSendClient? = null
    private val _senderProgress = MutableStateFlow(TransferProgress(status = TransferStatus.IDLE))
    val senderProgress = _senderProgress.asStateFlow()

    // Scanner modal & connection state
    private val _isScannerOpen = MutableStateFlow(false)
    val isScannerOpen = _isScannerOpen.asStateFlow()

    private val _connectedPeerName = MutableStateFlow<String?>(null)
    val connectedPeerName = _connectedPeerName.asStateFlow()

    private val _lastReceivedFile = MutableStateFlow<HistoryItem?>(null)
    val lastReceivedFile = _lastReceivedFile.asStateFlow()

    init {
        loadCategoryFiles(FileCategory.ALL)
    }

    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
        if (screen == ScreenState.RECEIVE && server == null) {
            startReceiver()
        }
    }

    fun setCategory(category: FileCategory) {
        _selectedCategory.value = category
        loadCategoryFiles(category)
    }

    fun loadCategoryFiles(category: FileCategory) {
        viewModelScope.launch {
            _isLoadingFiles.value = true
            val files = fileRepository.loadDeviceFiles(category)
            val selectedIds = _selectedFiles.value.map { it.id }.toSet()
            _availableFiles.value = files.map { file ->
                file.copy(isSelected = selectedIds.contains(file.id))
            }
            _isLoadingFiles.value = false
        }
    }

    fun toggleFileSelection(item: LocalFileItem) {
        val currentSelected = _selectedFiles.value.toMutableList()
        val existingIndex = currentSelected.indexOfFirst { it.id == item.id }

        if (existingIndex >= 0) {
            currentSelected.removeAt(existingIndex)
        } else {
            currentSelected.add(item.copy(isSelected = true))
        }
        _selectedFiles.value = currentSelected

        val selectedIds = currentSelected.map { it.id }.toSet()
        _availableFiles.value = _availableFiles.value.map { file ->
            file.copy(isSelected = selectedIds.contains(file.id))
        }
    }

    fun addCustomUris(uris: List<Uri>) {
        val newItems = uris.map { uri ->
            fileRepository.parseSelectedUri(uri)
        }
        val currentSelected = _selectedFiles.value.toMutableList()
        for (item in newItems) {
            if (currentSelected.none { it.uri == item.uri }) {
                currentSelected.add(item)
            }
        }
        _selectedFiles.value = currentSelected
    }

    fun clearSelection() {
        _selectedFiles.value = emptyList()
        _availableFiles.value = _availableFiles.value.map { it.copy(isSelected = false) }
    }

    fun openScanner() {
        _isScannerOpen.value = true
    }

    fun closeScanner() {
        _isScannerOpen.value = false
    }

    fun startReceiver() {
        stopReceiver()
        val ip = NetworkUtils.getLocalIpAddress()
        val port = 8888
        val pin = NetworkUtils.generatePin()
        val deviceName = NetworkUtils.getDeviceName()

        val payload = ConnectionPayload(
            ip = ip,
            port = port,
            deviceName = deviceName,
            pinToken = pin
        )
        _receiverPayload.value = payload

        // Generate QR Code
        val qrString = payload.toQrString()
        _receiverQrBitmap.value = QRCodeGenerator.generateQRCode(
            content = qrString,
            width = 600,
            height = 600
        )

        server = TajSendServer(
            context = context,
            port = port,
            expectedPin = pin,
            deviceName = deviceName,
            onFileReceived = { historyItem ->
                historyRepository.addHistoryItem(historyItem)
                _lastReceivedFile.value = historyItem
            }
        ).apply {
            start()
        }

        viewModelScope.launch {
            server?.progress?.collectLatest { p ->
                _receiverProgress.value = p
            }
        }
    }

    fun stopReceiver() {
        server?.stop()
        server = null
        _receiverProgress.value = TransferProgress(status = TransferStatus.IDLE)
    }

    fun connectAndSend(payload: ConnectionPayload) {
        val filesToSend = _selectedFiles.value
        if (filesToSend.isEmpty()) return

        _connectedPeerName.value = payload.deviceName
        _isScannerOpen.value = false

        client = TajSendClient(
            context = context,
            onFileSent = { historyItem ->
                historyRepository.addHistoryItem(historyItem)
            }
        )

        viewModelScope.launch {
            client?.progress?.collectLatest { p ->
                _senderProgress.value = p
            }
        }

        viewModelScope.launch {
            val success = client?.sendFiles(
                targetIp = payload.ip,
                targetPort = payload.port,
                pin = payload.pinToken,
                files = filesToSend,
                senderDeviceName = NetworkUtils.getDeviceName()
            )
            if (success == true) {
                // Keep selected or clear
            }
        }
    }

    fun cancelSending() {
        client?.cancel()
    }

    fun resetSending() {
        client?.reset()
        _senderProgress.value = TransferProgress(status = TransferStatus.IDLE)
    }

    override fun onCleared() {
        super.onCleared()
        stopReceiver()
        client?.cancel()
    }
}
