package net.av.vtask.data.models

import kotlinx.serialization.Serializable
import net.av.vtask.data.LocalDateTimeSerializer
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class Folder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",

    // Hierarchy (folders can be nested)
    val parentFolderId: String? = null,
    val childFolderIds: List<String> = emptyList(),

    // References to goals (not tasks - remember, folders are projections)
    val taskIds: List<String> = emptyList(),

    // Metadata
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateCreated: LocalDateTime = LocalDateTime.now(),

    val version: Int = 0,

    // Soft delete
    @Serializable(with = LocalDateTimeSerializer::class)
    val deletedAt: LocalDateTime? = null
) {
    fun isDeleted(): Boolean = deletedAt != null

    fun isRoot(): Boolean = parentFolderId == null

    fun hasChildren(): Boolean = childFolderIds.isNotEmpty()

    fun hasTasks(): Boolean = taskIds.isNotEmpty()

    fun addChildFolder(folderId: String): Folder = this.copy(
        childFolderIds = childFolderIds + folderId,
        version = version + 1
    )

    fun removeChildFolder(folderId: String): Folder = this.copy(
        childFolderIds = childFolderIds - folderId,
        version = version + 1
    )

    fun addGoal(taskId: String): Folder = this.copy(
        taskIds = taskIds+ taskId,
        version = version + 1
    )

    fun removeGoal(taskId: String): Folder = this.copy(
        taskIds = taskIds - taskId,
        version = version + 1
    )

    fun softDelete(): Folder = this.copy(
        deletedAt = LocalDateTime.now(),
        version = version + 1
    )

    fun restore(): Folder = this.copy(
        deletedAt = null,
        version = version + 1
    )
}