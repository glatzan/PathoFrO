package com.patho.main.action.dialog;

import com.patho.main.common.DateFormat;
import com.patho.main.common.Dialog;
import com.patho.main.config.PathoConfig;
import com.patho.main.util.config.VersionContainer;
import com.patho.main.util.helper.TimeUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Date;
import java.util.List;

@Configurable
@Getter
@Setter
public class ProgrammVersionDialog extends AbstractTabDialog {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private PathoConfig pathoConfig;

    private VersionTab versionTab;
    private ErrorTab errorTab;
    private InfoTab infoTab;
    private AboutTab aboutTab;

    public ProgrammVersionDialog() {
        setVersionTab(new VersionTab());
        setErrorTab(new ErrorTab());
        setAboutTab(new AboutTab());
        setInfoTab(new InfoTab());

        tabs = new AbstractTab[]{versionTab, errorTab, infoTab, aboutTab};
    }

    public ProgrammVersionDialog initAndPrepareBean() {
        if (initBean())
            prepareDialog();
        return this;
    }

    public boolean initBean() {
        return super.initBean(Dialog.INFO);
    }

    @Getter
    @Setter
    public class VersionTab extends AbstractTab {

        private List<VersionContainer.Version> versionInfo;

        public VersionTab() {
            setTabName("VersionTab");
            setName("dialog.info.version.headline");
            setViewID("versionTab");
            setCenterInclude("include/version.xhtml");
        }

        public boolean initTab() {
            setVersionInfo(pathoConfig.getVersionContainer().getVersions());
            return true;
        }

    }

    @Getter
    @Setter
    public class ErrorTab extends AbstractTab {

        private String errorMessage;

        private Date errorDate;

        public ErrorTab() {
            setTabName("ErrorTab");
            setName("dialog.info.error.headline");
            setViewID("errorTab");
            setCenterInclude("include/error.xhtml");
        }

        public boolean initTab() {
            setErrorDate(new Date(System.currentTimeMillis()));
            setErrorMessage("");
            return true;
        }

        public void sendErrorMessage() {
            // TODO rewrite if email system is updated
            logger.debug("Sending Error DiagnosisRevision Date {} Message {}",
                    TimeUtil.formatDate(errorDate, DateFormat.GERMAN_DATE_TIME.getDateFormat()), errorMessage);

            if (errorMessage != null && !errorMessage.isEmpty() && errorMessage != null) {

                // ErrorMail mail = MailHandler.getDefaultTemplate(ErrorMail.class);
                // mail.prepareTemplate(userHandlerAction.getCurrentUser(), errorMessage,
                // new Date(System.currentTimeMillis()));
                // mail.fillTemplate();
                //
                // globalSettings.getMailHandler().sendAdminMail(mail);
                //
                // initTab();
                //
                // mainHandlerAction.sendGrowlMessagesAsResource("growl.mail.sendErrorMail",
                // "growl.mail.sendErrorMail.text");
            }
        }

    }

    public class InfoTab extends AbstractTab {
        public InfoTab() {
            setTabName("InfoTab");
            setName("dialog.info.info.headline");
            setViewID("infoTab");
            setCenterInclude("include/info.xhtml");
        }
    }

    @Getter
    @Setter
    public class AboutTab extends AbstractTab {

        public AboutTab() {
            setTabName("AboutTab");
            setName("dialog.info.about.headline");
            setViewID("aboutTab");
            setCenterInclude("include/about.xhtml");
        }

    }
}
