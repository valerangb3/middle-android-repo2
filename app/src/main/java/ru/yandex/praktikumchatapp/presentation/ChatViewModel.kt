package ru.yandex.praktikumchatapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.yandex.praktikumchatapp.data.ChatRepository

class ChatViewModel(
    private val isWithReplies: Boolean = true,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val repository = ChatRepository()
    private val _chatState = MutableStateFlow<ChatState>(
        ChatState.Content(
            messages = emptyList(),
            shouldShowKeyboard = false
        )
    )
    val chatState = _chatState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            while (isWithReplies) {
                repository.getReplyMessage().collect { response ->
                    val currentMessage = Message.OtherMessage(response)
                    updateMessageList(currentMessage)
                }
            }
        }
    }

    private fun updateMessageList(message: Message) {
        val currentState = _chatState.value
        if (currentState is ChatState.Content) {
            var currentMessages = currentState.messages
            currentMessages = currentMessages + message
            _chatState.update { currentState ->
                if (currentState is ChatState.Content) {
                    currentState.copy(
                        messages = currentMessages,
                        shouldShowKeyboard = currentMessages.size == 1
                    )
                } else {
                    currentState
                }
            }
        }
    }

    fun sendMyMessage(message: String = "") {
        viewModelScope.launch(dispatcher) {
            val curState = chatState.value
            if (curState is ChatState.Content) {
                val myMessage = message.ifBlank { curState.inputMessageText }
                if (myMessage.isNotBlank()) {
                    val currentMessage = Message.MyMessage(myMessage)
                    updateMessageList(currentMessage)
                }
            }
        }
    }

    fun onInput(messageText: String) {
        _chatState.update { currentState ->
            if (currentState is ChatState.Content) {
                currentState.copy(inputMessageText = messageText)
            } else {
                currentState
            }
        }
    }
}