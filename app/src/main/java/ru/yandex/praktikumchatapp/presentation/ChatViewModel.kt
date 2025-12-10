package ru.yandex.praktikumchatapp.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.yandex.praktikumchatapp.data.ChatRepository

class ChatViewModel(
    val isWithReplies: Boolean = true
) : ViewModel() {

    private val repository = ChatRepository()
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()
    
    private val _shouldShowKeyboard = MutableStateFlow(false)
    val shouldShowKeyboard = _shouldShowKeyboard.asStateFlow()

    private val _chatState = MutableStateFlow<ChatState>(ChatState.Content(
        messages = emptyList(),
        shouldShowKeyboard = false
    ))
    val chatState = _chatState.asStateFlow()

    // TODO Задание 4: замените messages и shouldShowKeyboard на state

    init {
        viewModelScope.launch {
            while (isWithReplies) {
                repository.getReplyMessage().collect { response ->

                    /*
                    val currentMessages = _messages.value ?: emptyList()
                    val result = currentMessages + Message.OtherMessage(response)
                    _shouldShowKeyboard.value = result.size == 1
                    _messages.value = result
                    */

                    val currentState = _chatState.value
                    val currentMessage = Message.OtherMessage(response)
                    var currentMessages = emptyList<Message>()
                    if (currentState is ChatState.Content) {
                        currentMessages = currentState.messages
                        currentMessages = currentMessages + currentMessage
                        _chatState.value = currentState.copy(
                            messages = currentMessages,
                            shouldShowKeyboard = currentMessages.size == 1
                        )
                    }
                }
            }
        }
    }

    fun sendMyMessage(messageText: String) {
        val currentMessages = _messages.value
        _messages.value = currentMessages + Message.MyMessage(messageText)
    }
}