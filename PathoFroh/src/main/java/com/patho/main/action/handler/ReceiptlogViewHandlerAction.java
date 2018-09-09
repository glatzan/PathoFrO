package com.patho.main.action.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Transient;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.patho.main.action.DialogHandlerAction;
import com.patho.main.action.MainHandlerAction;
import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.diagnosis.QuickAddDiangosisRevisionDialog;
import com.patho.main.action.dialog.slides.AddSlidesDialog.SlideSelectResult;
import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;
import com.patho.main.service.SampleService;
import com.patho.main.template.print.SlideLable;
import com.patho.main.ui.StainingTableChooser;
import com.patho.main.util.exception.CustomUserNotificationExcepetion;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Controller
@Scope("session")
@Getter
@Setter
@Slf4j
public class ReceiptlogViewHandlerAction {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MainHandlerAction mainHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Lazy
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private SampleService sampleService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GlobalSettings globalSettings;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private QuickAddDiangosisRevisionDialog addDiangosisReviosionDialog;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Lazy
	private GlobalEditViewHandler globalEditViewHandler;
	
	/**
	 * Prints a lable for the choosen slide.
	 * 
	 * @param slide
	 */
	public void printLableForSlide(Slide slide) {

		SlideLable slideLabel = DocumentTemplate
				.getTemplateByID(globalSettings.getDefaultDocuments().getSlideLabelDocument());

		if (slideLabel == null) {
			log.debug("No template found for printing, returning!");
			return;
		}

		slideLabel.initData(slide.getTask(), slide, new Date(System.currentTimeMillis()));
		slideLabel.fillTemplate();

		userHandlerAction.getSelectedLabelPrinter().print(slideLabel);

	}

}
