package kenny.kotlinbot.discord

import kenny.kotlinbot.ai.ChatService
import kenny.kotlinbot.ai.ImageResult
import kenny.kotlinbot.ai.ImageService
import kenny.kotlinbot.storage.StorageService
import kenny.kotlinbot.storage.StoredImageResult
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.utils.FileUpload
import org.springframework.stereotype.Component
import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger

@Component
class DiscordEventListener(
    val chatService: ChatService,
    val imageService: ImageService,
    val storageService: StorageService
) : ListenerAdapter() {

    /**
     * Determine if messages are meant for the bot. PMs are treated as prompts.
     *
     * @param event The message event received
     */
    override fun onMessageReceived(event: MessageReceivedEvent) {
        // Ignore channel messages, ignore self messages, ignore system messages
        if (event.isFromGuild || event.author.isBot || event.author.isSystem) {
            return
        }

        val content = event.message.contentRaw
        if (content.isEmpty()) {
            return
        }

        // Treat DMs as prompts
        if (!content.startsWith("/")) {
            val userName: String = event.author.globalName.toString()
            val reply: String = chatService.chat(content, userName)
            event.channel.sendMessage(reply).queue()
        }
    }

    /**
     * Dispatch slash commands and send feedback to the channel
     *
     * @param event The slash command interaction event.
     */
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val userName: String = event.interaction.user.globalName ?: "Someone"
        val channel = event.channel
        event.deferReply().queue { interactionHook: InteractionHook ->
            val content: String = getCommandResponse(event, userName, channel)
            interactionHook.sendMessage(content).queue()
        }
        if (event.options.isNotEmpty()) {
            event.hook.sendMessage("${event.name} -> ${event.options.last().asString}").queue()
        }
    }

    fun getCommandResponse(
        event: SlashCommandInteractionEvent,
        userName: String,
        channel: MessageChannelUnion
    ): String {
        return when (event.name) {
            "summary" -> summarize(userName)
            "forget" -> forget(userName)
            "role" -> role(
                event.getOption<String>("role",
                    { null },
                    { obj: OptionMapping -> obj.asString })
            )

            "image" -> image(
                event.getOption<String>("prompt",
                    { "random" },
                    { obj: OptionMapping -> obj.asString }), userName, channel
            )

            "prompt" -> chat(
                event.getOption<String>("prompt",
                    { "" },
                    { obj: OptionMapping -> obj.asString }), userName
            )

            "temp" -> temperature(
                event.getOption<Double>("temp",
                    { 0.7 },
                    { obj: OptionMapping -> obj.asDouble })
            )

            "tokens" -> tokens(
                event.getOption<Int>("tokens",
                    { 4069 },
                    { obj: OptionMapping -> obj.asInt })
            )

            "history" -> history(
                event.getOption<Int>("n",
                    { 0 },
                    { obj: OptionMapping -> obj.asInt }), userName, channel
            )

            else -> "????"
        }
    }

    fun summarize(userName: String): String {
        return chatService.chat("Summarize the conversation so far, in one paragraph", userName)
    }

    fun role(prompt: String): String {
        return chatService.role(prompt)
    }

    fun temperature(temp: Double): String {
        return chatService.temperature(temp)
    }

    fun tokens(tokens: Int): String {
        return chatService.maxTokens(tokens)
    }

    fun forget(userName: String): String {
        return chatService.forget(userName)
    }

    fun history(n: Int, userName: String, channel: MessageChannelUnion): String {
        val list = storageService.list(userName)
        if (list.isEmpty()) {
            return "No history found for $userName"
        }

        if (n > 0) {
            val imageResult: StoredImageResult = list.stream()
                .skip((n - 1).toLong())
                .findFirst()
                .orElseThrow { RuntimeException("That choice wasn't on the menu") }

            return uploadImage(imageResult, channel).metaData.prompt
        } else {
            return makeHistoryList(list)
        }
    }

    fun makeHistoryList(list: List<StoredImageResult>): String {
        val a = AtomicInteger(1)
        return list.joinToString("\n") { m: StoredImageResult ->
            "${a.getAndIncrement()} \t ${m.metaData.prompt}"
        }
    }

    fun uploadImage(imageResult: StoredImageResult, channel: MessageChannelUnion): StoredImageResult {
        if (imageResult.metaData.discordUrl != null) {
            channel.sendMessage(imageResult.metaData.discordUrl).queue()
        } else {
            val inputStream: InputStream = storageService.load(imageResult.id)
            val fileUpload = FileUpload.fromData(inputStream, imageResult.fileName)

            channel.sendMessage("Reloading previously generated image...")
                .setFiles(fileUpload)
                .queue { message: Message ->
                    storageService.update(
                        imageResult.id,
                        message.attachments.first().url
                    )
                }
        }

        return imageResult
    }

    /**
     * Add a prompt to the OpenAI API chat conversation and
     * return the response
     *
     * @param promptText   the prompt to chat with the user
     * @param userName the name of the user
     * @return the response from the OpenAI API
     */
    fun chat(promptText: String, userName: String): String {
        return chatService.chat(promptText, userName)
    }

    /**
     * Generate an image from a prompt and return the revised prompt
     *
     * @param prompt the prompt to generate the image
     * @param userName the name of the user
     * @param channel the channel to send the image
     */
    fun image(prompt: String, userName: String, channel: MessageChannelUnion): String {
        val imageData: ImageResult = imageService.image(prompt, userName)
        channel.sendMessage(imageData.url).queue()
        storageService.store(imageData.url, userName, prompt, imageData.revisedPrompt)
        return imageData.revisedPrompt
    }
}