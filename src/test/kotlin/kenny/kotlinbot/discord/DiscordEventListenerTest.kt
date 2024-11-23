package kenny.kotlinbot.discord

import kenny.kotlinbot.ai.ChatService
import kenny.kotlinbot.ai.ImageResult
import kenny.kotlinbot.ai.ImageService
import kenny.kotlinbot.storage.ImageStorageService
import kenny.kotlinbot.storage.StoredImageResult
import kenny.kotlinbot.storage.StoredImageResult.MetaData
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.utils.FileUpload
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.anyString
import org.mockito.Mockito.startsWith
import org.mockito.kotlin.*
import java.io.InputStream
import java.util.function.Consumer
import kotlin.test.Test

class DiscordEventListenerTest {
    private lateinit var eventListener: DiscordEventListener
    private lateinit var spyEventListener: DiscordEventListener

    private val chatService: ChatService = mock()
    private val imageService: ImageService = mock()
    private val storageService: ImageStorageService = mock()

    private val properties = DiscordProperties().apply {
        token = "123456abc"
    }

    @BeforeEach
    fun setUp() {
        eventListener = DiscordEventListener(chatService, imageService, storageService, properties)
        spyEventListener = spy(eventListener)
    }

    @Test
    fun `onMessageReceived should ignore messages from bots`() {
        val event = mock<MessageReceivedEvent> { on { isFromGuild } doReturn false }
        val author = mock<User> { on { isBot } doReturn true }
        whenever(event.author).thenReturn(author)

        eventListener.onMessageReceived(event)

        // Nothing goes out
        verifyNoMoreInteractions(chatService, imageService, storageService)
    }

    @Test
    fun `onMessageReceived should ignore slash commands`() {
        val message = mock<Message> { on { contentRaw } doReturn "/command" }
        val author = mock<User> { on { isBot } doReturn true }
        val event = mock<MessageReceivedEvent> { on { isFromGuild } doReturn false }

        whenever(event.message).thenReturn(message)
        whenever(event.author).thenReturn(author)

        eventListener.onMessageReceived(event)

        // Nothing goes out
        verifyNoMoreInteractions(chatService, imageService, storageService)
    }

    @Test
    fun `onMessageReceived should process direct messages and reply`() {
        val event = mock<MessageReceivedEvent> {
            on { isFromGuild } doReturn false
        }
        val author = mock<User> {
            on { isBot } doReturn false
            on { isSystem } doReturn false
            on { globalName } doReturn "TestUser"
        }
        val message = mock<Message> {
            on { contentRaw } doReturn "Hello, bot!"
        }
        val channel = mock<MessageChannelUnion>()
        val messageCreateAction = mock<MessageCreateAction>()

        whenever(event.author).thenReturn(author)
        whenever(event.message).thenReturn(message)
        whenever(event.channel).thenReturn(channel)

        whenever(channel.sendMessage("Hi there!")).thenReturn(messageCreateAction)
        whenever(chatService.chat("Hello, bot!", "TestUser")).thenReturn("Hi there!")

        eventListener.onMessageReceived(event)

        // Query goes out to AI, response goes out to Discord
        verify(chatService).chat("Hello, bot!", "TestUser")
        verify(channel).sendMessage("Hi there!")
    }

    @Test
    fun `getCommandResponse should recognize summary command`() {
        val event = mock<SlashCommandInteractionEvent> {
            on { name } doReturn "summary"
        }

        spyEventListener.getCommandResponse(event, "TestUser", mock<MessageChannelUnion>())

        verify(spyEventListener).summarize(eq("TestUser"))
    }

    @Test
    fun `getCommandResponse should recognize forget command`() {
        val event = mock<SlashCommandInteractionEvent> {
            on { name } doReturn "forget"
        }

        spyEventListener.getCommandResponse(event, "TestUser", mock<MessageChannelUnion>())

        verify(spyEventListener).forget(eq("TestUser"))
    }

    @Test
    fun `getCommandResponse should recognize role command`() {
        val event = mock<SlashCommandInteractionEvent> {
            on { name } doReturn "role"
        }
        val option = mock<OptionMapping> {
            on { asString } doReturn "test role"
        }

        whenever(event.getOption("role")).thenReturn(option)

        spyEventListener.getCommandResponse(event, "TestUser", mock())
        verify(spyEventListener).role(eq("test role"))
    }

