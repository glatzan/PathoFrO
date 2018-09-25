package com.patho.main.action;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.dialog.notification.ContactDialog;
import com.patho.main.action.dialog.notification.ContactNotificationDialog;
import com.patho.main.action.dialog.notification.NotificationDialog;
import com.patho.main.action.dialog.notification.NotificationPhaseExitDialog;
import com.patho.main.action.dialog.patient.DeleteTaskDialog;
import com.patho.main.action.dialog.patient.EditPatientDialog;
import com.patho.main.action.dialog.print.FaxPrintDocumentDialog;
import com.patho.main.action.dialog.settings.groups.GroupEditDialog;
import com.patho.main.action.dialog.settings.groups.GroupListDialog;
import com.patho.main.action.dialog.settings.material.MaterialEditDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationEditDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationListDialog;
import com.patho.main.action.dialog.settings.physician.PhysicianEditDialog;
import com.patho.main.action.dialog.settings.physician.PhysicianSearchDialog;
import com.patho.main.action.dialog.settings.slide.StainingEditDialog;
import com.patho.main.action.dialog.settings.users.EditUserDialog;
import com.patho.main.action.dialog.settings.users.UserListDialog;
import com.patho.main.action.dialog.slides.AddSlidesDialog;
import com.patho.main.action.dialog.slides.StainingPhaseExitDialog;
import com.patho.main.action.dialog.task.ArchiveTaskDialog;
import com.patho.main.action.dialog.task.ChangeTaskIDDialog;

import lombok.Getter;

@Component
@Scope(value = "session")
@Getter
public class DialogHandlerAction {

	private OrganizationListDialog organizationListDialog = new OrganizationListDialog();

	private ContactDialog contactDialog = new ContactDialog();

	private ContactNotificationDialog contactNotificationDialog = new ContactNotificationDialog();

	private NotificationDialog notificationDialog = new NotificationDialog();

	private FaxPrintDocumentDialog faxPrintDocumentDialog = new FaxPrintDocumentDialog();

	private DeleteTaskDialog deleteTaskDialog = new DeleteTaskDialog();

	private EditPatientDialog editPatientDialog = new EditPatientDialog();

	private ArchiveTaskDialog archiveTaskDialog = new ArchiveTaskDialog();

	private StainingPhaseExitDialog stainingPhaseExitDialog = new StainingPhaseExitDialog();

	private PhysicianSearchDialog physicianSearchDialog = new PhysicianSearchDialog();

	private PhysicianEditDialog physicianEditDialog = new PhysicianEditDialog();

	private MaterialEditDialog materialEditDialog = new MaterialEditDialog();

	private StainingEditDialog stainingEditDialog = new StainingEditDialog();

	private GroupListDialog groupListDialog = new GroupListDialog();

	private GroupEditDialog groupEditDialog = new GroupEditDialog();

	private UserListDialog userListDialog = new UserListDialog();

	private AddSlidesDialog createSlidesDialog = new AddSlidesDialog();

	private ChangeTaskIDDialog changeTaskIDDialog = new ChangeTaskIDDialog();

	private EditUserDialog editUserDialog = new EditUserDialog();

	private OrganizationEditDialog organizationEditDialog = new OrganizationEditDialog();

	private NotificationPhaseExitDialog notificationPhaseExitDialog = new NotificationPhaseExitDialog();

}
