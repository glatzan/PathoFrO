package com.patho.main.model.util.audit;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Audit {

    @Column
    private long createdOn;

    @Column(length = 10)
    private String createdBy;

    @Column
    private long updatedOn;

    @Column(length = 10)
    private String updatedBy;

    public long getCreatedOn() {
        return this.createdOn;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public long getUpdatedOn() {
        return this.updatedOn;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedOn(long updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
