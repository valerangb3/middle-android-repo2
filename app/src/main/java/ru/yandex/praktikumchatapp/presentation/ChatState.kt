package ru.yandex.praktikumchatapp.presentation

sealed interface ChatState {
    data class Content(
        val messages: List<Message>,
        val shouldShowKeyboard: Boolean = false,
        val inputMessageText: String = ""
    ) : ChatState
}