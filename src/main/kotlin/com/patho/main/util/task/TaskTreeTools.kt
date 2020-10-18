package com.patho.main.util.task

import com.patho.main.model.interfaces.IdManuallyAltered
import com.patho.main.model.patient.Block
import com.patho.main.model.patient.Sample
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.util.helper.TaskUtil

class TaskTreeTools {
    companion object {


        @JvmStatic
        fun updateNamesInTree(entity: IdManuallyAltered, useAutoNomenclature: Boolean = entity.task?.useAutoNomenclature == true, ignoreManuallyNamedItems: Boolean = false, multipleEntities : Boolean = false) {
            when (entity) {
                is Task -> Companion.updateNamesInTree(entity as Task, useAutoNomenclature, ignoreManuallyNamedItems)
                is Sample -> Companion.updateNamesInTree(entity, useAutoNomenclature, ignoreManuallyNamedItems)
                is Block -> Companion.updateNamesInTree(entity, useAutoNomenclature, ignoreManuallyNamedItems)
                is Slide -> Companion.updateNamesInTree(entity, useAutoNomenclature, ignoreManuallyNamedItems)
                else -> {
                }
            }
        }

        @JvmStatic
        fun updateNamesInTree(task: Task, useAutoNomenclature: Boolean = task.useAutoNomenclature, ignoreManuallyNamedItems: Boolean = false) {
            task.samples.forEach { p -> TaskTreeTools.updateNamesInTree(p, useAutoNomenclature, ignoreManuallyNamedItems) }
        }

        @JvmStatic
        fun updateNamesInTree(sample: Sample, useAutoNomenclature: Boolean = sample.parent?.useAutoNomenclature
                ?: false, ignoreManuallyNamedItems: Boolean = false) {
            TaskTreeTools.updateName(sample, useAutoNomenclature, ignoreManuallyNamedItems)
            sample.blocks.forEach { p -> TaskTreeTools.updateNamesInTree(p, useAutoNomenclature, ignoreManuallyNamedItems) }
        }

        @JvmStatic
        fun updateName(sample: Sample, useAutoNomenclature: Boolean, ignoreManuallyNamedItems: Boolean): Boolean {
            if (!sample.idManuallyAltered || ignoreManuallyNamedItems && sample.idManuallyAltered) {

                val name = TaskUtil.getSampleName(sample.task?.samples?.size ?: 1,
                        (sample.parent?.samples?.indexOf(sample) ?: 0) + 1, useAutoNomenclature)

                if (sample.sampleID == null || sample.sampleID != name) {
                    sample.sampleID = name
                    sample.idManuallyAltered = false
                    return true
                }
            }

            return false
        }

        /**
         * Changes the name of all blocks
         */
        @JvmStatic
        fun updateNamesInTree(block: Block, useAutoNomenclature: Boolean = block.parent?.parent?.useAutoNomenclature
                ?: false, ignoreManuallyNamedItems: Boolean = false) {
            updateName(block, useAutoNomenclature, ignoreManuallyNamedItems)
            block.slides.forEach { p -> TaskTreeTools.updateNamesInTree(p, useAutoNomenclature, ignoreManuallyNamedItems) }
        }

        /**
         * Changes the name of a block
         */
        @JvmStatic
        fun updateName(block: Block, useAutoNomenclature: Boolean, ignoreManuallyNamedItems: Boolean): Boolean {
            if (!block.idManuallyAltered || block.idManuallyAltered && ignoreManuallyNamedItems) {
                if (useAutoNomenclature) {
                    var name: String

                    if (block.parent?.blocks?.size ?: 0 > 1) {
                        name = TaskUtil.getCharNumber(block.parent?.blocks?.indexOf(block) ?: 1)
                        if (block.parent?.parent?.samples?.size == 1)
                            name = name.toUpperCase()
                    } else {
                        // no block name
                        name = ""
                    }

                    if (block.blockID == null || block.blockID != name) {
                        block.blockID = name
                        block.idManuallyAltered = false
                        return true
                    }
                }
            }

            return false
        }

        /**
         * Changes the name of a slide
         */
        @JvmStatic
        fun updateNamesInTree(slide: Slide, useAutoNomenclature: Boolean = slide.parent?.parent?.parent?.useAutoNomenclature
                ?: false, ignoreManuallyNamedItems: Boolean = false) {
            updateName(slide, useAutoNomenclature, ignoreManuallyNamedItems)
        }

        /**
         * Changes the name of a slide
         */
        @JvmStatic
        fun updateName(slide: Slide, useAutoNomenclature: Boolean, ignoreManuallyNamedItems: Boolean): Boolean {
            if (!slide.idManuallyAltered || slide.idManuallyAltered && ignoreManuallyNamedItems) {
                var name = StringBuilder()

                if (useAutoNomenclature) {

                    // generating block id
                    name.append(slide.parent?.parent?.sampleID ?: "")
                    name.append(slide.parent?.blockID ?: "")
                    if (name.isNotEmpty())
                        name.append(" ")

                    name.append(slide.slidePrototype?.name ?: "")

                    var stainingsInBlock = TaskUtil.getNumerOfSameStainings(slide)

                    if (stainingsInBlock > 1)
                        name.append(stainingsInBlock.toString())

                    if (slide.slideID == null || slide.slideID != name.toString()) {
                        slide.slideID = name.toString()
                        slide.idManuallyAltered = false
                        return true
                    }
                } else if (slide.slideID == null || slide.slideID.isEmpty()) {
                    // only setting the staining and the number of the stating
                    name.append(slide.slidePrototype?.name ?: "")

                    var stainingsInBlock = TaskUtil.getNumerOfSameStainings(slide)

                    if (stainingsInBlock > 1)
                        name.append(stainingsInBlock.toString())

                    slide.slideID = name.toString()
                    slide.idManuallyAltered = false
                    return true
                }
            }

            return false
        }

    }
}


