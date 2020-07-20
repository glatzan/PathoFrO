package com.patho.main.model.interfaces

interface ListOrder<T : ListOrder<T>> {
    fun moveOrderUp(parentList: MutableList<T>): Boolean {
        val index = parentList.indexOf(this)
        if (index > 0) {
            parentList.remove(this)
            parentList.add(index - 1, this as T)
            reOrderList(parentList)
            return true
        }
        return false
    }

    fun moveOrderDown(parentList: MutableList<T>): Boolean {
        val index = parentList.indexOf(this)
        if (index < parentList.size - 1) {
            parentList.remove(this)
            parentList.add(index + 1, this as T)
            reOrderList(parentList)
            return true
        }
        return false
    }

    companion object {
        @JvmStatic
        fun reOrderList(parentList: List<ListOrder<*>>) {
            for ((i, listOrder) in parentList.withIndex()) {
                listOrder.indexInList = i
            }
        }
    }

    public var indexInList: Int
}