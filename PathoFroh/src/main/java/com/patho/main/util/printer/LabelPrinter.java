package com.patho.main.util.printer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.gson.annotations.Expose;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.config.PathoConfig;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.exception.CustomUserNotificationExcepetion;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Zebra AbstractPrinter with ftp printing function. Buffer can be filled
 * without opening a connection with the printer.
 * 
 * @author andi
 *
 */
@Getter
@Setter
@Configurable
public class LabelPrinter extends AbstractPrinter {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PrintDocumentRepository PrintDocumentRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PathoConfig pathoConfig;

	/**
	 * Default name of the ftp uploaded file. Should contain %counter% in order to
	 * print multiple files.
	 */

	@Expose
	private int timeout;

	public void print(PrintDocument... tempaltes) throws CustomUserNotificationExcepetion {
		String[] arr = new String[tempaltes.length];

		for (int i = 0; i < tempaltes.length; i++) {
			arr[i] = tempaltes[i].getFinalContent();
		}

		print(arr);
	}

	public void print(List<String> tempaltes) throws CustomUserNotificationExcepetion {
		String[] array = new String[tempaltes.size()];
		print(tempaltes.toArray(array));
	}

	public void print(String... tempaltes) throws CustomUserNotificationExcepetion {
		try {
			FTPClient connection = openConnection();
			for (String content : tempaltes) {
				print(connection, content, generateUnqiueName(6));
			}
			closeConnection(connection);
		} catch (SocketTimeoutException e) {
			throw new CustomUserNotificationExcepetion("growl.error", "growl.error.priter.timeout");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean printTestPage() {

		Optional<PrintDocument> doc = PrintDocumentRepository
				.findByID(pathoConfig.getDefaultDocuments().getSlideLableTestDocument());

		if (!doc.isPresent()) {
			logger.error("New Task: No TemplateUtil for printing label found");
			MessageHandler.sendGrowlErrorAsResource("growl.error.critical", "growl.print.slide.noTemplate");
			return false;
		}

		doc.get().initilize(new HashMap<String, Object>());

		print(doc.get().getFinalContent());

		return true;
	}

	/**
	 * Sends a file to the zpl printer
	 * 
	 * @param content
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public boolean print(FTPClient connection, String content, String file) throws IOException {
		InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

		if (connection.storeFile(file, stream)) {
			logger.debug("Upload " + file + ", ok");
			return true;
		} else {
			logger.debug("Upload " + file + ", failed");
			return false;
		}
	}

	/**
	 * Opens a ftp connection to the zpl printer
	 * 
	 * @throws SocketException
	 * @throws IOException
	 */
	public FTPClient openConnection() throws SocketException, IOException {
		FTPClient connection = new FTPClient();

		logger.debug("Connecting to label printer ftp://" + address + ":" + port);

		connection.setConnectTimeout(getTimeout());
		connection.connect(address, Integer.valueOf(getPort()));
		connection.login(userName, password);
		connection.setFileType(FTP.ASCII_FILE_TYPE);

		return connection;
	}

	/**
	 * closes the conncteion with the zpl printer
	 * 
	 * @throws IOException
	 */
	public void closeConnection(FTPClient connection) throws IOException {
		connection.logout();
		connection.disconnect();
	}

	public static String generateUnqiueName(int length) {
		return RandomStringUtils.randomAlphanumeric(length) + ".zpl";
	}
}
