package com.patho.main.service

import com.patho.main.model.interfaces.ListOrder
import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.ListItemType
import com.patho.main.model.preset.ListItem_
import com.patho.main.repository.jpa.ListItemRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


@Service()
open class ListItemService @Autowired constructor(
        private val listItemRepository: ListItemRepository) : AbstractService() {

    /**
     * Adds or updates a list Item. If added the list item indexInList is set to last in list
     */
    @Transactional
    open fun addOrUpdate(listItem: ListItem): ListItem {
        var result = listItem
        if (result.id == 0L) {
            logger.debug("Creating new ListItem " + result.value + " for " + result.listType.toString())
            result.indexInList = listItemRepository.count { root, _, cb -> cb.equal(root.get(ListItem_.listType), result.listType) }.toInt()
            result = listItemRepository.save(result, resourceBundle["log.settings.staticList.new", result.value, result.listType.toString()])
        } else {
            logger.debug("Updating ListItem " + result.value)
            result = listItemRepository.save(result, resourceBundle["log.settings.staticList.update", result.value, result.listType.toString()])
        }
        return result
    }

    /**
     * Deletes or archives a ListItem. If deleting is not possible the item will be archvied.
     */
    @Transactional(propagation = Propagation.NEVER)
    open fun deleteOrArchive(listItem: ListItem): Boolean {
        return try {
            listItemRepository.delete(listItem, resourceBundle["log.settings.staticList.deleted", listItem.value, listItem.listType])
            true
        } catch (e: Exception) {
            archive(listItem, true)
            false
        }
    }

    /**
     * Archives a ListItem
     */
    @Transactional
    open fun archive(listItem: ListItem, archive: Boolean): ListItem {
        listItem.archived = archive
        return listItemRepository.save(listItem,
                resourceBundle[if (archive) "log.settings.staticList.archived" else "log.settings.staticList.dearchived", listItem.value, listItem.listType])
    }

    /**
     * Reorganizes and persists the list
     */
    @Transactional
    open fun updateReorderedList(staticListContent: List<ListItem>, type: ListItemType): List<ListItem> {
        ListOrder.reOrderList(staticListContent)
        return listItemRepository.saveAll(staticListContent,
                resourceBundle["log.settings.staticList.list.reoder", type])
    }
}