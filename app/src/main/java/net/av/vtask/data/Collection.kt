package net.av.vtask.data

import kotlinx.serialization.Serializable


@Serializable()
data class Collection(
    override val referrers: MutableList<String> = mutableListOf(),
    override val children: MutableList<String> = mutableListOf(),
    override val title: String,
    override var id: String
) : IItemWithChildren