    @Test
    fun `getCommandResponse should recognize image command`() {
        val event = mock<SlashCommandInteractionEvent> {
            on { name } doReturn "image"
        }

        val option = mock<OptionMapping> {
            on { asString } doReturn "image prompt"
        }
        val channel = mock<MessageChannelUnion> {
            on { sendMessage(anyString()) } doReturn mock()
        }

        whenever(event.getOption("prompt")).thenReturn(option)
        whenever(imageService.image(any(), eq("TestUser"))).thenReturn(ImageResult("url", "prompt", "revisedPrompt"))

        spyEventListener.getCommandResponse(event, "TestUser", channel)

        verify(spyEventListener).image(eq("image prompt"), eq("TestUser"), any())
    }

    @Test
    fun `getCommandResponse should recognize chat command`() {
        val event = mock<SlashCommandInteractionEvent> {
            on { name } doReturn "prompt"

        }
        val option = mock<OptionMapping> {
            on { asString } doReturn "chat prompt"
        }

        whenever(event.getOption("prompt")).thenReturn(option)

        spyEventListener.getCommandResponse(event, "TestUser", mock<MessageChannelUnion>())

        verify(spyEventListener).chat(eq("chat prompt"), eq("TestUser"))
    }

    @Test
    fun `getCommandResponse should recognize temp command`() {
        val event = mock<SlashCommandInteractionEvent> {
            on { name } doReturn "temp"
        }

        val option = mock<OptionMapping> {
            on { asDouble } doReturn 0.5
        }

        whenever(event.getOption("temp")).thenReturn(option)

        spyEventListener.getCommandResponse(event, "TestUser", mock<MessageChannelUnion>())

        verify(spyEventListener).temperature(eq(0.5))
    }

    @Test
    fun `getCommandResponse should recognize tokens command`() {
        val event = mock<SlashCommandInteractionEvent> {
            on { name } doReturn "tokens"
        }
        val option = mock<OptionMapping> {
            on { asInt } doReturn 100
        }

        whenever(event.getOption("tokens")).thenReturn(option)

        spyEventListener.getCommandResponse(event, "TestUser", mock<MessageChannelUnion>())

        verify(spyEventListener).tokens(eq(100))
    }

    @Test
    fun `getCommandResponse should recognize history command`() {
        val event = mock<SlashCommandInteractionEvent> {
            on { name } doReturn "history"
        }
        val option = mock<OptionMapping> {
            on { asInt } doReturn 5
        }

        whenever(event.getOption("n")).thenReturn(option)

        spyEventListener.getCommandResponse(event, "TestUser", mock<MessageChannelUnion>())

        verify(spyEventListener).history(eq(5), eq("TestUser"), any())
    }

    @Test
    fun `getCommandResponse should ignore unknown command`() {
        val event = mock<SlashCommandInteractionEvent> {
            on { name } doReturn "unknown"
        }

        val result = eventListener.getCommandResponse(event, "TestUser", mock<MessageChannelUnion>())
        assertThat(result).isEqualTo("????")

        // Nothing goes out
        verifyNoMoreInteractions(chatService, imageService, storageService)
    }

    @Test
    fun `summarize command asks AI to summarize conversation`() {
        eventListener.summarize("TestUser")

        verify(chatService).chat(startsWith("Summarize"), eq("TestUser"))
    }

    @Test
    fun `role command sends role to chatService`() {
        eventListener.role("TestRole")

        verify(chatService).role(eq("TestRole"))
    }

    @Test
    fun `tokens command sends tokens to chatService`() {
        eventListener.tokens(100)

        verify(chatService).maxTokens(eq(100))
    }

    @Test
    fun `temperature command sends temperature to chatService`() {
        eventListener.temperature(0.5)

        verify(chatService).temperature(eq(0.5))
    }

    @Test
    fun `forget command sends forget to chatService`() {
        eventListener.forget("TestUser")

        verify(chatService).forget(eq("TestUser"))
    }

    @Test
    fun `history command should return no history found when no images are stored`() {
        whenever(storageService.list("TestUser")).thenReturn(emptyList())

        val result = eventListener.history(0, "TestUser", mock())

        assertThat(result).isEqualTo("No history found for TestUser")
    }

    @Test
    fun `history command should return a list of stored images when n is 0`() {
        val list = listOf(
            StoredImageResult("1", "file1.png", MetaData("userName", "url1", "prompt1", "revisedPrompt1")),
            StoredImageResult("2", "file2.png", MetaData("userName", "url2", "prompt2", "revisedPrompt2"))
        )

        whenever(storageService.list("TestUser")).thenReturn(list)

        val result = eventListener.history(0, "TestUser", mock())

        assertThat(result).isEqualTo("1 \t prompt1\n2 \t prompt2")
    }

