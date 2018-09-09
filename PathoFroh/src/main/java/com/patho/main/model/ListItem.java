package com.patho.main.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RevisionNumber;

import com.patho.main.common.Dialog;
import com.patho.main.model.interfaces.ArchivAble;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.interfaces.ListOrder;

import lombok.Getter;
import lombok.Setter;

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "listItem_sequencegenerator", sequenceName = "listItem_sequence")
@Getter
@Setter
public class ListItem implements ListOrder<ListItem>, ArchivAble, ID {

	public enum StaticList {
		WARDS, CASE_HISTORY, COUNCIL_ATTACHMENT, SLIDES, TASK_RESTORE;
	}

	@Id
	@GeneratedValue(generator = "listItem_sequencegenerator")
	@Column(unique = true, nullable = false)
	@RevisionNumber
	private long id;
	@Enumerated(EnumType.STRING)
	private StaticList listType;
	@Column(columnDefinition = "VARCHAR")
	private String value;
	@Column
	private int indexInList;
	@Column
	private boolean archived;

	/********************************************************
	 * Interface ArchiveAble
	 ********************************************************/
	@Override
	@Transient
	public String getTextIdentifier() {
		return null;
	}

	@Override
	@Transient
	public Dialog getArchiveDialog() {
		return null;
	}
	/********************************************************
	 * Interface ArchiveAble
	 ********************************************************/
}
