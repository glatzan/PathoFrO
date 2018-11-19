package com.patho.main.action.dialog;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.dialog.biobank.BioBankDialog;
import com.patho.main.action.dialog.council.CouncilDialog;
import com.patho.main.action.dialog.diagnosis.CopyHistologicalRecordDialog;
import com.patho.main.action.dialog.diagnosis.CreateDiagnosisRevisionDialog;
import com.patho.main.action.dialog.diagnosis.DeleteDiagnosisRevisionDialog;
import com.patho.main.action.dialog.diagnosis.DiagnosisPhaseExitDialog;
import com.patho.main.action.dialog.diagnosis.EditDiagnosisRevisionsDialog;
import com.patho.main.action.dialog.diagnosis.QuickAddDiangosisRevisionDialog;
import com.patho.main.action.dialog.media.DeletePDFDialog;
import com.patho.main.action.dialog.media.EditPDFDialog;
import com.patho.main.action.dialog.media.PDFOrganizer;
import com.patho.main.action.dialog.media.UploadDialog;
import com.patho.main.action.dialog.miscellaneous.AccountingDataDialog;
import com.patho.main.action.dialog.notification.ContactDialog;
import com.patho.main.action.dialog.notification.ContactNotificationDialog;
import com.patho.main.action.dialog.notification.ContactSelectDialog;
import com.patho.main.action.dialog.notification.NotificationDialog;
import com.patho.main.action.dialog.patient.PatientLogDialog;
import com.patho.main.action.dialog.patient.SearchPatientDialog;
import com.patho.main.action.dialog.print.CustomAddressDialog;
import com.patho.main.action.dialog.print.FaxDocumentDialog;
import com.patho.main.action.dialog.print.PrintDialog;
import com.patho.main.action.dialog.settings.SettingsDialog;
import com.patho.main.action.dialog.settings.UserSettingsDialog;
import com.patho.main.action.dialog.settings.diagnosis.DiagnosisPresetEditDialog;
import com.patho.main.action.dialog.settings.favourites.FavouriteListEditDialog;
import com.patho.main.action.dialog.settings.groups.GroupEditDialog;
import com.patho.main.action.dialog.settings.groups.GroupListDialog;
import com.patho.main.action.dialog.settings.listitem.ListItemEditDialog;
import com.patho.main.action.dialog.settings.material.MaterialEditDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationEditDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationListDialog;
import com.patho.main.action.dialog.settings.physician.PhysicianEditDialog;
import com.patho.main.action.dialog.settings.physician.PhysicianSearchDialog;
import com.patho.main.action.dialog.settings.slide.StainingEditDialog;
import com.patho.main.action.dialog.settings.users.ConfirmUserDeleteDialog;
import com.patho.main.action.dialog.settings.users.EditUserDialog;
import com.patho.main.action.dialog.settings.users.UserListDialog;
import com.patho.main.action.dialog.slides.AddSlidesDialog;
import com.patho.main.action.dialog.slides.SlideNamingDialog;
import com.patho.main.action.dialog.slides.SlideOverviewDialog;
import com.patho.main.action.dialog.slides.StainingPhaseExitDialog;
import com.patho.main.action.dialog.task.ChangeTaskIDDialog;
import com.patho.main.action.dialog.task.CreateSampleDialog;
import com.patho.main.action.dialog.task.CreateTaskDialog;
import com.patho.main.action.dialog.task.DeleteTaskEntityDialog;

import lombok.Getter;

@Component("dialog")
@Getter
@Scope(value = "session")
public class DialogHandler {

	private AddSlidesDialog addSlidesDialog = new AddSlidesDialog();
	private CreateSampleDialog createSampleDialog = new CreateSampleDialog();
	private SlideNamingDialog slideNamingDialog = new SlideNamingDialog();
	private DeleteTaskEntityDialog deleteTaskEntityDialog = new DeleteTaskEntityDialog();
	private StainingPhaseExitDialog stainingPhaseExitDialog = new StainingPhaseExitDialog();
	private SlideOverviewDialog slideOverviewDialog = new SlideOverviewDialog();
	private QuickAddDiangosisRevisionDialog quickAddDiangosisRevisionDialog = new QuickAddDiangosisRevisionDialog();
	private CreateTaskDialog createTaskDialog = new CreateTaskDialog();
	private SearchPatientDialog searchPatientDialog = new SearchPatientDialog();
	private PDFOrganizer pdfOrganizer = new PDFOrganizer();
	private UploadDialog uploadDialog = new UploadDialog();
	private EditPDFDialog editPDFDialog = new EditPDFDialog();
	private DeletePDFDialog deletePDFDialog = new DeletePDFDialog();
	private DiagnosisPhaseExitDialog diagnosisPhaseExitDialog = new DiagnosisPhaseExitDialog();
	private PrintDialog printDialog = new PrintDialog();
	private CreateDiagnosisRevisionDialog createDiagnosisRevisionDialog = new CreateDiagnosisRevisionDialog();
	private EditDiagnosisRevisionsDialog editDiagnosisRevisionsDialog = new EditDiagnosisRevisionsDialog();
	private DeleteDiagnosisRevisionDialog deleteDiagnosisRevisionDialog = new DeleteDiagnosisRevisionDialog();
	private PatientLogDialog patientLogDialog = new PatientLogDialog();
	private ChangeTaskIDDialog changeTaskIDDialog = new ChangeTaskIDDialog();
	private AccountingDataDialog accountingDataDialog = new AccountingDataDialog();
	private ContactDialog contactDialog = new ContactDialog();
	private ContactSelectDialog contactSelectDialog = new ContactSelectDialog();
	private ContactNotificationDialog contactNotificationDialog = new ContactNotificationDialog();
	private CopyHistologicalRecordDialog copyHistologicalRecordDialog = new CopyHistologicalRecordDialog();
	private ProgrammVersionDialog programmVersionDialog = new ProgrammVersionDialog();
	private CustomAddressDialog customAddressDialog = new CustomAddressDialog();
	private CouncilDialog councilDialog = new CouncilDialog();
	private SettingsDialog settingsDialog = new SettingsDialog();
	private FavouriteListEditDialog favouriteListEditDialog = new FavouriteListEditDialog();
	private NotificationDialog notificationDialog = new NotificationDialog();
	private FaxDocumentDialog faxDocumentDialog = new FaxDocumentDialog();
	private BioBankDialog bioBankDialog = new BioBankDialog();
	private EditUserDialog editUserDialog = new EditUserDialog();
	private OrganizationListDialog organizationListDialog = new OrganizationListDialog();
	private OrganizationEditDialog organizationEditDialog = new OrganizationEditDialog();
	private PhysicianEditDialog physicianEditDialog = new PhysicianEditDialog();
	private PhysicianSearchDialog physicianSearchDialog = new PhysicianSearchDialog();
	private ListItemEditDialog listItemEditDialog = new ListItemEditDialog();
	private StainingEditDialog stainingEditDialog = new StainingEditDialog();
	private MaterialEditDialog materialEditDialog = new MaterialEditDialog();
	private DiagnosisPresetEditDialog diagnosisPresetEditDialog = new DiagnosisPresetEditDialog();
	private GroupEditDialog groupEditDialog = new GroupEditDialog();
	private UserListDialog userListDialog = new UserListDialog();
	private GroupListDialog groupListDialog = new GroupListDialog();
	private UserSettingsDialog userSettingsDialog = new UserSettingsDialog();
	private ConfirmUserDeleteDialog confirmUserDeleteDialog = new ConfirmUserDeleteDialog();
}
