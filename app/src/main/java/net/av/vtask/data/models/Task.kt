package net.av.vtask.data.models

import kotlinx.serialization.Serializable
import net.av.vtask.data.LocalDateSerializer
import net.av.vtask.data.LocalDateTimeSerializer
import net.av.vtask.data.LocalTimeSerializer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Serializable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",

    val parentTaskId: String? = null,
    val childTaskIds: List<String> = emptyList(),

    @Serializable(with = LocalDateTimeSerializer::class)
    val dateCreated: LocalDateTime = LocalDateTime.now(),

    @Serializable(with = LocalDateTimeSerializer::class)
    val lastViewedTime: LocalDateTime? = null,

    val isWithoutDate: Boolean = false,
    val isDone: Boolean = false,

    @Serializable(with = LocalDateSerializer::class)
    val targetDate: LocalDate? = null,

    @Serializable(with = LocalTimeSerializer::class)
    val targetTime: LocalTime? = null,

    val version: Int = 0,

    @Serializable(with = LocalDateTimeSerializer::class)
    val deletedAt: LocalDateTime? = null
) {
    fun isDeleted(): Boolean = deletedAt != null

    fun isRoot(): Boolean = parentTaskId == null

    fun hasChildren(): Boolean = childTaskIds.isNotEmpty()

    fun markAsRead(): Task = this.copy(
        lastViewedTime = LocalDateTime.now(),
        version = version + 1
    )

    fun addChild(childId: String): Task = this.copy(
        childTaskIds = childTaskIds + childId,
        version = version + 1
    )

    fun removeChild(childId: String): Task = this.copy(
        childTaskIds = childTaskIds - childId,
        version = version + 1
    )

    fun softDelete(): Task = this.copy(
        deletedAt = LocalDateTime.now(),
        version = version + 1
    )

    fun restore(): Task = this.copy(
        deletedAt = null,
        version = version + 1
    )
}