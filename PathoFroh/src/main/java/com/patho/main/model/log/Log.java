package com.patho.main.model.log;

import com.patho.main.model.util.log.LogListener;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import javax.persistence.*;

@Entity
@SequenceGenerator(name = "log_sequencegenerator", sequenceName = "log_sequence", initialValue = 1, allocationSize = 50)
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
