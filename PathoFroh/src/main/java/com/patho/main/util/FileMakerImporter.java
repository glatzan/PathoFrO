package com.patho.main.util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.primefaces.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.task.CreateTaskDialog.TaskTempData.SampleTempData;
import com.patho.main.common.ContactRole;
import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.Eye;
import com.patho.main.common.TaskPriority;
import com.patho.main.config.excepion.ToManyEntriesException;
import com.patho.main.model.BioBank;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.service.BioBankService;
import com.patho.main.service.DiagnosisService;
import com.patho.main.service.PatientService;
import com.patho.main.service.SampleService;
import com.patho.main.service.TaskService;
import com.patho.main.util.exception.CustomNullPatientExcepetion;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.HistoUtil;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class FileMakerImporter {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final int piz = 6;
	private final int taskid = 0;
	private final int eye = 1;
	private final int ward = 10;
	private final int edate = 8;
	private final int material = 4;
	private final int caseHistory = 2;

	@Autowired
	private TaskService taskService;

	@Autowired
	private PatientService patientService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private SampleService sampleService;

	@Autowired
	private DiagnosisService diagnosisService;

	@Autowired
	private AssociatedContactService associatedContactService;

	@Autowired
	private BioBankService bioBankService;

	public void importFilemaker() throws IOException {

		CsvParserSettings settings = new CsvParserSettings(); // many options here, check the documentation
		CsvParser parser = new CsvParser(settings);
		parser.beginParsing(new InputStreamReader(new FileInputStream("d:/import.csv")));

		ArrayList<Object[]> notProcessed = new ArrayList<>();

		String[] row;
		while ((row = parser.parseNext()) != null) {

			if (HistoUtil.isNotNullOrEmpty(row[piz])) {

				Optional<Patient> p;
				try {

					String strPit = row[piz].replaceAll("[^\\d.]", "");

					logger.debug("Creating patient {}", strPit);
					p = patientService.findPatientByPizInDatabaseAndPDV(strPit, false);
					if (p.isPresent()) {

						Patient pait = patientService.addPatient(p.get(), false);

						if (taskService.isTaskIDAvailable(row[taskid])) {
							Task task = taskService.createTask(pait, row[taskid], true);
							Eye eyes;

							if (HistoUtil.isNotNullOrEmpty(row[eye])) {
								if (row[eye].equals("RA"))
									eyes = Eye.RIGHT;
								else if (row[eye].equals("LA"))
									eyes = Eye.LEFT;
								else
									eyes = Eye.UNKNOWN;
							} else
								eyes = Eye.UNKNOWN;

							task.setEye(eyes);
							task.setWard(row[ward]);

							SimpleDateFormat datep = new SimpleDateFormat("dd.MM.yy");
							Date date = datep.parse(row[edate]);

							task.setDateOfReceipt(date.getTime());
							task.setCaseHistory(HistoUtil.isNotNullOrEmpty(row[caseHistory]) ? row[caseHistory] : "");
							task.setInsurance("");

							task.setTaskPriority(TaskPriority.NONE);

							task = taskRepository.save(task);

							sampleService.createSample(task, null, row[material], true, true, true);

							// creating standard diagnoses
							task = diagnosisService.createDiagnosisRevision(task, DiagnosisRevisionType.DIAGNOSIS);

							task = associatedContactService
									.addAssociatedContact(task, pait.getPerson(), ContactRole.PATIENT).getTask();

							BioBank bioBank = bioBankService.createBioBank(task);
						}
						continue;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			logger.debug("Error for patient {}", row[piz]);
			notProcessed.add(row);
		}

		CsvWriter writer = new CsvWriter(new BufferedWriter(new FileWriter("d:/output.csv")), new CsvWriterSettings());
		// Write the record headers of this file
		writer.writeHeaders("rsult");
		writer.writeRowsAndClose(notProcessed);
	}
}

//180171,"LA","Descemet bei Fuchs DMEK","","Descemet","Dr. Lapp","26837251","","31.01.18","","stationär","29.06.42"	
//180172,"LA","Nachresektion z. Vergrößerung Sicherheitsabstand","","Nachresektat","Prof. Auw-Hädrich","35771336","","31.01.18","","stationär-privat","03.04.55"
