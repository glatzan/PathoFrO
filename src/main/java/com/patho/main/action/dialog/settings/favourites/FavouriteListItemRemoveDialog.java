package com.patho.main.action.dialog.settings.favourites;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.DateFormat;
import com.patho.main.common.Dialog;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.jpa.FavouriteListRepository;
import com.patho.main.service.FavouriteListService;
import com.patho.main.template.AbstractTemplate;
import com.patho.main.util.helper.TimeUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.util.Date;

@Component
@Scope(value = "session")
@Setter
@Getter
public class FavouriteListItemRemoveDialog extends AbstractDialog {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private FavouriteListRepository favouriteListRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private FavouriteListService favouriteListService;

    private FavouriteList favouriteList;

    private String commentary;

    public void initAndPrepareBean(Task task, Long favouriteListID) {
        if (initBean(task, favouriteListID))
            prepareDialog();
    }

    public void initAndPrepareBean(FavouriteList favouriteList, Task task) {
        if (initBean(favouriteList, task))
            prepareDialog();
    }

    public boolean initBean(Task task, Long favouriteListID) {
        FavouriteList list = favouriteListRepository.findOptionalByIdAndInitialize(favouriteListID, false, false, true)
                .orElse(new FavouriteList());
        return initBean(list, task);
    }

    public boolean initBean(FavouriteList favouriteList, Task task) {

        this.favouriteList = favouriteList;
        this.task = task;

        if (favouriteList.getDumpCommentary() != null) {
            AbstractTemplate.initVelocity();

            /* create a context and add data */
            VelocityContext context = new VelocityContext();

            context.put("date", TimeUtil.formatDate(new Date(), DateFormat.GERMAN_DATE.getDateFormat()));
            context.put("time", TimeUtil.formatDate(new Date(), DateFormat.TIME.getDateFormat()));
            context.put("oldList", favouriteList.getName());

            /* now render the template into a StringWriter */
            StringWriter writer = new StringWriter();

            Velocity.evaluate(context, writer, "", favouriteList.getDumpCommentary());

            this.commentary = writer.toString();
        } else
            this.commentary = "";

        super.initBean(task, Dialog.FAVOURITE_LIST_ITEM_REMOVE);
        return true;

    }

    @Transactional
    public void removeTaskFromList() {
//		favouriteListService.removeTaskFromList(task.getId(), favouriteList.getId());
//		mainHandlerAction.sendGrowlMessagesAsResource("growl.favouriteList.removed.headline", "growl.favouriteList.removed.headline.text",
//				new Object[] { task.getTaskID(), favouriteList.getName() });
    }

    @Transactional
    public void moveTaskToList() {
//		// is combien in removeList (removes to dumplist per default)
//		favouriteListService.moveTaskToList(favouriteList.getId(), favouriteList.getDumpList().getId(), task.getId(), getCommentary());
//		mainHandlerAction.sendGrowlMessagesAsResource("growl.favouriteList.move", "growl.favouriteList.move.text",
//				new Object[] { task.getTaskID(), favouriteList.getDumpList().getName() });
    }

}
