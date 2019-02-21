package com.patho.main.util.notification;

import com.patho.main.model.patient.notification.ReportIntent;
import com.patho.main.model.patient.notification.ReportIntentNotification;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.util.printer.TemplatePDFContainer;
import lombok.Getter;
import lombok.Setter;

/**
 * Class for encapsulating notifications, used for performing the notification
 * task.
 *
 * @author andi
 */
@Getter
@Setter
public class NotificationContainer {

    /**
     * If true the notification should be performed
     */
    protected boolean perform;

    /**
     * contact
     */
    protected ReportIntent contact;

    /**
     * Notification
     */
    protected ReportIntentNotification notification;

    /**
     * The individual pdf is saved here
     */
    protected TemplatePDFContainer pdf;

    /**
     * Address, copied form contact
     */
    protected String contactAddress;

    /**
     * True if the notification failed on a previous notification attempt
     */
    protected boolean faildPreviously;

    /**
     * True if e.g. the address is not correct
     */
    protected boolean warning;

    /**
     * Warning text
     */
    protected String warningInfo;

    /**
     * If true this is an already performed notification which should be resend.
     */
    protected boolean recreateBeforeUsage;

    public NotificationContainer(ReportIntent contact, ReportIntentNotification notification) {
        this(contact, notification, false);
    }

    public NotificationContainer(ReportIntent contact, ReportIntentNotification notification,
                                 boolean recreateBeforeUsage) {
        update(contact, notification);
        this.recreateBeforeUsage = recreateBeforeUsage;
        initAddressForNotificationType();
    }

    public long getId() {
        return getNotification().getId();
    }

    public void update(ReportIntent contact, ReportIntentNotification notification) {
        this.contact = contact;
        this.notification = notification;
        this.faildPreviously = notification.getFailed();
        this.perform = true;
    }

    /**
     * Copies the notification address if failed from notification (for user to
     * correct it) or if first try from contact.
     */
    public void initAddressForNotificationType() {
        if (!notification.getFailed() && !notification.getRenewed()) {
            switch (notification.getNotificationTyp()) {
                case EMAIL:
                    setContactAddress(contact.getPerson().getContact().getEmail());
                    break;
                case FAX:
                    setContactAddress(contact.getPerson().getContact().getFax());
                    break;
                case PHONE:
                    setContactAddress(contact.getPerson().getContact().getPhone());
                    break;
                case LETTER:
                    setContactAddress(AssociatedContactService.generateAddress(getContact(),
                            getContact().getPerson().getDefaultAddress()));
                default:
                    break;
            }
        } else {
            setContactAddress(notification.getContactAddress());
        }
    }

    /**
     * Clears the warning
     */
    public void clearWarning() {
        setWarning(false, "");
    }

    /**
     * Sets warning and the warning info
     *
     * @param warning
     * @param info
     */
    public void setWarning(boolean warning, String info) {
        setWarning(warning);
        setWarningInfo(info);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NotificationContainer) {
            NotificationContainer no = (NotificationContainer) obj;
            return getContact().equals(no.getContact()) && getNotification().equals(no.getNotification());
        }
        return super.equals(obj);
    }

}
