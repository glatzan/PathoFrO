package com.patho.main.adaptors;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

import com.patho.main.action.handler.GlobalSettings;
import com.patho.main.model.PDFContainer;
import com.patho.main.util.helper.FileUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
@Slf4j
public class FaxHandler {

	@Autowired
	@Lazy
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GlobalSettings globalSettings;

	private FaxSettings settings;

	public void sendFax(String faxNumber, PDFContainer container) {
		sendFax(Arrays.asList(new String[] { faxNumber }), container);
	}

	/**
	 * Sends a fax to the given nubers
	 * 
	 * @param faxNumbers
	 * @param container
	 */
	public void sendFax(List<String> faxNumbers, PDFContainer container) {
		String faxCommand = getSettings().getCommand();

		if (faxCommand != null && faxNumbers.size() > 0) {

			File workingDirectory = new File(
					FileUtil.getAbsolutePath(globalSettings.getProgramSettings().getWorkingDirectory()));

			File tempFile = new File(workingDirectory.getAbsolutePath() + File.separator
					+ RandomStringUtils.randomAlphanumeric(10) + ".pdf");

			FileUtil.saveContentOfFile(tempFile, container.getData());

			for (String faxNumber : faxNumbers) {
				String tmp = faxCommand.replace("$faxNumber", faxNumber).replace("$file",
						tempFile.getPath().replaceAll("\\\\", "/"));

				String[] tmpArr = tmp.split(settings.getCommandSplitter());

				log.debug("Using faxCommand: " + tmp);

				try {

					ProcessBuilder builder = new ProcessBuilder(tmpArr);
					// builder.redirectErrorStream(true);
					Process p = builder.start();
					// BufferedReader r = new BufferedReader(new
					// InputStreamReader(p.getInputStream()));
					// String line;
					// while (true) {
					// line = r.readLine();
					// if (line == null) {
					// break;
					// }
					// }

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Getter
	@Setter
	public class FaxSettings {
		private String windows_command;
		private String linux_command;
		private String commandSplitter;

		public String getCommand() {
			if (SystemUtils.IS_OS_WINDOWS) {
				log.debug("Sending fax, windows detected!");
				return getWindows_command();
			} else if (SystemUtils.IS_OS_LINUX) {
				log.debug("Sending fax, windows detected!");
				return getLinux_command();
			} else {
				log.debug("Sending fax, not supported os detected!");
				return null;
			}
		}
	}
}
