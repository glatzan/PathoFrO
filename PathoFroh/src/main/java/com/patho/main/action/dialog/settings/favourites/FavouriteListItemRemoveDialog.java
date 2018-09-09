package com.patho.main.action.dialog.settings.favourites;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.common.DateFormat;
import com.patho.main.common.Dialog;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.FavouriteListRepository;
import com.patho.main.service.FavouriteListService;
import com.patho.main.template.Template;
import com.patho.main.util.helper.TimeUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope(value = "session")
@Setter
@Getter
public class FavouriteListItemRemoveDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Lazy
	private GlobalEditViewHandler globalEditViewHandler;

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
			Template.initVelocity();

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
		favouriteListService.removeTaskFromList(task.getId(), favouriteList.getId());
		mainHandlerAction.sendGrowlMessagesAsResource("growl.favouriteList.removed", "growl.favouriteList.removed.text",
				new Object[] { task.getTaskID(), favouriteList.getName() });
	}

	@Transactional
	public void moveTaskToList() {
		favouriteListService.moveTaskToList(favouriteList.getId(), favouriteList.getDumpList().getId(), task.getId(), getCommentary());
		mainHandlerAction.sendGrowlMessagesAsResource("growl.favouriteList.move", "growl.favouriteList.move.text",
				new Object[] { task.getTaskID(), favouriteList.getDumpList().getName() });
	}

}
