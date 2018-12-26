package com.patho.main.ui.pdf;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletResponse;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.config.PathoConfig;
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