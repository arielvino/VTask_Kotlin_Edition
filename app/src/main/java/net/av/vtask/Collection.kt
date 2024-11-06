package net.av.vtask

import kotlinx.serialization.Serializable


@Serializable()
data class Collection(
    override val referrers: MutableList<String> = mutableListOf(),
    override val children: MutableList<String> = mutableListOf(),
    override val title: String
) : IItemWithChildren