package com.patho.main.common;

/**
 * @author andi
 *
 */
public enum Dialog {
	
	WORKLIST_SEARCH("/pages/dialog/worklist/worklistSearch/worklistSearch", null, 1280, 720, false, false, true),															// OK -> extended search missing, disable on load
	WORKLIST_EXPORT("/pages/dialog/export/exportDialog", null, 1024, 600, false, false, true), // 16:9
	PATIENT_ADD("/pages/dialog/patient/searchPatient/searchPatient", null, 1024, 600, false, false, true), 																	// OK
	PATIENT_DATA_CONFIRM("/pages/dialog/patient/confirmExternalPatientData/confirmExternalPatientData", null, 640, 360, false, false, true), 								// OK
	PATIENT_MERGE("/pages/dialog/patient/merge/mergePatient", null, 858, 484, false, false, true), // 16:9
	PATIENT_EDIT("/pages/dialog/patient/editPatient", null, 1024, 600, false, false, true), // 16:9
	PATIENT_REMOVE("/pages/dialog/patient/removePatient", null, 480, 272, false, false, true), // 16:9
	WORKLIST_ORDER("/pages/dialog/worklist/worklistOrder", null,480, 272, false, false, true),  																			// OK
	WORKLIST_SETTINGS("/pages/dialog/worklist/worklistSettings", null,480, 272, false, false, true), 																		// OK
	PATIENT_LOG("/pages/dialog/history/patientLog", null,  1024, 600, false, false, true),																					// OK
	TASK_CREATE("/pages/dialog/task/createTask", null, 1024, 600, false, false, true), 																						// OK		
	TASK_DELETE("/pages/dialog/task/delete/deleteTask", null, 480, 272, false, false, true), // 16:9	
	TASK_CHANGE_ID("/pages/dialog/task/chagneTaskID", null, 480, 272, false, false, true), 																					// OK
	SAMPLE_CREATE("/pages/dialog/task/sample/createSample", null, 480, 272, false, false, true), 																			// OK				// TEST OK
	BIO_BANK("/pages/dialog/biobank/biobank", null, 858, 484, false, false, true), 																							// OK					
	SLIDE_OVERVIEW("/pages/dialog/task/staining/slideOverview", null, 1024, 600, false, false, true), 																		// OK 				// TEST OK
	SLIDE_NAMING("/pages/dialog/task/staining/slideNaming", null,430, 270, false, false, true), 																			// OK				// TEST OK
	SLIDE_CREATE("/pages/dialog/task/staining/addSlide", null, 858, 484, false, false, true),																				// OK				// TEST OK
	DIAGNOSIS_RECORD_OVERWRITE("/pages/dialog/task/diagnosis/diagnosisRecordOverwrite", null, 480, 272, false, false, true), 												// OK				// TEST OK
	CONTACTS("/pages/dialog/contact/contacts", null, 1024, 600, false, false, true),																						// OK				// TEST OK
	CONTACTS_NOTIFICATION("/pages/dialog/contact/contactNotification", null, 858, 484, false, false, true), 																// OK				// TEST OK
	CONTACTS_SELECT("/pages/dialog/contact/contactSelect", null, 858, 484, false, false, true), 																			// OK				// TEST OK
	SETTINGS("/pages/dialog/globalSettings/settings", null, 1024, 600, false, false, true),																					// OK
	SETTINGS_PHYSICIAN_SEARCH("/pages/dialog/settings/physician/physicianSearch/physicianSearch", null, 1280, 720, false, false, true), 									// OK
	SETTINGS_PHYSICIAN_EDIT("/pages/dialog/settings/physician/physicianEdit", null, 1280, 740, false, false, true), 														// OK
	SETTINGS_STAINING_EDIT("/pages/dialog/settings/staining/stainingEdit", null, 1024, 600, false, false, true), 															// OK
	SETTINGS_MATERIAL_EDIT("/pages/dialog/settings/material/materialEdit", null, 1024, 600, false, false, true), 															// OK
	SETTINGS_DIAGNOSIS_EDIT("/pages/dialog/settings/diagnosis/diagnosisEdit", null, 1024, 600, false, false, true), 														// OK														
	SETTINGS_GROUP_LIST("/pages/dialog/settings/groups/groupList", null, 1024, 600, false, false, true), 																	// OK
	SETTINGS_GROUP_EDIT("/pages/dialog/settings/groups/groupEdit", null, 1024, 600, false, false, true), 																	// OK
	SETTINGS_USERS_LIST("/pages/dialog/settings/users/usersList", null, 1024, 600, false, false, true), 																	// OK
	SETTINGS_USERS_EDIT("/pages/dialog/settings/users/editUser/userEdit", null, 1280, 740, false, false, true), 															// OK
	SETTINGS_USERS_DELETE("/pages/dialog/settings/users/userDelete", null, 480, 272, false, false, true), 																	// OK
	SETTINGS_ORGANIZATION_EDIT("/pages/dialog/settings/organization/organizationEdit", null, 1280, 720, false, false, true),												// OK
	SETTINGS_ORGANIZATION_LIST("/pages/dialog/settings/organization/organizationList", null, 858, 484, false, false, true), 												// OK
	SETTINGS_FAVOURITE_LIST_EDIT("/pages/dialog/settings/favouriteList/favouriteListEdit", null, 1024, 600, false, false, true), 											// OK
	SETTINGS_LISTITEM_EDIT("/pages/dialog/settings/listItems/listItemEdit", null, 480, 272, false, false, true), 															// OK
	FAVOURITE_LIST_ITEM_REMOVE("/pages/dialog/settings/favouriteList/favouriteListItemRemove", null,  480, 272, false, false, true), // 16:9
	PRINT("/pages/dialog/print/print", null, 1280, 720, false, false, true), // 16:9
	PRINT_ADDRESS("/pages/dialog/print/address", null,  480, 272, false, false, true), 																						// OK
	PRINT_FAX("/pages/dialog/print/fax", null,  480, 272, false, false, true), 																								// OK
	COUNCIL("/pages/dialog/council/council", null,  1280, 720, false, false, true), 																						// OK
	USER_SETTINGS("/pages/dialog/userSettings/userSettings", null,  1024, 600, false, false, true), 																		// OK
	USER_SETTINGS_SAVE("/pages/dialog/userSettings/confirmSave", null,  480, 272, false, false, true), 																		// OK		
	MEDICAL_FINDINGS("/pages/dialog/medicalFindings/medicalFindings", null,  1024, 600, false, false, true),  // 16:9
	NOTIFICATION_ALREADY_PERFORMED("/pages/dialog/notification/notification_already_performed", null,  480, 272, false, false, true), //  16:9
	NOTIFICATION("/pages/dialog/notification/notification", null,  1024, 600, false, false, true),  // 16:9
	NOTIFICATION_PREVIEW("/pages/dialog/notification/notification_preview", null,  1024, 600, false, false, true), //  16:9
	INFO("/pages/dialog/info/info", null,  1024, 600, false, false, true),																									// OK
	DELETE_TASK_ENTITY("/pages/dialog/task/delete/deleteTaskEntity", null,  480, 272, false, false, true), //  16:9
	PDF_ORGANIZER("/pages/dialog/upload/pdfOrganizer", null,  1280, 720,  false, false, true), 																				// OK
	PDF_UPLOAD("/pages/dialog/upload/upload", null,  640, 360, false, false, true),																							// OK
	PDF_EDIT("/pages/dialog/upload/editPDF", null,  640, 360, false, false, true),																							// OK
	PDF_DELETE("/pages/dialog/upload/deletePDF", null, 480, 272, false, false, true), 																						// OK
	STAINING_PHASE_EXIT("/pages/dialog/task/staining/stainingPhaseExit", null, 480, 272, false, false, true), // 16:9
	DIAGNOSIS_PHASE_EXIT("/pages/dialog/task/diagnosis/diagnosisPhaseExit", null, 480, 272, false, false, true), // 16:9
	NOTIFICATION_PHASE_EXIT("/pages/dialog/notification/notificationPhaseExit", null, 480, 272, false, false, true), // 16:9
	TASK_ARCHIVE("/pages/dialog/task/archive/archiveTask", null, 480, 272, false, false, true), // 16:9
	TASK_RESTORE("/pages/dialog/task/archive/restoreTask", null, 480, 272, false, false, true), // 16:9
	DIAGNOSIS_REVISION_CREATE("/pages/dialog/task/diagnosisRevision/diagnosisRevisionsCreate", null, 640, 360, false, false, true),											// OK	
	DIAGNOSIS_REVISION_EDIT("/pages/dialog/task/diagnosisRevision/diagnosisRevisionsEdit", null,480, 272, false, false, true), 												// OK
	DIAGNOSIS_REVISION_DELETE("/pages/dialog/task/diagnosisRevision/diagnosisRevisionDelete", null, 480, 272, false, false, true), 											// OK
	DIAGNOSIS_REVISION_ADD("/pages/dialog/task/diagnosisRevision/diagnosisRevisionDialogQuickAdd", null, 480, 272, false, false, true), 									// OK		// TEST OK
	PRINT_SELECT_PRINTER("/pages/dialog/selectPrinter", null, 640, 360, false, false, true),
	ACCOUNTING_DATA("/pages/dialog/miscellaneous/accounting", null,  1024, 600, false, false, true),																		// OK
	CONFIRM_CHANGE("/pages/dialog/miscellaneous/confirm", null, 320, 180, false, false, true);																				// OK
	
