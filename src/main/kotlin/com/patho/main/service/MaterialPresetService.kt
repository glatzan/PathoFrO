package com.patho.main.service

import com.patho.main.model.interfaces.ListOrder
import com.patho.main.model.preset.MaterialPreset
import com.patho.main.model.preset.StainingPrototype
import com.patho.main.repository.jpa.MaterialPresetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service()
open class MaterialPresetService @Autowired constructor(
        private val materialPresetRepository: MaterialPresetRepository) : AbstractService() {

    /**
     * Adds or updates a new material preset
     */
    @Transactional
    open fun addOrUpdate(material: MaterialPreset): MaterialPreset? {
        return materialPresetRepository.save(material, resourceBundle[if (material.id == 0L) "log.settings.material.new" else "log.settings.material.update", material.name])
    }

    /**
     * Increases the priority counter of a preset
     */
    @Transactional
    open fun incrementPriorityCounter(material: MaterialPreset): MaterialPreset? {
        val p: Optional<MaterialPreset> = materialPresetRepository.findById(material.id)
        if (p.isPresent) {
            p.get().priorityCount = p.get().priorityCount + 1
            return materialPresetRepository.save(p.get(), resourceBundle["log.settings.staining.priority.increment", p.get().name, p.get().priorityCount])
        }
        return material
    }

    /**
     * Deletes or archives a preset. If deleting is not possible the preset is archived
     */
    @Transactional(propagation = Propagation.NEVER)
    open fun deleteOrArchive(material: MaterialPreset): Boolean {
        return try {
            materialPresetRepository.delete(material, resourceBundle["log.settings.material.deleted", material.name])
            true
        } catch (e: Exception) {
            archive(material, true)
            false
        }
    }

    /**
     * Archives a preset
     */
    @Transactional
    open fun archive(material: MaterialPreset, archive: Boolean): MaterialPreset? {
        material.archived = archive
        return materialPresetRepository.save(material, resourceBundle[if (archive) "log.settings.material.archived" else "log.material.staining.dearchived", material.name])
    }

    /**
     * Reorganizes and persists the list
     */
    @Transactional
    open fun updateReorderedList(preset: List<MaterialPreset>): List<MaterialPreset> {
        ListOrder.reOrderList(preset)
        return materialPresetRepository.saveAll(preset, resourceBundle["log.settings.staining.list.reorder"])
    }
}