    @Test
    fun `history command should call uploadImage with the nth StoredImageResult if the discord URL is already set`() {
        val channel = mock<MessageChannelUnion> {
            on { sendMessage(anyString()) } doReturn mock()
        }
        val list = listOf(
            StoredImageResult("1", "file1.png", MetaData("userName", "url1", "prompt1", "revisedPrompt1")),
            StoredImageResult("2", "file2.png", MetaData("userName", "url2", "prompt2", "revisedPrompt2"))
        )

        whenever(storageService.list("TestUser")).thenReturn(list)

        val result = spyEventListener.history(2, "TestUser", channel)
        assertThat(result).isEqualTo("prompt2")
        verify(spyEventListener).uploadImage(eq(list[1]), eq(channel))
    }

    @Test
    fun `uploadImage should send discord URL if it is already in the StoredImageResult`() {
        val imageResult = StoredImageResult("1", "file1.png", MetaData("userName", "url1", "prompt1", "revisedPrompt1"))
        val channel = mock<MessageChannelUnion> {
            on { sendMessage(anyString()) } doReturn mock()
        }

        eventListener.uploadImage(imageResult, channel)

        verify(channel).sendMessage("url1")
    }

    @Test
    fun `uploadImage should reload image if discord URL is not set`() {
        val url = "https://example.com/image.png"
        val imageResult = StoredImageResult("1", "image.png", MetaData("userName", null, "prompt1", "revisedPrompt1"))

        val channel = mock<MessageChannelUnion>()
        val messageCreateAction = mock<MessageCreateAction>()
        val message = mock<Message> {
            on { attachments } doReturn
                    listOf(Message.Attachment(1, url, null, "file1.png", "image/png", null, 100, 100, 100, false, null, 0.0, null))
        }

        whenever(storageService.load("1")).thenReturn(mock<InputStream>())

        whenever(channel.sendMessage(anyString())).thenReturn(messageCreateAction)
        whenever(messageCreateAction.setFiles(any<FileUpload>())).thenReturn(messageCreateAction)
        whenever(messageCreateAction.queue(any<Consumer<Message>>())).thenAnswer {
            val consumer = it.arguments[0] as Consumer<Message>
            consumer.accept(message)
            messageCreateAction
        }

        eventListener.uploadImage(imageResult, channel)

        verify(storageService).update("1", url)
    }

    @Test
    fun `chat command sends chat to chatService`() {
        eventListener.chat("chat prompt", "TestUser")

        verify(chatService).chat(eq("chat prompt"), eq("TestUser"))
    }

    @Test
    fun `image command should send image to chatService and store the result`() {
        val channel = mock<MessageChannelUnion> {
            on { sendMessage(anyString()) } doReturn mock()
        }
        val imageData = ImageResult("url", "prompt", "revisedPrompt")

        whenever(imageService.image("image prompt", "TestUser")).thenReturn(imageData)

        val result = eventListener.image("image prompt", "TestUser", channel)

        assertThat(result).isEqualTo("revisedPrompt")
        verify(channel).sendMessage("url")
        verify(storageService).store("url", "TestUser", "image prompt", "revisedPrompt")
    }

    @Test
    fun `onSlashCommandInteraction should decode and execute command and send response to channel`() {
        val event = mock<SlashCommandInteractionEvent> {
            on { name } doReturn "command"
            on { interaction } doReturn mock()
            on { interaction.user } doReturn mock()
            on { interaction.user.globalName } doReturn "TestUser"
        }
        val interactionHook = mock<InteractionHook> {
            on { sendMessage(anyString()) } doReturn mock()
        }
        val option = mock<OptionMapping> {
            on { asString } doReturn "chat"
        }
        val hook = mock<InteractionHook> {
            on { sendMessage(anyString()) } doReturn mock()
        }
        val channel = mock<MessageChannelUnion>()
        val replyCallbackAction = mock<ReplyCallbackAction>()

        whenever(event.channel).thenReturn(channel)
        whenever(event.hook).thenReturn(hook)
        whenever(event.deferReply()).thenReturn(replyCallbackAction)
        whenever(replyCallbackAction.queue(any())).thenAnswer {
            val consumer = it.arguments[0] as Consumer<InteractionHook>
            consumer.accept(interactionHook)
            replyCallbackAction
        }
        whenever(event.options).thenReturn(listOf(option))

        whenever(spyEventListener.getCommandResponse(event, "TestUser", channel)).thenReturn("response")

        spyEventListener.onSlashCommandInteraction(event)

        verify(event.hook).sendMessage(eq("command -> chat"))
        verify(interactionHook).sendMessage(eq("response"))
    }

}