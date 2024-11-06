package net.av.vtask

import androidx.annotation.IntRange
import kotlinx.serialization.Serializable

@Serializable
class Task(
    override var title: String,
    var content: String,
    @IntRange(0, 255) var urgency: Int,
    @IntRange(0, 255) var familiarity: Int,
    @IntRange(0, 255) var motivation: Int,
    @IntRange(0, 255) var deterrence: Int,
    override val children: MutableList<String> = mutableListOf(),
    override val referrers: MutableList<String> = mutableListOf()
) : IItemWithChildren