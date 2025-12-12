package ru.yandex.praktikumchatapp.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen

class ChatRepository(
    private val api: ChatApi = ChatApi(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getReplyMessage(): Flow<String> {
        return api.getReply()
            .retryWhen { cause, attempt ->
                if (cause is Exception) {
                    delay(1_000 * (attempt + 1))
                    true
                } else {
                    throw Exception( "Unknown error", cause)
                }
            }
            .flowOn(dispatcher)
    }
}