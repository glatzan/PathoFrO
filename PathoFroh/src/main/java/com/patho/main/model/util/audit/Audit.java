package com.patho.main.model.util.audit;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Audit {
 
    @Column
    private long createdOn;
 
    @Column(length=10)
    private String createdBy;
 
    @Column
    private long updatedOn;
 
    @Column(length=10)
    private String updatedBy;
    
}
