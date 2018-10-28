package com.patho.main.util;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.common.ContactRole;
import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.Eye;
import com.patho.main.common.TaskPriority;
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
// ok Operateur/Einsender 10
// ok	Rechts linkes Auge 11
// ok Anamnese 12
// ok Maligner Tumor 13
// ok Diagnose 14
// ok Datum 15
// ok festNachbefund 16
//	Konsil 17
// ok Histologischer Befund 18
// ok Assistenz 19
// ok OA-Liste 20
// ok informiert 21
//	Fax an HA (externenr einsen( 22
// ok nachbefund angeschaut
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
	private final int index_diagnosisRevision = 23;

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

	private HashMap<String, String> redirect = new HashMap<String, String>();
	{
		redirect.put("Neß", "Ness");
		redirect.put("MVH", "Mittelviefhaus");
		redirect.put("A.H.", "Auw-Hädrich");
		redirect.put("A.H.", "Auw-Hädrich");
		redirect.put("Bü.", "Bühler");
		redirect.put("Bie.", "Biermann");

	}

	public Physician findPhysician(String name) {

		name = redirect.get(name) == null ? name : redirect.get(name);

		String[] spl = name.split("[. ]");

		if (spl.length > 0) {
			String name1 = redirect.get(spl[spl.length - 1]) == null ? spl[spl.length - 1]
					: redirect.get(spl[spl.length - 1]);

			List<Physician> physician = physicianRepository.findAllByPersonLastName(name1);

			return physician.size() > 0 ? physician.get(0) : null;

		}

		return null;
	}

	public Date getDate(String str) {
		if (str == null)
			return null;

		str = str.replaceAll("[\\.]{1,}", ".");
		
		Pattern pattern = Pattern.compile("([0-9]{2}.[0-9]{2}.[0-9]{2})");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {

			SimpleDateFormat datep = new SimpleDateFormat("dd.MM.yy");
			try {
				return datep.parse(matcher.group(1));
			} catch (ParseException e) {
				return null;
			}
		}

		pattern = Pattern.compile("([0-9]{2}.[0-9]{2}.[0-9]{4})");
		matcher = pattern.matcher(str);
		if (matcher.find()) {

			SimpleDateFormat datep = new SimpleDateFormat("dd.MM.yyyy");
			try {
				return datep.parse(matcher.group(1));
			} catch (ParseException e) {
				return null;
			}
		}

		return null;

	}

	public void importFilemaker(String number) throws IOException {

		CsvParserSettings settings = new CsvParserSettings(); // many options here, check the documentation
		CsvParser parser = new CsvParser(settings);
		parser.beginParsing(new InputStreamReader(new FileInputStream("d:/import.csv"), "Cp1252"));

		FileWriter fw = new FileWriter("d:/importLog.log", true);
		BufferedWriter bw = new BufferedWriter(fw);

		ArrayList<Object[]> notProcessed = new ArrayList<>();

		StringBuilder inserLog = new StringBuilder();

		int rowCount = 0;

		HashSet<String> wuu = new HashSet<>();

		String[] row;
		while ((row = parser.parseNext()) != null) {
			rowCount++;

			if (number == null && rowCount % 100 != 0)
				continue;

			if (number == null) {
				inserLog.append("-------------------------------\r\n");
				inserLog.append(Arrays.toString(row) + "\r\n\\r\\n");
			}

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

					if (!taskid.matches(number)) {
						continue;
					}

					inserLog.append("-------------------------------\r\n");
					inserLog.append(Arrays.toString(row) + "\r\n\\r\\n");

					inserLog.append("TaskID: " + taskid + "\r\n");

					logger.debug("Creating");
					logger.debug("-> " + Arrays.toString(row));

					boolean malign = row[index_malign] != null ? row[index_malign].equals("X") : false;

					String taskPrioArr[] = row[index_dueDate] != null
							? row[index_dueDate].split(Character.toString((char) 11))
							: new String[] { "" };

					boolean taskPrio = false;
					Date taskPrioDate = new Date();

					Date sigantureDate = getDate(row[index_notified]);

					Date edate = getDate(row[index_edate]);

					if (edate == null) {
						inserLog.append("Leeres EDatum");
						logger.debug("Leeres EDatum");
						throw new IllegalIdentifierException("Empty");
					}

					if (taskPrioArr.length == 2) {
						taskPrioDate = getDate(taskPrioArr[0]);
						taskPrio = taskPrioArr[1].equals("X");
					}

					String oa = row[index_oa];
					String assi = row[index_assi];
					String surgeon = row[index_surgeon];
					String diagnosisRevision1 = row[index_diagnosisRevision];

					Physician oaPhys = null;
					Physician assiPhys = null;
					Physician surgeonPhys = null;
					Physician diagnosisRevisionPhys = null;

					if (HistoUtil.isNotNullOrEmpty(oa)) {
						Physician py = findPhysician(oa.trim());

						if (py == null) {

							inserLog.append("OA not found: " + oa);
							throw new IllegalIdentifierException("Empty");
						}

						oaPhys = py;
					}

					if (HistoUtil.isNotNullOrEmpty(assi)) {
						Physician py = findPhysician(assi.trim());

						if (py == null) {

							inserLog.append("Assi not found" + assi);
							throw new IllegalIdentifierException("Empty");
						}

						assiPhys = py;
					}

					if (HistoUtil.isNotNullOrEmpty(surgeon)) {
						Physician py = findPhysician(surgeon.trim());

						if (py == null) {

							inserLog.append("Assi not found" + surgeon);
							throw new IllegalIdentifierException("Empty");
						}

						surgeonPhys = py;
					}

					if (HistoUtil.isNotNullOrEmpty(diagnosisRevision1)) {
						Physician py = findPhysician(diagnosisRevision1.trim());

						if (py == null) {

							inserLog.append("Revision phys not found" + diagnosisRevision1);
							throw new IllegalIdentifierException("Empty");
						}

						diagnosisRevisionPhys = py;
					}

					logger.debug("Creating patient {}", strPit);
					p = patientService.findPatientByPizInDatabaseAndPDV(strPit, false);

					if (!p.isPresent()) {
						inserLog.append("Patient not found abort");
						throw new IllegalIdentifierException("Empty");
					}

					if (sigantureDate == null) {
						inserLog.append("Signature Date empty");
						throw new IllegalIdentifierException("Signature Date empty");
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
							task.setDueDate(taskPrioDate != null ? taskPrioDate.getTime() : edate.getTime());

						task = taskRepository.save(task);

						String[] diagnosis = row[index_diagnosis] != null
								? row[index_diagnosis].split(Character.toString((char) 29))
								: new String[] { "" };

						String[] materialArr = row[index_material] != null
								? row[index_material].split(Character.toString((char) 29))
								: new String[] { "" };

						for (int i = 0; i < materialArr.length; i++) {
							List<MaterialPreset> preset = materialPresetRepository.findAllByName(materialArr[i], true);

							task = sampleService.createSample(task, preset.size() > 0 ? preset.get(0) : null,
									materialArr[i], true, true, true, true);
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
						rev.setCompletionDate(sigantureDate.getTime());

						task.setFinalized(true);
						task.setFinalizationDate(sigantureDate.getTime());

						task = taskRepository.save(task);

						if (diagnosisRevisionPhys != null) {
							task = diagnosisService.createDiagnosisRevision(task,
									DiagnosisRevisionType.DIAGNOSIS_REVISION);
							Iterator<DiagnosisRevision> iter = task.getDiagnosisRevisions().iterator();
							iter.next();
							DiagnosisRevision re = iter.next();

							Date revdate = getDate(row[index_date]);

							re.setSignatureTwo(new Signature(diagnosisRevisionPhys));

							if (revdate != null) {
								re.setCreationDate(revdate.getTime());
								re.setText(row[index_rediagnosis]);
								re.setSignatureDate(revdate.getTime());
							}

							re.setCompletionDate(sigantureDate.getTime());
							re.setNotificationDate(sigantureDate.getTime());
						}

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

		for (String string : wuu) {
			System.out.println(string);
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