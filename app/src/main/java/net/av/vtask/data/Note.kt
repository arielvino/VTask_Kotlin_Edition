package net.av.vtask.data

data class Note(
    override var id: String,
    override val title: String,
    override val referrers: MutableList<String>,
    val content: String = ""
) : IDataItem
