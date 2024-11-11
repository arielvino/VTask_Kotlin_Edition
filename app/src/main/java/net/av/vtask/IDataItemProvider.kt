package net.av.vtask

import net.av.vtask.data.IDataItem

interface IDataItemProvider {
    companion object {
        val current: IDataItemProvider
            get() = FilesDataItemProvider(App.userName!!)

        val rootId: Map<IndependentId, String> = mapOf(
            IndependentId.Root to "--ROOT--",
            IndependentId.PendingTasks to "--TASK--",
            IndependentId.Lists to "--LIST--",
            IndependentId.Notes to "--NOTE--"
        )
    }

    enum class IndependentId {
        Root, PendingTasks, Lists, Notes
    }

    fun init()
    fun create(item: IDataItem): String
    fun edit(id: String, item: IDataItem): Boolean
    fun delete(id: String): Boolean
    fun get(id: String): IDataItem?
    fun get(idList: List<String>): List<IDataItem?> {
        val list = mutableListOf<IDataItem?>()
        for (id in idList) {
            list.add(get(id))
        }
        return list.toList()
    }
}