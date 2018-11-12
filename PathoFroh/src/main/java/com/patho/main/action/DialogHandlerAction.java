package com.patho.main.action;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.dialog.notification.ContactDialog;
import com.patho.main.action.dialog.notification.ContactNotificationDialog;
import com.patho.main.action.dialog.notification.NotificationDialog;
import com.patho.main.action.dialog.notification.NotificationPhaseExitDialog;
import com.patho.main.action.dialog.patient.DeleteTaskDialog;
import com.patho.main.action.dialog.patient.EditPatientDialog;
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


}
