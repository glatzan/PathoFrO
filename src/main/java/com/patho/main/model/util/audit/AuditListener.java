package com.patho.main.model.util.audit;


import com.patho.main.model.audit.AuditAble;
import com.patho.main.service.impl.SpringContextBridge;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class AuditListener {

    @PrePersist
    public void setCreatedOn(AuditAble auditable) {
        Audit audit = auditable.getAudit();

        if (audit == null) {
            audit = new Audit();
            auditable.setAudit(audit);
        }

        audit.setCreatedOn(System.currentTimeMillis());
        audit.setCreatedBy(SpringContextBridge.services().getUserService().getCurrentUser().getUsername());
    }

    @PreUpdate
    public void setUpdatedOn(AuditAble auditable) {
        Audit audit = auditable.getAudit();
        audit.setUpdatedOn(System.currentTimeMillis());
        audit.setUpdatedBy(SpringContextBridge.services().getUserService().getCurrentUser().getUsername());
    }
}