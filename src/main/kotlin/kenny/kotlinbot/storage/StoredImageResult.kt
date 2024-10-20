package kenny.kotlinbot.storage

data class StoredImageResult(val id: String, val fileName: String, val metaData: MetaData) {
    data class MetaData(val userName: String, val discordUrl: String, val prompt: String, val revisedPrompt: String)
}
