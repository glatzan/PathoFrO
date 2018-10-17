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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.commons.collections.map.HashedMap;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
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
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.Physician;
import com.patho.main.model.Signature;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.MaterialPresetRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.service.BioBankService;
import com.patho.main.service.DiagnosisService;
import com.patho.main.service.FileService;
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

// ok	# 0
//	- Name 1
//	- Strasse 2
//	- PLZ, Ort  3
// ok EDatum 4
// ok	Station 5
// ok Terminpräperat 6
//  - GDatum 7
// ok 	Piz 8
//	Material 9 
//	Operateur/Einsender 10
// ok	Rechts linkes Auge 11
// ok Anamnese 12
// ok Maligner Tumor 13
// ok Diagnose 14
// ok Datum 15
//	festNachbefund 16
//	Konsil 17
// ok Histologischer Befund 18
//	Assistenz 19
//	OA-Liste 20
//	informiert 21
//	Fax an HA (externenr einsen( 22
//
//	-> gs trennt diangosen
//	

	private final int index_taskid = 0;
	private final int index_name = 1;
	private final int index_plz = 2;
	private final int index_edate = 4;
	private final int index_ward = 5;
	private final int index_dueDate = 6;
	private final int index_gdate = 7;
	private final int index_piz = 8;
	private final int index_material = 9;
	private final int index_surgeon = 10;
	private final int index_eye = 11;
	private final int index_history = 12;
	private final int index_malign = 13;
	private final int index_diagnosis = 14;
	private final int index_date = 15;
	private final int index_rediagnosis = 16;
	private final int index_council = 17;
	private final int index_long_diagnosis = 18;
	private final int index_assi = 19;
	private final int index_oa = 20;
	private final int index_notified = 21;
	private final int index_external = 22;

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

	@Autowired
	private FileService fileService;

	@Autowired
	private MaterialPresetRepository materialPresetRepository;

	@Autowired
	private PhysicianRepository physicianRepository;

	private HashMap<String, Long> phys = new HashMap();

	{
		phys.put("Prof. Dr. C. Auw-Hädrich", (long) 6);
		phys.put("Auw-Hädrich", (long) 6);
		phys.put("Prof. Auw-Hädrich", (long) 6);

		phys.put("Frau PD Dr. L. Gasser", (long) 10);
		phys.put("PD Dr. Laura Gasser", (long) 10);
		phys.put("PD Dr. Gasser", (long) 10);
		phys.put("Dr. Laura Gasser", (long) 10);
		phys.put("Frau Dr. L. Gasser", (long) 10);
		phys.put("Dr. Gasser", (long) 10);
		phys.put("Gasser", (long) 10);

		phys.put("Herr Dr. S. Lang", (long) 12);
		phys.put("Dr. Lang", (long) 12);
		phys.put("Dr. S.Lang", (long) 12);
		phys.put("Lang", (long) 12);

		phys.put("Dr. Kristina Schölles", (long) 32);
		phys.put("Schölles", (long) 32);
		
		phys.put("Dr. Lisa Atzrodt", (long) 138);
		phys.put("Dr. Atzrodt", (long) 138);
		phys.put("Atzrodt", (long) 138);
		phys.put("Dr. Zimmermann", (long) 138);
		phys.put("Dr. Lisa Zimmermann", (long) 138);
		phys.put("Zimmermann", (long) 138);

		phys.put("Dr. Evers", (long) 25);
		phys.put("Evers", (long) 25);
		
		phys.put("Susanne Ißleib", (long) 28);
		phys.put("Ißleib", (long) 28);
		
		phys.put("Dr. Jan Lübke", (long) 13);
		phys.put("Dr. Lübke", (long) 13);
		phys.put("Lübke", (long) 13);

		phys.put("Dr. Clemens Lange", (long) 31);
		phys.put("Lange", (long) 31);
		
		phys.put("Dr. Benjamin Thabo Lapp", (long) 14);
		phys.put("Dr. Lapp", (long) 14);
		phys.put("Lapp", (long) 14);
		
		phys.put("Dr. Philip Maier", (long) 9);
		phys.put("PD Dr. Maier", (long) 9);
		phys.put("Maier", (long) 9);

		phys.put("Prof. Reinhard", (long) 4);
		phys.put("Reinhard", (long) 4);
		
		phys.put("Prof.  Mittelviefhaus", (long) 339);
		phys.put("Mittelviefhaus", (long) 339);
		phys.put("MVH", (long) 339);
		
		phys.put("Prof. Lagreze", (long) 8);
		phys.put("Lagreze", (long) 8);
		
		phys.put("Prof. Agostini", (long) 7);
		phys.put("Agostini", (long) 7);
		
		phys.put("PD Dr. Eberwein", (long) 11);
		phys.put("Eberwein", (long) 11);
		
		phys.put("Dr. Bleul", (long) 17);
		phys.put("Bleul", (long) 17);
		
		phys.put("Dr. Ludwig", (long) 18);
		phys.put("Ludwig", (long) 18);
		
		phys.put("Dr. Stech", (long) 19);
		phys.put("Stech", (long) 19);
		
		phys.put("Dr. Bründer", (long) 20);
		phys.put("Bründer", (long) 20);
		
		phys.put("Dr. Anton", (long) 21);
		phys.put("Anton", (long) 21);
		
		phys.put("Dr. Gross", (long) 22);
		phys.put("Gross", (long) 22);
		
		phys.put("Dr. Grundel", (long) 23);
		phys.put("Grundel", (long) 23);

		phys.put("M. Avar", (long) 489);
		phys.put("Dr. Avar", (long) 489);
		phys.put("Avar", (long) 489);

		phys.put("Prof. Stahl", (long) 24);
		phys.put("Stahl", (long) 24);

		phys.put("Dr. Joachimsen", (long) 27);
		phys.put("Joachimsen", (long) 27);
		
		phys.put("Dr. Reichel", (long) 29);
		phys.put("Reichel", (long) 29);
		
		phys.put("Osteried", (long) 287);

		phys.put("M.Reich", (long) 438);
		phys.put("Reich", (long) 438);
		
		phys.put("Dr. Lange", (long) 31);
		phys.put("Lange", (long) 31);
		
		phys.put("PD Dr.Ness", (long) 28);
		phys.put("Ness", (long) 28);
		
		phys.put("Dr. Schölles", (long) 32);
		phys.put("Schölles", (long) 32);
		
		phys.put("Dr. Schmid", (long) 441);
		phys.put("Schmid", (long) 441);
		
		phys.put("Dr. Jehle", (long) 16);
		phys.put("Jehle", (long) 16);
		
		phys.put("Dr. Daniel", (long) 387);
		phys.put("Daniel", (long) 387);
		
		phys.put("Dr. Grewing", (long) 237);
		phys.put("Grewing", (long) 237);
		
		phys.put("Cazana", (long) 139);

		// phys.put("Dr. Paduraru", (long) 4);
		// phys.put("Dr. S. Reichl", (long) 4);

//		Augenklinik Göttingen
//		Ammerland-Klinik Westerstede
//		Prof. Grüb, Breisach 
//		PG Kloevekorn Halle
	}

	public void importFilemaker() throws IOException {

		CsvParserSettings settings = new CsvParserSettings(); // many options here, check the documentation
		CsvParser parser = new CsvParser(settings);
		parser.beginParsing(new InputStreamReader(new FileInputStream("d:/import.csv"), "Cp1252"));

		FileWriter fw = new FileWriter("d:/importLog.log", true);
		BufferedWriter bw = new BufferedWriter(fw);
		SimpleDateFormat datep = new SimpleDateFormat("dd.MM.yy");

		ArrayList<Object[]> notProcessed = new ArrayList<>();

		StringBuilder inserLog = new StringBuilder();

		int rowCount = 0;

		String[] row;
		while ((row = parser.parseNext()) != null) {
			rowCount++;

			if (rowCount % 100 != 0)
				continue;

			inserLog.append("-------------------------------\r\n");
			inserLog.append(Arrays.toString(row) + "\r\n\\r\\n");

			logger.debug("-> " + Arrays.toString(row));
			if (HistoUtil.isNotNullOrEmpty(row[index_piz])) {
				Optional<Patient> p;
				try {

					// piz
					String strPit = row[index_piz].replaceAll("[^\\d.]", "");
					// taskid
					String taskid = row[index_taskid] != null ? row[index_taskid].replaceAll("[^\\d.]", "") : null;

					if (taskid == null) {
						inserLog.append("Empty task id abort");
						continue;
					}

					inserLog.append("TaskID: " + taskid + "\r\n");

//					if (!taskid.equals("170737")) {
//						continue;
//					}

					logger.debug("Creating");

					String material = row[index_material];

					boolean malign = row[index_malign] != null ? row[index_malign].equals("X") : false;

					String taskPrioArr[] = row[index_dueDate] != null
							? row[index_dueDate].split(Character.toString((char) 11))
							: new String[] { "" };

					boolean taskPrio = false;
					Date taskPrioDate = new Date();

					Date sigantureDate = row[index_notified] != null ? datep.parse(row[index_notified]) : null;

					Date edate = row[index_edate] != null ? datep.parse(row[index_edate]) : null;

					if (edate == null) {
						inserLog.append("Leeres EDatum");
						logger.debug("Leeres EDatum");
						throw new IllegalIdentifierException("Empty");
					}

					if (taskPrioArr.length == 2) {
						taskPrioDate = datep.parse(taskPrioArr[0]);
						taskPrio = taskPrioArr[1].equals("X");
					}

					String oa = row[index_oa];
					String assi = row[index_assi];
					String surgeon = row[index_surgeon];

					Physician oaPhys = null;
					Physician assiPhys = null;
					Physician surgeonPhys = null;

					if (HistoUtil.isNotNullOrEmpty(oa)) {
						Long oaId = phys.get(oa);

						if (oaId == null) {

							String[] spl = oa.split("[. ]");

							if (spl.length > 0)
								oaId = phys.get(spl[spl.length - 1]);
						}
						
						if (oaId == null) {

							inserLog.append("OA not found: " + oa);
							throw new IllegalIdentifierException("Empty");
						}

						oaPhys = physicianRepository.findById(oaId).get();
					}

					if (HistoUtil.isNotNullOrEmpty(assi)) {
						Long assiId = phys.get(assi);
						
						if (assiId == null) {

							String[] spl = assi.split("[. ]");

							if (spl.length > 0)
								assiId = phys.get(spl[spl.length - 1]);
						}
						
						
						if (assiId == null) {
							inserLog.append("Assi not found " + assi);
							throw new IllegalIdentifierException("Empty");
						}

						assiPhys = physicianRepository.findById(assiId).get();
					}

					if (HistoUtil.isNotNullOrEmpty(surgeon)) {
						Long surgeonId = phys.get(surgeon);
						
						if (surgeonId == null) {

							String[] spl = surgeon.split("[. ]");

							if (spl.length > 0)
								surgeonId = phys.get(spl[spl.length - 1]);
						}
						
						if (surgeonId == null) {
							inserLog.append("Assi not found " + surgeon);
							throw new IllegalIdentifierException("Empty");
						}

						surgeonPhys = physicianRepository.findById(surgeonId).get();
					}

					logger.debug("Creating patient {}", strPit);
					p = patientService.findPatientByPizInDatabaseAndPDV(strPit, false);

					if (!p.isPresent()) {
						inserLog.append("Patient not found abort");
						throw new IllegalIdentifierException("Empty");
					}

					if (sigantureDate == null) {
						inserLog.append("Signature Date empty");
						throw new IllegalIdentifierException("Empty");
					}

					Patient pait = patientService.addPatient(p.get(), false);
					inserLog.append("Patient found: " + pait.getPerson().getFullName());

					if (taskService.isTaskIDAvailable(taskid)) {
						Task task = taskService.createTask(pait, taskid, true);

						// eye
						Eye eyes;
						if (HistoUtil.isNotNullOrEmpty(row[index_eye])) {
							if (row[index_eye].equals("RA"))
								eyes = Eye.RIGHT;
							else if (row[index_eye].equals("LA"))
								eyes = Eye.LEFT;
							else
								eyes = Eye.UNKNOWN;
						} else
							eyes = Eye.UNKNOWN;
						task.setEye(eyes);

						// ward
						task.setWard(row[index_ward]);

						// edate
						task.setDateOfReceipt(edate.getTime());
						task.setDateOfSugery(edate.getTime());
						task.setCaseHistory(HistoUtil.isNotNullOrEmpty(row[index_history])
								? row[index_history].replaceAll(Character.toString((char) 11), "\r\n")
								: "");
						task.setInsurance("");
						task.getAudit().setCreatedOn(edate.getTime());
						task.setTaskPriority(taskPrio ? TaskPriority.TIME : TaskPriority.NONE);
						if (taskPrio)
							task.setDueDate(taskPrioDate.getTime());

						task = taskRepository.save(task);

						String[] diagnosis = row[index_diagnosis] != null
								? row[index_diagnosis].split(Character.toString((char) 29))
								: new String[] { "" };

						String[] materialArr = row[index_material] != null
								? row[index_material].split(Character.toString((char) 29))
								: new String[] { "" };

						for (int i = 0; i < materialArr.length; i++) {
							task = sampleService.createSample(task, null, materialArr[i], true, true, true);
						}

						// creating standard diagnoses
						task = diagnosisService.createDiagnosisRevision(task, DiagnosisRevisionType.DIAGNOSIS);

						DiagnosisRevision rev = task.getDiagnosisRevisions().iterator().next();

						int i = 0;
						for (Diagnosis diagnosisD : rev.getDiagnoses()) {
							diagnosisD.setDiagnosis(diagnosis[i]);
							diagnosisD.setMalign(malign);
							i++;
						}

						rev.setSignatureDate(sigantureDate.getTime());
						rev.setText(row[index_long_diagnosis] != null
								? row[index_long_diagnosis].replaceAll(Character.toString((char) 11), "\r\n")
								: "");
						rev.setCreationDate(edate.getTime());
						rev.setNotificationDate(sigantureDate.getTime());

						rev.setSignatureOne(new Signature(assiPhys));
						rev.setSignatureTwo(new Signature(oaPhys));

						task.setFinalized(true);
						task.setFinalizationDate(sigantureDate.getTime());

						task = taskRepository.save(task);

						task = associatedContactService
								.addAssociatedContact(task, surgeonPhys.getPerson(), ContactRole.SURGEON).getTask();
//
//									logger.debug("Searching for material... {}", material);
//									List<MaterialPreset> m = materialPresetRepository.findAllByName(material, false);
//									for (MaterialPreset materialPreset : m) {
//										logger.debug("Found material {}", materialPreset.getName());
//									}

						task = associatedContactService
								.addAssociatedContact(task, pait.getPerson(), ContactRole.PATIENT).getTask();

						BioBank bioBank = bioBankService.createBioBank(task);
					}

				} catch (Exception e) {
					e.printStackTrace();

					logger.debug("Error for patient {}", row[index_piz]);
					notProcessed.add(row);

					bw.write(inserLog.toString());
					bw.newLine();
				}

				inserLog.setLength(0);
			}

		}

		bw.close();

		CsvWriter writer = new CsvWriter(new BufferedWriter(new FileWriter("d:/output.csv")), new CsvWriterSettings());
		// Write the record headers of this file
		writer.writeHeaders("rsult");
		writer.writeRowsAndClose(notProcessed);
	}
}

//180171,"LA","Descemet bei Fuchs DMEK","","Descemet","Dr. Lapp","26837251","","31.01.18","","stationär","29.06.42"	
//180172,"LA","Nachresektion z. Vergrößerung Sicherheitsabstand","","Nachresektat","Prof. Auw-Hädrich","35771336","","31.01.18","","stationär-privat","03.04.55"
