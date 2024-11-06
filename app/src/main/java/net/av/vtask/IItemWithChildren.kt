package net.av.vtask

import kotlinx.serialization.Serializable

@Serializable
sealed interface IItemWithChildren : IDataItem {
    val children: MutableList<String>
}