package com.patho.main.util.task

import com.patho.main.model.patient.Sample
import com.patho.main.model.patient.Task
import com.patho.main.util.helper.TaskUtil
import javax.persistence.Transient

class TaskTreeTools {
    companion object {

        @JvmStatic
        fun updateNamesInTree(task: Task, useAutoNomenclature: Boolean = task.isUseAutoNomenclature, ignoreManuallyNamedItems: Boolean = false) {
            task.samples.forEach { p -> TaskStatus.Companion.updateAllNames(p, useAutoNomenclature, ignoreManuallyNamedItems) }
        }

        @JvmStatic
        fun updateName(sample: Sample, useAutoNomenclature: Boolean = sample?.task?.isUseAutoNomenclature
                ?: false, ignoreManuallyNamedItems: Boolean = false): Boolean {
            if (!sample.isIdManuallyAltered() || ignoreManuallyNamedItems && sample.isIdManuallyAltered()) {

                val name = TaskUtil.getSampleName(sample.task?.getSamples().size,
                        this.getParent()!!.getSamples().indexOf(this) + 1, useAutoNomenclature)

                if (getSampleID() == null || getSampleID() != name) {
                    setSampleID(name)
                    setIdManuallyAltered(false)
                    return true
                }
            }

            return false
        }


        fun updateNamesInTree(sample: Sample, useAutoNomenclature: Boolean = sample.getTask().isUseAutoNomenclature, ignoreManuallyNamedItems: Boolean = false) {
            updateNameOfSample(useAutoNomenclature, ignoreManuallyNamedItems)
            getBlocks().stream().forEach { p -> p.updateAllNames(useAutoNomenclature, ignoreManuallyNamedItems) }
        }
    }
}

/**
 * Updates the name of all block children
 *
 * @param useAutoNomenclature
 */
@Transient
override fun updateAllNames(useAutoNomenclature: Boolean, ignoreManuallyNamedItems: Boolean) {
    updateNameOfSample(useAutoNomenclature, ignoreManuallyNamedItems)
    blocks.stream().forEach { p -> p.updateAllNames(useAutoNomenclature, ignoreManuallyNamedItems) }
}

@Transient
fun updateAllNames() {
    updateNameOfSlide(getTask()!!.isUseAutoNomenclature, false)
}

@Transient
fun updateAllNames(useAutoNomenclature: Boolean, ignoreManuallyNamedItems: Boolean) {
    updateNameOfSlide(useAutoNomenclature, ignoreManuallyNamedItems)
}

@Transient
fun updateNameOfSlide(useAutoNomenclature: Boolean, ignoreManuallyNamedItems: Boolean): Boolean {
    if (!isIdManuallyAltered() || isIdManuallyAltered() && ignoreManuallyNamedItems) {
        var name = StringBuilder()

        if (useAutoNomenclature) {

            // generating block id
            name.append(parent!!.getParent()!!.sampleID)
            name.append(parent!!.getBlockID())
            if (name.length > 0)
                name.append(" ")

            name.append(slidePrototype!!.name)

            var stainingsInBlock = TaskUtil.getNumerOfSameStainings(this)

            if (stainingsInBlock > 1)
                name.append(stainingsInBlock.toString())

            if (getSlideID() == null || getSlideID() != name.toString()) {
                setSlideID(name.toString())
                setIdManuallyAltered(false)
                return true
            }
        } else if (getSlideID() == null || getSlideID().isEmpty()) {
            // only setting the staining and the number of the stating
            name.append(slidePrototype!!.name)

            var stainingsInBlock = TaskUtil.getNumerOfSameStainings(this)

            if (stainingsInBlock > 1)
                name.append(stainingsInBlock.toString())

            setSlideID(name.toString())
            setIdManuallyAltered(false)
            return true
        }
    }

    return false
}

@Transient
fun updateNameOfBlock(useAutoNomenclature: Boolean, ignoreManuallyNamedItems: Boolean): Boolean {
    if (!isIdManuallyAltered || ignoreManuallyNamedItems && isIdManuallyAltered) {
        if (useAutoNomenclature) {
            var name: String

            if (this.parent!!.blocks.size > 1) {
                name = TaskUtil.getCharNumber(parent!!.blocks.indexOf(this))
                if (task!!.samples.size == 1)
                    name = name.toUpperCase()
            } else {
                // no block name
                name = ""
            }

            if (blockID == null || blockID != name) {
                blockID = name
                isIdManuallyAltered = false
                return true
            }
        }
    }

    return false
}

@Transient
fun updateAllNames() {
    updateAllNames(task!!.isUseAutoNomenclature, false)
}

@Transient
override fun updateAllNames(useAutoNomenclature: Boolean, ignoreManuallyNamedItems: Boolean) {
    updateNameOfBlock(useAutoNomenclature, ignoreManuallyNamedItems)
    slides.stream().forEach { p -> p.updateNameOfSlide(useAutoNomenclature, ignoreManuallyNamedItems) }
}