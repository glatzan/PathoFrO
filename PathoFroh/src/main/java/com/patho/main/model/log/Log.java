package com.patho.main.model.log;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import com.patho.main.common.DateFormat;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.user.HistoUser;
import com.patho.main.model.util.log.LogListener;
import com.patho.main.util.helper.TimeUtil;

import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "log_sequencegenerator", sequenceName = "log_sequence", initialValue=1, allocationSize = 1)
@RevisionEntity(LogListener.class)
@Getter
@Setter
public class Log {

	@Id
	@GeneratedValue(generator = "log_sequencegenerator")
	@Column(unique = true, nullable = false)
	@RevisionNumber
	private int id;

	@RevisionTimestamp
	private long timestamp;

	@Embedded
	private LogInfo logInfo;

}
