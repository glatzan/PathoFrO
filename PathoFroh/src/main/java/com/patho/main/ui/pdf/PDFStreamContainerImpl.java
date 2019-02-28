package com.patho.main.ui.pdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.model.PDFContainer;
import com.patho.main.repository.MediaRepository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Default implementation of the PDFStreamContainer
 * 
 * @author Andreas
 *
 */
@Configurable
@Getter
@Setter
public class PDFStreamContainerImpl implements PDFStreamContainer {

	@Autowired
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	protected PDFContainer displayPDF;

	protected PDFContainer tooltip;

}