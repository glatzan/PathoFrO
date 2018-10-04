package com.patho.main.action.handler;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.patho.main.adaptors.FaxHandler;
import com.patho.main.adaptors.MailHandler;
import com.patho.main.model.transitory.DefaultDocuments;
import com.patho.main.model.transitory.ProgramSettings;
import com.patho.main.util.helper.FileUtil;
import com.patho.main.util.version.VersionContainer;

import lombok.Getter;
import lombok.Setter;

@Component
@Scope(value = "singleton")
@Getter
@Setter
public class GlobalSettings {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static final String HISTO_BASE_URL = "/Histo2016";
	public static final String HISTO_LOGIN_PAGE = "/login.xhtml";

	public static final String PROGRAM_SETTINGS = "classpath:settings/general.json";
	public static final String SETTINGS_GENERAL = "generalSettings";
	public static final String SETTINGS_DEFAULT_DOCUMENTS = "defaultDocuments";
	public static final String SETTINGS_DEFAULT_NOTIFICATION = "defaultNotification";
	public static final String SETTINGS_LDAP = "ldapSettings";
	public static final String SETTINGS_MAIL = "mail";
	public static final String SETTINGS_FAX = "fax";
	public static final String SETTINGS_CUPS_SERVER = "cupsServer";
	public static final String SETTINGS_LABLE_PRINTERS = "labelPrinters";
	public static final String SETTINGS_CLINIC_BACKEND = "clinicBackend";
	public static final String SETTINGS_PRINTER_FOR_ROOM = "findPrinterForRoomBackend";

	public static final String VERSIONS_INFO = "classpath:settings/version.txt";
	public static final String MAIL_TEMPLATES = "classpath:settings/mailTemplates.json";
	public static final String PRINT_TEMPLATES = "classpath:settings/printTempaltes.json";
	/**
	 * Default program settings
	 */
	private ProgramSettings programSettings;

	/**
	 * List of default documents
	 */
	private DefaultDocuments defaultDocuments;



	/**
	 * Object for handling mails
	 */
	private MailHandler mailHandler;

	/**
	 * Object for handling mails
	 */
	private FaxHandler faxHandler;

	/**
	 * The current version of the program
	 */
	private String currentVersion;

	/**
	 * Container for providing version information
	 */
	private VersionContainer versionContainer;

	@Autowired
	private ResourceLoader resourceLoader;
	
	@PostConstruct
	public void initBean() {
		Gson gson = new Gson();

		final Resource fileResource = resourceLoader.getResource(PROGRAM_SETTINGS);
		
		System.out.println(fileResource);
		
		JsonParser parser = new JsonParser();
		JsonObject o = parser.parse(FileUtil.getContentOfFile(fileResource)).getAsJsonObject();

		programSettings = gson.fromJson(o.get(SETTINGS_GENERAL), ProgramSettings.class);

		mailHandler = gson.fromJson(o.get(SETTINGS_MAIL), MailHandler.class);

		faxHandler = gson.fromJson(o.get(SETTINGS_FAX), FaxHandler.class);

		defaultDocuments = gson.fromJson(o.get(SETTINGS_DEFAULT_DOCUMENTS), DefaultDocuments.class);

	}

}