	private final String path;
	private final boolean useOptions;
	private final String header;
	
	private final int width;
	private final int height;
	private final boolean resizeable;
	private final boolean draggable;
	private final boolean modal;
	
	Dialog(final String path) {
		this.path = path;
		this.width = 0;	
		this.height = 0;
		this.resizeable = false;
		this.draggable = false;
		this.modal = false;
		this.useOptions = false;
		this.header = null;
	}

	Dialog(final String path, final int width, final int heigt) {
		this.path = path;
		this.width = width;	
		this.height = heigt;
		this.resizeable = true;
		this.draggable = true;
		this.modal = false;
		this.useOptions = true;
		this.header = null;
	}
	
	Dialog(final String path, final String header, final int width, final int heigt, final boolean resizeable, final boolean draggable, final boolean modal) {
		this.path = path;
		this.width = width;	
		this.height = heigt;
		this.resizeable = resizeable;
		this.draggable = draggable;
		this.modal = modal;
		this.useOptions = true;
		this.header = header;
	}
	
	public String getPath() {
		return path;
	}

	public boolean isUseOptions() {
		return useOptions;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isResizeable() {
		return resizeable;
	}

	public boolean isDraggable() {
		return draggable;
	}

	public boolean isModal() {
		return modal;
	}
	
	public String getHeader(){
		return header;
	}
}
