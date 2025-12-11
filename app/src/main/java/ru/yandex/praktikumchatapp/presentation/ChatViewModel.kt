package ru.yandex.praktikumchatapp.presentation

import androidx.compose.ui.graphics.vector.Path
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.yandex.praktikumchatapp.data.ChatRepository

class ChatViewModel(
    val isWithReplies: Boolean = true
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
        viewModelScope.launch {
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
            _chatState.value = currentState.copy(
                messages = currentMessages,
                shouldShowKeyboard = currentMessages.size == 1
            )
        }
    }

    fun sendMyMessage() {
        val curState = chatState.value
        if (curState is ChatState.Content) {
            val myMessage = curState.inputMessageText
            if (myMessage.isNotBlank()) {
                val currentMessage = Message.MyMessage(myMessage)
                updateMessageList(currentMessage)
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