import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.yandex.praktikumchatapp.presentation.ChatState
import ru.yandex.praktikumchatapp.presentation.ChatViewModel
import ru.yandex.praktikumchatapp.presentation.Message

@ExperimentalCoroutinesApi
class ChatViewModelTest {
    private var testDispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ChatViewModel(isWithReplies = false)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `send message should update state with MyMessage`() = runTest(testDispatcher) {
        val message = Message.MyMessage("TestMessage")
        val viewModel = ChatViewModel(
            isWithReplies = false,
            dispatcher = testDispatcher
        )
        viewModel.sendMyMessage(message.text)
        advanceUntilIdle()
        val expect = ChatState.Content(
            messages = emptyList(),
            inputMessageText = message.text,
        )
        val state = viewModel.chatState.value as ChatState.Content
        val firstMessage = state.messages.first()
        val actual = firstMessage as Message.MyMessage
        assert(actual.text == expect.inputMessageText)
    }

    @Test
    fun testReceiveMessage_concurrentMessages() = runTest(testDispatcher) {
        val viewModel = ChatViewModel(
            isWithReplies = false,
            dispatcher = testDispatcher
        )
        val messagesToSend = (1..100).map { Message.MyMessage("Message $it") }
        val expectMessagesText = messagesToSend.map { it.text }
        val jobs = expectMessagesText.map { message ->
            launch {
                viewModel.sendMyMessage(message)
            }
        }
        jobs.joinAll()
        advanceUntilIdle()
        val actualMessages = (viewModel.chatState.value as ChatState.Content).messages
        val actualMessagesText = actualMessages.map { (it as Message.MyMessage).text }
        assert(expectMessagesText == actualMessagesText)
    }
}