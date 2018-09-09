package com.patho.main.model.util.audit;

/**
 * Audit Interface for jpa
 * @author andi
 *
 */
public interface AuditAble {

	public Audit getAudit();

	public void setAudit(Audit audit);
	
}