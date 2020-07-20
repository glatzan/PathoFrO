package com.patho.main.service;

import com.patho.main.common.View;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.jpa.GroupRepository;
import com.patho.main.util.search.settings.SimpleListSearchOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional
public class GroupService extends AbstractService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserService userService;

    public HistoGroup addOrUpdate(HistoGroup g) {

        boolean newList = g.getId() == 0;

        g = groupRepository.save(g,
                resourceBundle.get(newList ? "log.settings.group.new" : "log.settings.group.edit", g.getName()));

        // updating user with groupsettings
        if (!newList) {
            userService.updateGroupOfUser(g);
        }

        return g;
    }

    /**
     * Tries to delete the old group
     */
    @Transactional(propagation = Propagation.NEVER)
    public boolean deleteOrArchive(HistoGroup g) {
        try {
            groupRepository.delete(g, resourceBundle.get("log.settings.group.deleted", g.getName()));
            return true;
        } catch (Exception e) {
            archive(g, true);
            return false;
        }
    }

    /**
     * Archived or dearchives a group
     *
     * @param archive
     * @return
     */
    public HistoGroup archive(HistoGroup g, boolean archive) {
        g.setArchived(archive);
        return groupRepository.save(g, resourceBundle
                .get(archive ? "log.settings.group.archived" : "log.settings.group.dearchived", g.getName()));
    }

    /**
     * Copies crucial settings from group settings to user settings
     */
    public static void copyGroupSettings(HistoUser user, HistoGroup group, boolean overwrite) {

        user.setArchived(group.getUserDeactivated());

        user.getSettings().setAvailableViews(new ArrayList<View>(group.getSettings().getAvailableViews()));
        user.getSettings().setDefaultView(group.getSettings().getDefaultView());
        user.getSettings().setStartView(group.getSettings().getStartView());

        if (user.getSettings().getInputFieldColor() == null || overwrite)
            user.getSettings().setInputFieldColor(group.getSettings().getInputFieldColor());

        if (user.getSettings().getInputFieldFontColor() == null || overwrite)
            user.getSettings().setInputFieldFontColor(group.getSettings().getInputFieldFontColor());

        user.getSettings()
                .setAvailableWorklists(new ArrayList<SimpleListSearchOption>(group.getSettings().getAvailableWorklists()));

        user.getSettings().setWorklistToLoad(group.getSettings().getWorklistToLoad());

        user.getSettings().setWorklistSortOrder(group.getSettings().getWorklistSortOrder());

        user.getSettings().setWorklistSortOrderAsc(group.getSettings().getWorklistSortOrderAsc());

        user.getSettings().setWorklistHideNoneActiveTasks(group.getSettings().getWorklistHideNoneActiveTasks());

        user.getSettings().setWorklistAutoUpdate(group.getSettings().getWorklistAutoUpdate());

        user.getSettings().setAlternatePatientAddMode(group.getSettings().getAlternatePatientAddMode());

        user.getSettings().setAddTaskWithSingleClick(group.getSettings().getAddTaskWithSingleClick());

    }

    /**
     * Copies only view settings to the user settings
     */
    public static void copyUpdatedGroupSettings(HistoUser user, HistoGroup group) {

        user.getSettings().setAvailableViews(new ArrayList<View>(group.getSettings().getAvailableViews()));
        user.getSettings().setDefaultView(group.getSettings().getDefaultView());
        user.getSettings().setStartView(group.getSettings().getStartView());

        user.getSettings()
                .setAvailableWorklists(new ArrayList<SimpleListSearchOption>(group.getSettings().getAvailableWorklists()));

        user.getSettings().setWorklistToLoad(group.getSettings().getWorklistToLoad());
    }

}
