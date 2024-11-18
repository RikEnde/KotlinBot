package kenny.kotlinbot.discord

import kenny.kotlinbot.ai.ChatService
import kenny.kotlinbot.ai.ImageService
import kenny.kotlinbot.storage.StorageService
import kenny.kotlinbot.storage.StoredImageResult
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.FileUpload
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.retry.NonTransientAiException
import org.springframework.stereotype.Component

@Component
class DiscordEventListener(
    val chatService: ChatService,
    val imageService: ImageService,
    val storageService: StorageService,
    val properties: DiscordProperties
) : ListenerAdapter() {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun send(message: String, action: (String) -> Unit) {
        val parts = message.chunked(properties.messageSize)
        parts.forEach(action)
    }

    fun cap(message: String?): String? = message?.take(properties.messageSize)

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
            try {
                val userName: String = event.author.globalName.toString()
                val reply: String = chatService.chat(content, userName)

                send(reply) { part ->
                    event.channel.sendMessage(part).queue()
                }
            } catch (e: Exception) {
                logger.error("Error sending DM message: ${e.message}")
                event.channel.sendMessage("Error: ${cap(e.message)}").queue()
            }
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
            try {
                send(content) { part ->
                    interactionHook.sendMessage(part).queue()
                }
            } catch (e: Exception) {
                logger.error("Error sending slash command message: ${e.message}")
                interactionHook.sendMessage("Error: ${cap(e.message)}").queue()
            }
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
            "role" -> role(event.getOption("role")?.asString ?: "")
            "image" -> image(event.getOption("prompt")?.asString ?: "random", userName, channel)
            "prompt" -> chat(event.getOption("prompt")?.asString ?: "", userName)
            "temp" -> temperature(event.getOption("temp")?.asDouble ?: 0.7)
            "tokens" -> tokens(event.getOption("tokens")?.asInt ?: 4069)
            "history" -> history(event.getOption("n")?.asInt ?: 0, userName, channel)
            else -> "????"
        }
    }

    fun summarize(userName: String): String =
        chatService.chat("Summarize the conversation so far, in one paragraph", userName)

    fun role(prompt: String): String = chatService.role(prompt)

    fun temperature(temp: Double): String = chatService.temperature(temp)

    fun tokens(tokens: Int): String = chatService.maxTokens(tokens)

    fun forget(userName: String): String = chatService.forget(userName)

    fun history(n: Int, userName: String, channel: MessageChannelUnion): String {
        val list = storageService.list(userName)
        if (list.isEmpty()) {
            return "No history found for $userName"
        }

        return if (n > 0) {
            val imageResult = list.getOrNull(n - 1)
                ?: throw RuntimeException("That choice wasn't on the menu")
            uploadImage(imageResult, channel).metaData.prompt
        } else {
            makeHistoryList(list)
        }
    }

    fun makeHistoryList(list: List<StoredImageResult>): String {
        return list.withIndex().joinToString("\n") { (index, m) ->
            "${index + 1} \t ${m.metaData.prompt}"
        }
    }

    fun uploadImage(imageResult: StoredImageResult, channel: MessageChannelUnion): StoredImageResult {
        val discordUrl = imageResult.metaData.discordUrl
        if (discordUrl != null) {
            channel.sendMessage(discordUrl).queue()
        } else {
            val inputStream = storageService.load(imageResult.id)
            val fileUpload = FileUpload.fromData(inputStream, imageResult.fileName)

            channel.sendMessage("Reloading previously generated image...")
                .setFiles(fileUpload)
                .queue { message ->
                    val url = message.attachments.firstOrNull()?.url
                    if (url != null) {
                        storageService.update(imageResult.id, url)
                    }
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
    fun chat(promptText: String, userName: String): String = chatService.chat(promptText, userName)

    /**
     * Generate an image from a prompt and return the revised prompt
     *
     * @param prompt the prompt to generate the image
     * @param userName the name of the user
     * @param channel the channel to send the image
     */
    fun image(prompt: String, userName: String, channel: MessageChannelUnion): String {
        try {
            val imageData = imageService.image(prompt, userName)
            channel.sendMessage(imageData.url).queue()
            chatService.imageChat(userName, prompt, imageData.revisedPrompt)
            storageService.store(imageData.url, userName, prompt, imageData.revisedPrompt)
            return imageData.revisedPrompt
        } catch (e: NonTransientAiException) {
            return "Error generating image: ${e.message}"
        }
    }
}