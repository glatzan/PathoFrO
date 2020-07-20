package com.patho.main.ui.selectors;

import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.notification.ReportIntent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class AssociatedContactSelector extends AbstractSelector {

    private ReportIntent contact;
    private boolean deleteAble;

    protected AssociatedContactSelector(ReportIntent contact) {
        this.contact = contact;
        // deletion only possible if no notification was performed
//		setDeleteAble(
//////				contact.getNotifications() != null ? !contact.getNotifications().stream().anyMatch(p -> p.getPerformed())
//////						: true);
        setDeleteAble(false);
    }

    public static List<AssociatedContactSelector> factory(Task task) {
        return task.getContacts().stream().map(p -> new AssociatedContactSelector(p)).collect(Collectors.toList());
    }
}
