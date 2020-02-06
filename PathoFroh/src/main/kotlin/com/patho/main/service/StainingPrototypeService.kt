package com.patho.main.service

import com.patho.main.model.interfaces.ListOrder
import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.ListItemType
import com.patho.main.model.preset.StainingPrototype
import com.patho.main.repository.jpa.StainingPrototypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service()
open class StainingPrototypeService @Autowired constructor(
        private val stainingPrototypeRepository: StainingPrototypeRepository) : AbstractService() {

    /**
     * Adds or updates a new staining prototype
     */
    @Transactional
    open fun addOrUpdate(prototype: StainingPrototype): StainingPrototype {
        return stainingPrototypeRepository.save(prototype,
                resourceBundle[if (prototype.id == 0L) "log.settings.staining.new" else "log.slide.edit.textUpdate", prototype])
    }

    /**
     * Increases the priority counter of a preset
     */
    @Transactional
    open fun incrementPriorityCounter(prototype: StainingPrototype): StainingPrototype {
        val p: Optional<StainingPrototype> = stainingPrototypeRepository.findById(prototype.id)
        if (p.isPresent) {
            p.get().priorityCount = p.get().priorityCount + 1
            return stainingPrototypeRepository.save(p.get(), resourceBundle["log.settings.staining.priority.increment", p.get().name, p.get().priorityCount])
        }
        return prototype
    }

    /**
     * Tries to delete the staining prototype, if not possible the prototype will be
     * deleted
     */
    @Transactional(propagation = Propagation.NEVER)
    open fun deleteOrArchive(prototype: StainingPrototype): Boolean {
        return try {
            stainingPrototypeRepository.delete(prototype, resourceBundle.get("log.settings.staining.deleted", prototype.name))
            true
        } catch (e: Exception) {
            archive(prototype, true)
            false
        }
    }

    /**
     * Archived or dearchives a prototype
     */
    @Transactional
    open fun archive(p: StainingPrototype, archive: Boolean): StainingPrototype? {
        p.archived = archive
        return stainingPrototypeRepository.save(p, resourceBundle
                .get(if (archive) "log.settings.staining.archived" else "log.settings.staining.dearchived", p.name))
    }

    /**
     * Reorganizes and persists the list
     */
    @Transactional
    open fun updateReorderedList(stainingPrototypes: List<StainingPrototype>): List<StainingPrototype> {
        ListOrder.reOrderList(stainingPrototypes)
        return stainingPrototypeRepository.saveAll(stainingPrototypes, resourceBundle["log.settings.material.list.reoder"])
    }
}