package com.patho.main.model.util.audit;


import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

import com.patho.main.action.UserHandlerAction;

@Configurable
public class AuditListener {
	
	@Autowired
	@Lazy
	private UserHandlerAction userHandlerAction;
	
    @PrePersist
    public void setCreatedOn(AuditAble auditable) {
        Audit audit = auditable.getAudit();
 
        if(audit == null) {
            audit = new Audit();
            auditable.setAudit(audit);
        }
 
        audit.setCreatedOn(System.currentTimeMillis());
        audit.setCreatedBy(userHandlerAction.getCurrentUser().getUsername());
    }
 
    @PreUpdate
    public void setUpdatedOn(AuditAble auditable) {
        System.out.println("persists");
    	Audit audit = auditable.getAudit();
        audit.setUpdatedOn(System.currentTimeMillis());
        audit.setUpdatedBy(userHandlerAction.getCurrentUser().getUsername());
    }
}