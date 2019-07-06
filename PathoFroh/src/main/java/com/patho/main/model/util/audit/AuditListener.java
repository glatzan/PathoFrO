package com.patho.main.model.util.audit;


import com.patho.main.model.audit.AuditAble;
import com.patho.main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Configurable
public class AuditListener {
	
	@Autowired
	@Lazy
	private UserService userService;
	
    @PrePersist
    public void setCreatedOn(AuditAble auditable) {
        Audit audit = auditable.getAudit();
 
        if(audit == null) {
            audit = new Audit();
            auditable.setAudit(audit);
        }
 
        audit.setCreatedOn(System.currentTimeMillis());
        audit.setCreatedBy(userService.getCurrentUser().getUsername());
    }
 
    @PreUpdate
    public void setUpdatedOn(AuditAble auditable) {
        System.out.println("persists " +userService.getCurrentUser().getUsername());
    	Audit audit = auditable.getAudit();
        audit.setUpdatedOn(System.currentTimeMillis());
        audit.setUpdatedBy(userService.getCurrentUser().getUsername());
    }
}