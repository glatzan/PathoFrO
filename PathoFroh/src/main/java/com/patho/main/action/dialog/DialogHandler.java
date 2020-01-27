package com.patho.main.action.dialog;

import com.patho.main.action.dialog.biobank.BioBankDialog;
import com.patho.main.action.dialog.diagnosis.*;
import com.patho.main.action.dialog.media.EditPDFDialog;
import com.patho.main.action.dialog.media.UploadDialog;
import com.patho.main.action.dialog.miscellaneous.AccountingDataDialog;
import com.patho.main.action.dialog.miscellaneous.ConfirmDialog;
import com.patho.main.action.dialog.notification.ContactDialog;
import com.patho.main.action.dialog.notification.ContactNotificationDialog;
import com.patho.main.action.dialog.patient.*;
import com.patho.main.action.dialog.print.CustomAddressDialog;
import com.patho.main.action.dialog.print.FaxDocumentDialog;
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
import com.patho.main.action.dialog.task.ChangeTaskIDDialog;
import com.patho.main.action.dialog.task.CreateSampleDialog;
import com.patho.main.action.dialog.task.CreateTaskDialog;
import com.patho.main.action.dialog.worklist.WorklistSettingsDialog;
import com.patho.main.action.dialog.worklist.WorklistSortDialog;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("dialog")
@Scope(value = "session")
@Deprecated
public class DialogHandler {

    private AddSlidesDialog addSlidesDialog = new AddSlidesDialog();
    private CreateSampleDialog createSampleDialog = new CreateSampleDialog();
    private SlideNamingDialog slideNamingDialog = new SlideNamingDialog();
    private CreateTaskDialog createTaskDialog = new CreateTaskDialog();
    private SearchPatientDialog searchPatientDialog = new SearchPatientDialog();
    private UploadDialog uploadDialog = new UploadDialog();
    private EditPDFDialog editPDFDialog = new EditPDFDialog();
    private DiagnosisPhaseExitDialog diagnosisPhaseExitDialog = new DiagnosisPhaseExitDialog();
    private CreateDiagnosisRevisionDialog createDiagnosisRevisionDialog = new CreateDiagnosisRevisionDialog();
    private EditDiagnosisRevisionsDialog editDiagnosisRevisionsDialog = new EditDiagnosisRevisionsDialog();
    private DeleteDiagnosisRevisionDialog deleteDiagnosisRevisionDialog = new DeleteDiagnosisRevisionDialog();
    private PatientLogDialog patientLogDialog = new PatientLogDialog();
    private ChangeTaskIDDialog changeTaskIDDialog = new ChangeTaskIDDialog();
    private AccountingDataDialog accountingDataDialog = new AccountingDataDialog();
    private ContactDialog contactDialog = new ContactDialog();
    private ContactNotificationDialog contactNotificationDialog = new ContactNotificationDialog();
    private ProgrammVersionDialog programmVersionDialog = new ProgrammVersionDialog();
    private CustomAddressDialog customAddressDialog = new CustomAddressDialog();
    private SettingsDialog settingsDialog = new SettingsDialog();
    private FavouriteListEditDialog favouriteListEditDialog = new FavouriteListEditDialog();
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
    private WorklistSettingsDialog worklistSettingsDialog = new WorklistSettingsDialog();
    private WorklistSortDialog worklistSortDialog = new WorklistSortDialog();
    private MergePatientDialog mergePatientDialog = new MergePatientDialog();
    private EditPatientDialog editPatientDialog = new EditPatientDialog();
    private DeletePatientDialog deletePatientDialog = new DeletePatientDialog();
    private ConfirmDialog confirmDialog = new ConfirmDialog();

    public AddSlidesDialog getAddSlidesDialog() {
        return this.addSlidesDialog;
    }

    public CreateSampleDialog getCreateSampleDialog() {
        return this.createSampleDialog;
    }

    public SlideNamingDialog getSlideNamingDialog() {
        return this.slideNamingDialog;
    }

    public CreateTaskDialog getCreateTaskDialog() {
        return this.createTaskDialog;
    }

