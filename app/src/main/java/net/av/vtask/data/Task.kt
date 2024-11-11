package net.av.vtask.data

import kotlinx.serialization.Serializable

@Serializable
class Task(
    override var title: String,
    var content: String,
    var status: Status = Status.Pending,
    override val children: MutableList<String> = mutableListOf(),
    override val referrers: MutableList<String> = mutableListOf(),
    override var id: String
) : IItemWithChildren {
    enum class Status {
        Pending, AwaitingSubTask, InYourFreeTime, OnHold
    }
}