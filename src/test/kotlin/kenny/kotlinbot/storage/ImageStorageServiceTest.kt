package kenny.kotlinbot.storage

import kenny.kotlinbot.storage.ImageStorageService.Companion.fileName
import kenny.kotlinbot.storage.ImageStorageService.Companion.url
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class ImageStorageServiceTest {

    private val discordUrl = "https://cdn.discordapp.com/attachments/1189760295636320266/1298669650774851634/img-aYW3Atl44T5a8vA9VCdIwwjN.png?ex=671b109c&is=6719bf1c&hm=e2a12508c328ef5902da43c429d884d8cea355a5df69829c14d062101da0893d&"
    private val blobStoreUrl = "https://oaidalleapiprodscus.blob.core.windows.net/private/org-a0msWrHUkuY87I8BYVwl1sfb/user-3oEOPLe0Vi1Mcv5zhP5UUlDk/img-5QkeduHqhKBocUBW7URn5Vkl.png?st=2024-10-24T10%3A55%3A16Z&se=2024-10-24T12%3A55%3A16Z&sp=r&sv=2024-08-04&sr=b&rscd=inline&rsct=image/png&skoid=d505667d-d6c1-4a0a-bac7-5c84a87759f8&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2024-10-23T22%3A57%3A14Z&ske=2024-10-24T22%3A57%3A14Z&sks=b&skv=2024-08-04&sig=pKmvEbE53w8YmVrkyk/42H6dJrxV4j2IOP9Wx%2BFvsYU%3D"
    private val fileSystemUrl = "file:src/test/resources/image.jpg"

    @Test
    fun getFilenameFromUrl() {
        assertThat(fileName(url(discordUrl))).isEqualTo("img-aYW3Atl44T5a8vA9VCdIwwjN.png")
        assertThat(fileName(url(blobStoreUrl))).isEqualTo("img-5QkeduHqhKBocUBW7URn5Vkl.png")
        assertThat(fileName(url(fileSystemUrl))).isEqualTo("image.jpg")
    }
}