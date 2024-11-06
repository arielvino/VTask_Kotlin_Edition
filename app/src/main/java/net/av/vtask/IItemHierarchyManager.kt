package net.av.vtask

interface IItemHierarchyManager {
    companion object {
        val current: IItemHierarchyManager = object : IItemHierarchyManager {}
    }

    fun addReferrer(referrerId: String, id: String) {
        val item = IDataItemProvider.current.get(id)!!
        if (!item.referrers.contains(referrerId)) {
            item.referrers.add(referrerId)
            IDataItemProvider.current.edit(id, item)
        }
    }

    fun removeReferrer(referrerId: String, id: String) {
        val item = IDataItemProvider.current.get(id)!!
        item.referrers.remove(referrerId)
        IDataItemProvider.current.edit(id, item)
    }

    fun appendChild(parentId: String, childId: String) {
        val item = IDataItemProvider.current.get(parentId)!! as IItemWithChildren
        if (!item.children.contains(childId)) {
            item.children.add(childId)
            IDataItemProvider.current.edit(parentId, item)
        }
    }

    fun removeChild(parentId: String, childId: String) {
        val item = IDataItemProvider.current.get(parentId)!! as IItemWithChildren
        item.children.remove(childId)
        IDataItemProvider.current.edit(parentId, item)
    }

    fun move(itemId: String, originId: String, destinationId: String) {
        addReferrer(destinationId, itemId)
        appendChild(destinationId, itemId)
        removeReferrer(originId, itemId)
        removeChild(originId, itemId)
    }

    fun create(item: IDataItem, parentId: String) {
        val id = IDataItemProvider.current.create(item)
        addReferrer(parentId, id)
        appendChild(parentId, id)
    }

    fun delete(itemId: String) {
        val item = IDataItemProvider.current.get(itemId)!!
        for (referrer in item.referrers) {
            removeChild(referrer, itemId)
        }
        if (item is IItemWithChildren) {
            for (child in item.children) {
                removeReferrer(itemId, child)
                val childItem = IDataItemProvider.current.get(child)!!
                if (childItem.referrers.isEmpty()) {
                    delete(child)
                }
            }
        }
    }
}