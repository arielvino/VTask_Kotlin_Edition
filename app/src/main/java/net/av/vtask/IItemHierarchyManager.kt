package net.av.vtask

import net.av.vtask.data.IDataItem
import net.av.vtask.data.IItemWithChildren
import net.av.vtask.data.Task

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
        link(destinationId, itemId)
        unlink(originId, itemId)
    }

    fun link(referrerId: String, childId: String) {
        addReferrer(referrerId, childId)
        appendChild(referrerId, childId)
    }

    fun unlink(referrerId: String, childId: String) {
        removeReferrer(referrerId, childId)
        removeChild(referrerId, childId)
    }

    fun create(item: IDataItem, parentId: String) :String{
        val id = IDataItemProvider.current.create(item)
        link(parentId, id)

        if (item is Task) {
            if (item.status == Task.Status.Pending) {
                link(IDataItemProvider.rootId[IDataItemProvider.IndependentId.PendingTasks]!!, id)
            }
        }

        return id
    }

    fun edit(item: IDataItem, itemId: String) {
        IDataItemProvider.current.edit(itemId, item)
        if (item is Task) {
            if (item.status == Task.Status.Pending) {
                link(
                    IDataItemProvider.rootId[IDataItemProvider.IndependentId.PendingTasks]!!,
                    itemId
                )
            } else {
                unlink(
                    IDataItemProvider.rootId[IDataItemProvider.IndependentId.PendingTasks]!!,
                    itemId
                )
            }
        }
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
        IDataItemProvider.current.delete(itemId)
    }

    fun makeParentAwait(id: String){
        val item = IDataItemProvider.current.get(id)!!
        item.referrers.forEach {
            val referrer = IDataItemProvider.current.get(it)!!
            if(referrer is Task){
                if(referrer.status == Task.Status.Pending){
                    referrer.status = Task.Status.AwaitingSubTask
                    edit(referrer, it)
                }
            }
        }
    }
}