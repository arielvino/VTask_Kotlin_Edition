package net.av.vtask

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.av.vtask.data.Collection
import net.av.vtask.data.IDataItem
import net.av.vtask.security.CryptoFactory
import java.io.File
import java.security.SecureRandom
import java.util.Base64

class FilesDataItemProvider(private val userName: String) : IDataItemProvider {
    companion object {
        private const val ID_LENGTH = 4
    }

    override fun init() {
        File("${App.appContext.filesDir.absolutePath}/$userName/items").mkdirs()
        for (root in IDataItemProvider.IndependentId.entries) {
            val id = IDataItemProvider.rootId[root]
            val rootFile = File("${App.appContext.filesDir.absolutePath}/$userName/items/$id")
            if (!rootFile.exists()) {
                val rootCollection = Collection(mutableListOf(), mutableListOf(), "", id!!)
                val json = Json.encodeToString<IDataItem>(rootCollection)
                val cipher = CryptoFactory.aesGcmEncrypt(CryptoFactory.key!!, json)
                rootFile.writeBytes(cipher)
            }
        }
    }

    override fun create(item: IDataItem): String {
        var file: File
        var id: String
        do {
            val idBytes = ByteArray(ID_LENGTH)
            SecureRandom().nextBytes(idBytes)
            id = Base64.getEncoder().encodeToString(idBytes)
            item.id = id
            file = File("${App.appContext.filesDir.absolutePath}/$userName/items/$id")
        } while (file.exists())
        file.writeBytes(CryptoFactory.aesGcmEncrypt(CryptoFactory.key!!, Json.encodeToString(item)))
        return if (file.exists()) id
        else ""
    }

    override fun edit(id: String, item: IDataItem): Boolean {
        val itemBytes = CryptoFactory.aesGcmEncrypt(CryptoFactory.key!!, Json.encodeToString(item))
        val itemFile = File("${App.appContext.filesDir.absolutePath}/$userName/items/$id")
        itemFile.writeBytes(itemBytes)
        return true
    }

    override fun delete(id: String): Boolean {
        return File("${App.appContext.filesDir.absolutePath}/$userName/items/$id").delete()
    }

    override fun get(id: String): IDataItem? {
        return try {
            Json.decodeFromString<IDataItem>(
                CryptoFactory.aesGcmDecryptAsString(
                    CryptoFactory.key!!,
                    File("${App.appContext.filesDir.absolutePath}/$userName/items/$id").readBytes()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}