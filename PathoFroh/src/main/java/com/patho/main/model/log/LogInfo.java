package com.patho.main.model.log;

import com.patho.main.model.patient.Patient;
import com.patho.main.model.user.HistoUser;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.OneToOne;

/**
 * Object containing additional data for login. A string which describes the
 * action and optional a patient for whom the action was performed. Workaround
 * for adding more data to the LogListener
 *
 * @author glatza
 */
@Embeddable
@Getter
@Setter
public class LogInfo {

    @OneToOne
    private HistoUser histoUser;

    @OneToOne
    private Patient patient;

    @Column(columnDefinition = "text")
    private String logString;

    private String test;

    public LogInfo() {
    }

    public LogInfo(String log) {
        this(log, null, null);
    }

    public LogInfo(String log, HistoUser histoUser) {
        this(log, histoUser, null);
    }

    public LogInfo(String log, Patient patient) {
        this(log, null, patient);
    }

    public LogInfo(String log, HistoUser histoUser, Patient patient) {
        this.logString = log;
        this.histoUser = histoUser;
        this.patient = patient;
    }

}