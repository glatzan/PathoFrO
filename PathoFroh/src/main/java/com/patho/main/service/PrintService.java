package com.patho.main.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.user.HistoUser;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.json.JsonHandler;
import com.patho.main.util.printer.ClinicPrinter;
import com.patho.main.util.printer.ClinicPrinterDummy;
import com.patho.main.util.printer.LabelPrinter;
import com.patho.main.util.printer.RoomContainer;

import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
@Getter
@Setter
@ConfigurationProperties(prefix = "patho.printer")
public class PrintService extends AbstractService {

	private CupsPrinterHandler cupsPrinter;

	private LablePrinterHandler lablePrinter;

	@PostConstruct
	public void initializePrinters() {
		getCupsPrinter().initialize();
		getLablePrinter().initialize();
	}

	public ClinicPrinter getCurrentPrinter(HistoUser user) {
		return cupsPrinter.findPrinterForUser(user);
	}

	/**
	 * returns the current label priter
	 * 
	 * @return
	 */
	public LabelPrinter getCurrentLabelPrinter(HistoUser user) {
		return lablePrinter.findPrinterForUser(user);
	}

	@Getter
	@Setter
	public static class CupsPrinterHandler {

		private final Logger logger = LoggerFactory.getLogger(this.getClass());

		private String host;

		private int port;

		private String testPage;

		private String printerForRoom;

		/**
		 * List of clinicla pritners
		 */
		private List<ClinicPrinter> printer;

		/**
		 * Transformer for printerList
		 */
		private DefaultTransformer<ClinicPrinter> printerTransformer;

		public void initialize() {
			setPrinter(loadCupsPrinters(host, port));
			setPrinterTransformer(new DefaultTransformer<ClinicPrinter>(getPrinter()));
		}

		private List<ClinicPrinter> loadCupsPrinters(String host, int port) {
			ArrayList<ClinicPrinter> result = new ArrayList<ClinicPrinter>();
			CupsClient cupsClient;

			try {
				cupsClient = new CupsClient(host, port);
				List<CupsPrinter> cupsPrinter = cupsClient.getPrinters();
				// transformin into clinicprinters
				for (CupsPrinter p : cupsPrinter) {
					result.add(new ClinicPrinter(p));
				}

			} catch (Exception e) {
				logger.error("Retriving printers failed" + e);
				e.printStackTrace();
			}

			result.add(0, new ClinicPrinterDummy());

			return result;
		}

		/**
		 * Returns a printer for the given user
		 * 
		 * @param user
		 * @return
		 */
		public ClinicPrinter findPrinterForUser(HistoUser user) {
			if(user == null)
				return getPrinter().get(0);
			
			if (user.getSettings().getAutoSelectedPreferredPrinter()) {
				ClinicPrinter printer = getCupsPrinterForRoom();
				if (printer != null) {
					logger.debug("Pritner found, setting auto printer to " + printer.getName());
					return printer;
				} else
					logger.debug("No Pritner found!, selecting default printer");

			}

			if (user.getSettings().getPreferredPrinter() == 0) {
				// dummy printer is always there
				return getPrinter().get(0);
			} else {
				// updating the current printer, if no printer was selected the dummy printer
				// will be returned
				return findPrinterByID(user.getSettings().getPreferredPrinter());
			}
		}

		/**
		 * Searches the loaded clinical printers for the given printer. If found the
		 * printer from the global list will be returned. This will prevent problems if
		 * something has changed.
		 * 
		 * If no printer was found the first printer will be returned (DummyPrinter)
		 * 
		 * @return
		 */
		public ClinicPrinter findPrinterByID(long id) {
			for (ClinicPrinter printer : getPrinter()) {
				if (printer.getId() == id)
					return printer;
			}

			logger.debug("Returning dummy printer");
			return getPrinter().get(0);
		}

		/**
		 * Loads a printer from the room in association with the current ip
		 * 
		 * @return
		 */
		public ClinicPrinter getCupsPrinterForRoom() {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
					.getRequest();
			String ip = getRemoteAddress(request);

			if (ip != null) {
				String printerToRoomJson = (new JsonHandler()).requestJsonData(printerForRoom.replace("$ip", ip));

				List<RoomContainer> container = RoomContainer.factory(printerToRoomJson);

				if (container.size() > 0) {
					RoomContainer firstConatiner = container.get(0);

					for (ClinicPrinter printer : getPrinter()) {
						if (HistoUtil.isNotNullOrEmpty(firstConatiner.getPrinter())
								&& firstConatiner.getPrinter().equals(printer.getDeviceUri())) {
							logger.debug("Printer found for room " + ip + "; printer = " + printer.getName());
							return printer;
						}
					}
				}

			}

			return null;

		}

		/**
		 * Gets the remote address from a HttpServletRequest object. It prefers the
		 * `X-Forwarded-For` header, as this is the recommended way to do it (user may
		 * be behind one or more proxies).
		 *
		 * Taken from https://stackoverflow.com/a/38468051/778272
		 *
		 * @param request - the request object where to get the remote address from
		 * @return a string corresponding to the IP address of the remote machine
		 */
		public static String getRemoteAddress(HttpServletRequest request) {
			String ipAddress = request.getHeader("X-FORWARDED-FOR");
			if (ipAddress != null) {
				// cares only about the first IP if there is a list
				ipAddress = ipAddress.replaceFirst(",.*", "");
			} else {
				ipAddress = request.getRemoteAddr();
			}
			return ipAddress;
		}

	}

	@Getter
	@Setter
	public static class LablePrinterHandler {

		private final Logger logger = LoggerFactory.getLogger(this.getClass());

		private String testPage;

		/**
		 * Transformer for labelprinters
		 */
		private List<LabelPrinter> printer;

		/**
		 * Transformer for labelprinters
		 */
		private DefaultTransformer<LabelPrinter> printerTransformer;

		public void initialize() {
			setPrinterTransformer(new DefaultTransformer<LabelPrinter>(getPrinter()));
		}
		
		public LabelPrinter findPrinterByID(String id) {

			for (LabelPrinter labelPrinter : getPrinter()) {
				if (labelPrinter.getId() == Long.valueOf(id)) {
					return labelPrinter;
				}
			}
			return null;
		}

		/**
		 * Returns a printer for the given user
		 * 
		 * @param user
		 * @return
		 */
		public LabelPrinter findPrinterForUser(HistoUser user) {
			if(user == null) {
				return printer.get(0);
			}
			
			// TODO auto select for roon
			if (user.getSettings().getPreferredLabelPrinter() == null) {
				return printer.get(0);
			} else {
				LabelPrinter labelPrinter = findPrinterByID(user.getSettings().getPreferredLabelPrinter());

				if (labelPrinter != null) {
					logger.debug("Settings printer " + labelPrinter.getName() + " as selected printer");
					return labelPrinter;
				} else {
					// TODO serach for pritner in the same room
					return printer.get(0);
				}
			}
		}
	}

}