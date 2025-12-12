package ru.yandex.praktikumchatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.yandex.praktikumchatapp.presentation.ChatState
import ru.yandex.praktikumchatapp.presentation.ChatViewModel
import ru.yandex.praktikumchatapp.presentation.Message
import ru.yandex.praktikumchatapp.ui.theme.PraktikumChatAppTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PraktikumChatAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(R.string.app_name))
                            }
                        )
                    },
                    content = { innerPadding ->
                        val viewModel = remember { ChatViewModel() }
                        ChatScreen(
                            viewModel = viewModel,
                            onSend = viewModel::sendMyMessage,
                            onInputMessage = viewModel::onInput,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    onInputMessage: (String) -> Unit,
    onSend: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    val chatState by viewModel.chatState.collectAsState()

    when (chatState) {
        is ChatState.Content -> {
            ChatContent(
                state = chatState as ChatState.Content,
                modifier = modifier,
                focusRequester = focusRequester,
                onSend = onSend,
                onInputMessage = onInputMessage
            )
        }
    }
}

@Composable
private fun ChatContent(
    state: ChatState.Content,
    focusRequester: FocusRequester,
    onInputMessage: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        val shouldShowKeyBoard = state.shouldShowKeyboard
        LaunchedEffect(shouldShowKeyBoard) {
            if (shouldShowKeyBoard) {
                focusRequester.requestFocus()
            }
        }
        MessageList(state.messages, modifier = Modifier.weight(1f))

        MessageInput(
            messageText = state.inputMessageText,
            focusRequester = focusRequester,
            onInputMessage = onInputMessage,
            onSend = onSend ,
        )
    }
}

@Composable
private fun MessageInput(
    messageText: String,
    focusRequester: FocusRequester,
    onInputMessage: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = messageText,
            onValueChange = onInputMessage,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                .padding(10.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSend()
                }
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onSend
        ) {
            Text(stringResource(R.string.send))
        }
    }
}

@Composable
fun MessageList(messages: List<Message>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            items(items = messages, key = { it.hashCode() }) { message ->
                when (message) {
                    is Message.MyMessage -> MyMessageCard(message)
                    is Message.OtherMessage -> OtherMessageCard(message)
                }
            }
        }
    }
}

@Composable
fun MyMessageCard(message: Message.MyMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = message.text,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .background(Color.Green.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small)
                .padding(8.dp)
                .align(Alignment.CenterEnd)
        )
    }
}

@Composable
fun OtherMessageCard(message: Message.OtherMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = message.text,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .background(Color.Blue.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small)
                .padding(8.dp)
                .align(Alignment.CenterStart)
        )
    }
}