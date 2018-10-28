package com.patho.main.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.patho.main.common.ContactRole;
import com.patho.main.model.AssociatedContactNotification;
import com.patho.main.repository.MediaRepository;
import com.patho.main.util.helper.StreamUtils;
import com.patho.main.util.version.Version;
import com.patho.main.util.version.VersionContainer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "patho.settings")
public class PathoConfig {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String PDF_NOT_FOUND_PDF = "classpath:templates/print/pdfnotfound.pdf";
	public static final String PDF_NOT_FOUND_IMG = "classpath:templates/print/pdfnotfound.png";
	
	public static final String REPORT_NOT_APPROVED_PDF= "classpath:templates/print/reportnotapproved.pdf";
	public static final String REPORT_NOT_APPROVED_IMG= "classpath:templates/print/reportnotapproved.png";
	
	public static final String RENDER_ERROR_PDF= "classpath:templates/print/pdfnotfound.pdf";
	
	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Lazy
	private MediaRepository mediaRepository;
	
	private DefaultNotification defaultNotification;

	private DefaultDocuments defaultDocuments;

	private FileSettings fileSettings;
	
	private Miscellaneous miscellaneous;
	/**
	 * Container for providing version information
	 */
	private VersionContainer versionContainer;
	
	@PostConstruct
	private void initilizePathoFroh() {

		File fileRepository = new File(fileSettings.getFileRepository());

		// checking directories
		if (!fileRepository.isDirectory() && !fileRepository.mkdirs()) {
			logger.error("Error directory not found: fileRepository " + fileRepository.getAbsolutePath());
		}

		File workDirectory = mediaRepository.getWriteFile(fileSettings.getWorkDirectory());

		// checking directories
		if (!workDirectory.isDirectory() && !workDirectory.mkdirs()) {
			logger.error("Error directory not found: workDirectory " + workDirectory.getAbsolutePath());
		}

		File auxDirectory = mediaRepository.getWriteFile(fileSettings.getAuxDirectory());

		// checking directories
		if (!auxDirectory.isDirectory() && !auxDirectory.mkdirs()) {
			logger.error("Error directory not found: auxDirectory " + auxDirectory.getAbsolutePath());
		}

		File errorDirectory = mediaRepository.getWriteFile(fileSettings.getErrorDirectory());

		// checking directories
		if (!errorDirectory.isDirectory() && !errorDirectory.mkdirs()) {
			logger.error("Error directory not found: errorDirectory " + errorDirectory.getAbsolutePath());
		}	
		
		File printDirectory = mediaRepository.getWriteFile(fileSettings.getPrintDirectory());

		// checking directories
		if (!printDirectory.isDirectory() && !printDirectory.mkdirs()) {
			logger.error("Error directory not found: errorDirectory " + printDirectory.getAbsolutePath());
		}	
		
		logger.debug("Copying files to working directory...");
		for (String copyStr : fileSettings.getCopyFromClasspathToWorkDirectory()) {
			logger.debug("Copying files from '"+ copyStr +"' to working directory...");
			File[] printResouces = mediaRepository.getFilesOfDirectory(copyStr);
			
			for (int i = 0; i < printResouces.length; i++) {
				try {
					FileUtils.copyFileToDirectory(printResouces[i], workDirectory);
					logger.debug("Copying... " + printResouces[i].getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// loading versions
		List<Version> versions = Version.factroy(mediaRepository.getStrings(fileSettings.getProgramVersionInfo()));
		setVersionContainer(new VersionContainer(versions));
		
		// setting current version
		if (versions != null && versions.size() > 0) {
			getVersionContainer().setCurrentVersion(versions.get(0).getVersion());
		}
		
		if((new File(fileSettings.getProgramInfo()).exists())){
			String programVersionJson = mediaRepository.getString(fileSettings.getProgramInfo());
		}else {
			logger.debug("First program start");
		}
		
	}

	@Getter
	@Setter
	public static class DefaultNotification {

		private List<DefaultNotificationEntity> defaultNotifications;

		public List<AssociatedContactNotification.NotificationTyp> getDefaultNotificationForRole(ContactRole role) {
			if (defaultNotifications != null) {
				try {
					return defaultNotifications.stream().filter(p -> p.getRole().equals(role))
							.collect(StreamUtils.singletonCollector()).getNotificationTyps();
				} catch (IllegalStateException e) {
					// returning empty list
				}
			}

			return new ArrayList<AssociatedContactNotification.NotificationTyp>();
		}
	}

	@Getter
	@Setter
	public static class DefaultNotificationEntity {
		private ContactRole role;
		private List<AssociatedContactNotification.NotificationTyp> notificationTyps;
	}

	@Getter
	@Setter
	public static class DefaultDocuments {

		/**
		 * Document-Template which is used on diagnosis phase exit.
		 */
		private long diagnosisApprovedDocument;

		/**
		 * Document which can be printed on task creation.
		 */
		private long taskCreationDocument;

		/**
		 * Default document for email notification
		 */
		private long notificationDefaultEmailDocument;

		/**
		 * Default Email template which is used to notify physicians if task was
		 * completed
		 */
		private long notificationDefaultEmail;

		/**
		 * Default document for fax notification
		 */
		private long notificationDefaultFaxDocument;

		/**
		 * Default document for letter notification
		 */
		private long notificationDefaultLetterDocument;

		/**
		 * Default document for printing in order to sign
		 */
		private long notificationDefaultPrintDocument;

		/**
		 * Sendreport which is created after the notification dialog was processed.
		 */
		private long notificationSendReport;

		/**
		 * ID of the default slide label
		 */
		private long slideLabelDocument;

		/**
		 * Template for diagnosis report for program users
		 */
		private long diagnosisReportForUsers;

		/**
		 * ID of the testlabel for slide printing
		 */
		private long slideLableTestDocument;

		/**
		 * ID of the test page for document printing
		 */
		private long printerTestDocument;

		/**
		 * Path of one empty pdf page
		 */
		private String emptyPage;

		/**
		 * Testpage for label printer
		 */
		private String lablePrinterTestPage;

		/**
		 * Test page for cups printer
		 */
		private String cupsPrinterTestPage;
	}

	@Getter
	@Setter
	public static class FileSettings {

		public static final String FILE_REPOSITORY_PATH_TOKEN = "fileRepository:";

		private String fileRepository;

		private String workDirectory;

		private String auxDirectory;

		private String errorDirectory;

		private String printDirectory;

		private String programInfo;

		private String programVersionInfo;

		private String[] copyFromClasspathToWorkDirectory;
		
		private int thumbnailDPI;

		private boolean cleanup;

		private boolean keepErrorFiles;
	}

	@Getter
	@Setter
	public static class ProgramInfo{
		private String version;
	}

	@Getter
	@Setter
	public static class Miscellaneous{
		String phoneRegex;
	}
}