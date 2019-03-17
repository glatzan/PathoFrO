package com.patho.main.action.views

import com.patho.main.model.DiagnosisPreset
import com.patho.main.model.ListItem
import com.patho.main.model.MaterialPreset
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.repository.PhysicianRepository
import com.patho.main.ui.selectors.PhysicianSelector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
open class DiagnosisView @Autowired constructor(
        private val physicianRepository: PhysicianRepository) : AbstractTaskView() {

    var diagnosisViewData = mutableListOf<DiagnosisViewData>()

    /**
     * Selected material presets
     */
    var selectedMaterialPresets: Array<MaterialPreset?> = arrayOf<MaterialPreset?>()

    /**
     * Material preset filter
     */
    var selectedMaterialPresetFilter: Array<String> = arrayOf<String>()

    /**
     * Search string for case history
     */
    var caseHistoryFilter: String = ""

    /**
     * Selected List item form caseHistory list
     */
    var selectedCaseHistoryItem: ListItem? = null

    /**
     * Selected surgeon
     */
    var selectedSurgeon: PhysicianSelector? = null

    /**
     * Surgeon filter
     */
    var selectedSurgeonFilter: String = ""

    /**
     * Private physician surgeon
     */
    var selectedPrivatePhysician: PhysicianSelector? = null

    /**
     * Private physician filter
     */
    var selectedPrivatePhysicianFilter: String = ""

    /**
     * Array for diagnoses filter
     */
    var diagnosisFilter: Array<Array<String>> = arrayOf<Array<String>>()

    /**
     * Array for selected diagnosis presets
     */
    var selectedDiagnosisPresets: Array<Array<DiagnosisPreset?>> = arrayOf<Array<DiagnosisPreset?>>()

    override fun loadView(task: Task) {
        super.loadView(task)
        diagnosisViewData.clear()
        diagnosisViewData.addAll(task.diagnosisRevisions.map { p -> DiagnosisViewData(p) })

        // updating signature date and person to sign
        for (revision in task.diagnosisRevisions) {
            if (!revision.completed) {
                revision.signatureDate = LocalDate.now()

                if (revision.signatureOne.physician == null || revision.signatureTwo.physician == null) {
                    // TODO set if physician to the left, if consultant to the right
                }
            }
        }

        selectedMaterialPresets = Array<MaterialPreset?>(task.samples.size) { p -> null }
        selectedMaterialPresetFilter = Array<String>(task.samples.size) { p -> "" }

        selectedCaseHistoryItem = null
        caseHistoryFilter = ""

        selectedSurgeon = null
        selectedSurgeonFilter = ""

        selectedPrivatePhysician = null
        selectedPrivatePhysicianFilter = ""

        diagnosisFilter = arrayOf<Array<String>>()
        selectedDiagnosisPresets = arrayOf<Array<DiagnosisPreset?>>()

        for (revision in task.diagnosisRevisions) {
            diagnosisFilter += Array<String>(revision.diagnoses.size) { i -> "" }
            selectedDiagnosisPresets += Array<DiagnosisPreset?>(revision.diagnoses.size) { i -> null }
        }
    }

    class DiagnosisViewData(diagnosisRevision: DiagnosisRevision) {
        val diagnosisRevision = diagnosisRevision
    }
}