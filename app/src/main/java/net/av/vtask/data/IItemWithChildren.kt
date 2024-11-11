package net.av.vtask.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface IItemWithChildren : IDataItem {
    val children: MutableList<String>
}