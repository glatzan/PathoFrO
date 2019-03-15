package com.patho.main.action.views

import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.repository.PhysicianRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
class DiagnosisView @Autowired constructor(
        private val physicianRepository: PhysicianRepository) : AbstractTaskView() {

    var diagnosisViewData = mutableListOf<DiagnosisViewData>()

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
    }

    class DiagnosisViewData(diagnosisRevision: DiagnosisRevision) {
        val diagnosisRevision = diagnosisRevision
    }
}