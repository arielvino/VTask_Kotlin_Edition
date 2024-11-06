package net.av.vtask

interface IDataItemProvider {
    companion object {
        val current: IDataItemProvider = DataItemProvider

        val rootId: Map<RootGroups, String> = mapOf(
            RootGroups.Tasks to "--TASK--",
            RootGroups.Lists to "--LIST--",
            RootGroups.Notes to "--NOTE--"
        )
    }
    enum class RootGroups{
        Tasks,
        Lists,
        Notes
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