    public SearchPatientDialog getSearchPatientDialog() {
        return this.searchPatientDialog;
    }

    public UploadDialog getUploadDialog() {
        return this.uploadDialog;
    }

    public EditPDFDialog getEditPDFDialog() {
        return this.editPDFDialog;
    }


    public DiagnosisPhaseExitDialog getDiagnosisPhaseExitDialog() {
        return this.diagnosisPhaseExitDialog;
    }

    public CreateDiagnosisRevisionDialog getCreateDiagnosisRevisionDialog() {
        return this.createDiagnosisRevisionDialog;
    }

    public EditDiagnosisRevisionsDialog getEditDiagnosisRevisionsDialog() {
        return this.editDiagnosisRevisionsDialog;
    }

    public DeleteDiagnosisRevisionDialog getDeleteDiagnosisRevisionDialog() {
        return this.deleteDiagnosisRevisionDialog;
    }

    public PatientLogDialog getPatientLogDialog() {
        return this.patientLogDialog;
    }

    public ChangeTaskIDDialog getChangeTaskIDDialog() {
        return this.changeTaskIDDialog;
    }

    public AccountingDataDialog getAccountingDataDialog() {
        return this.accountingDataDialog;
    }

    public ContactDialog getContactDialog() {
        return this.contactDialog;
    }

    public ContactNotificationDialog getContactNotificationDialog() {
        return this.contactNotificationDialog;
    }

    public ProgrammVersionDialog getProgrammVersionDialog() {
        return this.programmVersionDialog;
    }

    public CustomAddressDialog getCustomAddressDialog() {
        return this.customAddressDialog;
    }

    public SettingsDialog getSettingsDialog() {
        return this.settingsDialog;
    }

    public FavouriteListEditDialog getFavouriteListEditDialog() {
        return this.favouriteListEditDialog;
    }

    public FaxDocumentDialog getFaxDocumentDialog() {
        return this.faxDocumentDialog;
    }

    public BioBankDialog getBioBankDialog() {
        return this.bioBankDialog;
    }

    public EditUserDialog getEditUserDialog() {
        return this.editUserDialog;
    }

    public OrganizationListDialog getOrganizationListDialog() {
        return this.organizationListDialog;
    }

    public OrganizationEditDialog getOrganizationEditDialog() {
        return this.organizationEditDialog;
    }

    public PhysicianEditDialog getPhysicianEditDialog() {
        return this.physicianEditDialog;
    }

    public PhysicianSearchDialog getPhysicianSearchDialog() {
        return this.physicianSearchDialog;
    }

    public ListItemEditDialog getListItemEditDialog() {
        return this.listItemEditDialog;
    }

    public StainingEditDialog getStainingEditDialog() {
        return this.stainingEditDialog;
    }

    public MaterialEditDialog getMaterialEditDialog() {
        return this.materialEditDialog;
    }

    public DiagnosisPresetEditDialog getDiagnosisPresetEditDialog() {
        return this.diagnosisPresetEditDialog;
    }

    public GroupEditDialog getGroupEditDialog() {
        return this.groupEditDialog;
    }

    public UserListDialog getUserListDialog() {
        return this.userListDialog;
    }

    public GroupListDialog getGroupListDialog() {
        return this.groupListDialog;
    }

    public UserSettingsDialog getUserSettingsDialog() {
        return this.userSettingsDialog;
    }

    public ConfirmUserDeleteDialog getConfirmUserDeleteDialog() {
        return this.confirmUserDeleteDialog;
    }

    public WorklistSettingsDialog getWorklistSettingsDialog() {
        return this.worklistSettingsDialog;
    }

    public WorklistSortDialog getWorklistSortDialog() {
        return this.worklistSortDialog;
    }

    public MergePatientDialog getMergePatientDialog() {
        return this.mergePatientDialog;
    }

    public EditPatientDialog getEditPatientDialog() {
        return this.editPatientDialog;
    }

    public DeletePatientDialog getDeletePatientDialog() {
        return this.deletePatientDialog;
    }

    public ConfirmDialog getConfirmDialog() {
        return this.confirmDialog;
    }

}
