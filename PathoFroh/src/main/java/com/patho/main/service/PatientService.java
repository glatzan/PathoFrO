package com.patho.main.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.primefaces.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.config.excepion.ToManyEntriesException;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.JSONPatientRepository;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.util.exception.CustomNullPatientExcepetion;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.HistoUtil;

@Service
@Transactional
public class PatientService extends AbstractService {

	@Autowired
	private JSONPatientRepository jsonPatientRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private TaskRepository taskRepository;

	/**
	 * Adds a patient to the database, if the patient is not new, the patient is
	 * saved. Compares and updates patient data with the clinic backed.
	 * 
	 * @param patient
	 * @param update
	 * @throws JSONException
	 */
	public Patient addPatient(Patient patient) throws JSONException {
		return addPatient(patient, true);
	}

	/**
	 * Adds a patient to the database, if the patient is not new, the patient is
	 * saved. Compares and updates patient data with the clinic backed. If update is
	 * true a data comparison with the pdv will be initialized.
	 * 
	 * @param patient
	 * @param update
	 * @throws JSONException
	 * @throws ToManyEntriesException
	 * @throws CustomNullPatientExcepetion
	 */
	public Patient addPatient(Patient patient, boolean update) throws JSONException {

		// add patient from the clinic-backend, get all data of this
		// patient, piz search is more specific
		if (HistoUtil.isNotNullOrEmpty(patient.getPiz()) && update) {
			logger.debug("Getting data from pdv for patient " + patient.getPiz());
			Optional<Patient> clincPatient = jsonPatientRepository.findByPIZ(patient.getPiz());

			if (clincPatient.isPresent())
				copyPatientData(clincPatient.get(), patient);
		}

		// patient not in database, is new patient from database
		if (patient.getId() == 0) {
			logger.debug("Adding patient (" + patient.getPiz() + ") to database");
			// set add date
			patient.setCreationDate(System.currentTimeMillis());
			patient.setInDatabase(true);
			// setting external patient if piz is null
			patient.setExternalPatient(HistoUtil.isNullOrEmpty(patient.getPiz()));
			patient = patientRepository.save(patient, resourceBundle
					.get(patient.isExternalPatient() ? "log.patient.extern.new" : "log.patient.search.new"));
		} else {
			logger.debug("Patient (" + patient.getPiz() + ") in database, updating and saving");
			patient = patientRepository.save(patient, resourceBundle.get("log.patient.search.update"));
		}

		return patient;
	}

	/**
	 * Tries to delete a patient, if that does not work, the patient is archvied.
	 * 
	 * @param patient
	 * @return
	 */
	@Transactional(propagation = Propagation.NEVER)
	public boolean deleteOrArchive(Patient patient) {
		try {
			patientRepository.delete(patient, resourceBundle.get("log.patient.remove", patient));
			return true;
		} catch (Exception e) {
			archive(patient, true);
			return false;
		}
	}

	/**
	 * Removes a patient without tasks from local database. TODO: Remove logs
	 * 
	 * @param patient
	 */
	public void removePatient(Patient patient) {
		if (patient.getTasks().isEmpty()) {
			patientRepository.delete(patient, resourceBundle.get("log.patient.remove", patient));
		}
	}

	/**
	 * Archives a patient without tasks from local database
	 * 
	 * @param patient
	 */
	public Patient archive(Patient patient, boolean archive) {
		if (archive && patient.getTasks().isEmpty()) {
			patient.setArchived(true);
			return patientRepository.save(patient, resourceBundle.get("log.patient.archived", patient));
		} else if (!archive && patient.isArchived()) {
			patient.setArchived(false);
			return patientRepository.save(patient, resourceBundle.get("log.patient.log.patient.dearchived", patient));
		}

		return patient;
	}

	/**
	 * Moves tasks within two patients.
	 * 
	 * @param source
	 * @param target
	 * @param newSourceTasks
	 * @param newTargetTasks
	 */
	public void moveTaskBetweenPatients(Patient source, Patient target, List<Task> newSourceTasks,
			List<Task> newTargetTasks) {

		StringBuffer moveLog = new StringBuffer();
		List<Task> saveList = new ArrayList<Task>();

		for (Task task : newSourceTasks) {
			// checking if task is already associated with source
			if (!source.getTasks().stream().anyMatch(p -> p.equals(task))) {
				task.setParent(source);
				moveLog.append(source.getPerson().getFullName() + " -> " + task.toString());
				saveList.add(task);
			}
		}

		for (Task task : newTargetTasks) {
			// checking if task is already associated with target
			if (!target.getTasks().stream().anyMatch(p -> p.equals(task))) {
				task.setParent(target);
				moveLog.append(target.getPerson().getFullName() + " -> " + task.toString());
				saveList.add(task);
			}
		}

		taskRepository.saveAll(saveList, resourceBundle.get("log.patient.merge", source.getPerson().getFullName(),
				target.getPerson().getFullName(), moveLog.toString()));

	}

	/**
	 * Merges two patients. Copies all tasks from one patient to the other.
	 * 
	 * @param from
	 * @param to
	 */
	public Patient mergePatient(Patient from, Patient to) {
		return mergePatient(from, to, null);
	}

	/**
	 * Merges two patients. Copies all tasks from one patient to the other. Tasks a
	 * lists of tasks to merge.
	 * 
	 * @param from
	 * @param to
	 * @param tasksToMerge
	 */
	public Patient mergePatient(Patient source, Patient target, Set<Task> tasksToMerge) {
		return mergePatient(source, target, tasksToMerge, false);
	}

	/**
	 * Merges two patients. Copies all tasks from one patient to the other. Tasks a
	 * lists of tasks to merge.
	 * 
	 * @param from
	 * @param to
	 * @param tasksToMerge
	 */
	public Patient mergePatient(Patient source, Patient target, Set<Task> tasksToMerge, boolean copyPatientData) {
		Set<Task> tasksFrom = tasksToMerge == null ? source.getTasks() : tasksToMerge;

		if (tasksFrom != null && !tasksFrom.isEmpty()) {

			for (Task task : tasksFrom) {
				task.setParent(target);
			}

			taskRepository.saveAll(tasksFrom,
					resourceBundle.get("log.patient.merge", source.getPerson().getFullName()));
			source.setTasks(new HashSet<Task>());
		}

		if (copyPatientData) {
			target = copyPatientDataAndSave(source, target);
		}

		return target;
	}

	/**
	 * Copies patient data from the source to the target. Saves the target.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public Patient copyPatientDataAndSave(Patient source, Patient target) {
		return copyPatientDataAndSave(source, target, false);
	}

	/**
	 * Copies patient data from the source to the target, if forceAutoUpdate is true
	 * even if data shouln'd been copied. Saves the target.
	 * 
	 * @param source
	 * @param target
	 * @param forceAutoUpdate
	 * @return
	 */
	public Patient copyPatientDataAndSave(Patient source, Patient target, boolean forceAutoUpdate) {
		if (copyPatientData(source, target, forceAutoUpdate))
			return patientRepository.save(target, resourceBundle.get("log.patient.copyData", target));
		return target;
	}

	public Optional<Patient> findPatientByPizInDatabaseAndPDV(String piz, boolean localDatabaseOnly)
			throws HistoDatabaseInconsistentVersionException, JSONException, ToManyEntriesException,
			CustomNullPatientExcepetion {
		return findPatientByPizInDatabaseAndPDV(piz, localDatabaseOnly, false, false);
	}

	/**
	 * Returns a Patient by the given piz. If localDatabaseOnly is true no pdv
	 * patient will be displayed. (Notice that data of local patient will be synced
	 * with pdv nevertheless.
	 * 
	 * @param piz
	 * @param localDatabaseOnly
	 * @return
	 * @throws HistoDatabaseInconsistentVersionException
	 * @throws CustomNullPatientExcepetion
	 * @throws ToManyEntriesException
	 * @throws JSONException
	 */
	public Optional<Patient> findPatientByPizInDatabaseAndPDV(String piz, boolean localDatabaseOnly,
			boolean initializeTasks, boolean initializeFiles) throws HistoDatabaseInconsistentVersionException,
			JSONException, ToManyEntriesException, CustomNullPatientExcepetion {
		// only search if 8 digit are provides
		if (piz != null && piz.matches("^[0-9]{8}$")) {
			Optional<Patient> patient = patientRepository.findOptionalByPiz(piz, initializeTasks, initializeFiles,
					true);

			// abort search, not found in local database
			if (!patient.isPresent() && localDatabaseOnly)
				return Optional.empty();

			Optional<Patient> pdvPatient = jsonPatientRepository.findByPIZ(piz);

			if (patient.isPresent()) {
				copyPatientDataAndSave(pdvPatient.get(), patient.get());
				return patient;
			} else if (pdvPatient.isPresent()) {
				logger.debug("Patient not in database, returning pdv data");
				return pdvPatient;
			}
		}
		return Optional.empty();
	}

	/**
	 * Searches for a range of not completed pizes 6 to 8 digits, searches only in
	 * histo database, pdv database does not support this. Updates found patients
	 * from pdv database.
	 * 
	 * @param piz
	 * @return
	 * @throws JSONException
	 * @throws ToManyEntriesException
	 * @throws CustomNullPatientExcepetion
	 * @throws HistoDatabaseInconsistentVersionException
	 */
	public List<Patient> findAllPatientsByPizInDatabaseAndPDV(String piz) throws JSONException, ToManyEntriesException,
			CustomNullPatientExcepetion, HistoDatabaseInconsistentVersionException {

		List<Patient> patients = patientRepository.findByPiz(piz);

		// updates all patients from the local database with data from the
		// clinic backend
		for (Patient patient : patients) {

			Optional<Patient> pdvPatient = jsonPatientRepository.findByPIZ(patient.getPiz());

			logger.debug("Patient (" + piz + ") found in database, updating with pdv data");

			// saves patient if data are changed by pdf data
			if (pdvPatient.isPresent())
				copyPatientDataAndSave(pdvPatient.get(), patient);

		}
		return patients;
	}

	/**
	 * Searches for patients in local and clinic database. Does not auto update all
	 * local patient, does save changes if both clinic patien and local patient was
	 * found
	 * 
	 * @param name
	 * @param surname
	 * @param birthday
	 * @param localDatabaseOnly
	 * @return
	 * @throws HistoDatabaseInconsistentVersionException
	 * @throws CustomNullPatientExcepetion
	 */
	public List<Patient> findAllPatientsByNameSurnameBirthdayInDatabaseAndPDV(String name, String surname,
			Date birthday, boolean localDatabaseOnly)
			throws HistoDatabaseInconsistentVersionException, CustomNullPatientExcepetion {
		return findAllPatientsByNameSurnameBirthdayInDatabaseAndPDV(name, surname, birthday, localDatabaseOnly,
				new AtomicBoolean(false));
	}

	/**
	 * Searches for patients in local and clinic database. Does not auto update all
	 * local patient, does save changes if both clinic patien and local patient was
	 * found
	 * 
	 * @param name
	 * @param surname
	 * @param birthday
	 * @param localDatabaseOnly
	 * @param toManyEntriesInClinicDatabase
	 * @return
	 * @throws CustomNullPatientExcepetion
	 * @throws HistoDatabaseInconsistentVersionException
	 */
	public List<Patient> findAllPatientsByNameSurnameBirthdayInDatabaseAndPDV(String name, String surname,
			Date birthday, boolean localDatabaseOnly, AtomicBoolean toManyEntriesInClinicDatabase)
			throws CustomNullPatientExcepetion, HistoDatabaseInconsistentVersionException {

		ArrayList<Patient> result = new ArrayList<Patient>();

		List<Patient> histoPatients = patientRepository.findByNameAndFirstnameAndBirthday(name, surname, birthday,
				false, false, true);

		List<Patient> clinicPatients = new ArrayList<Patient>();

		if (!localDatabaseOnly) {
			try {
				clinicPatients = jsonPatientRepository.findByNameAndSurNameAndBirthday(name, surname, birthday);
			} catch (ToManyEntriesException e) {
				toManyEntriesInClinicDatabase.set(true);
				clinicPatients = new ArrayList<Patient>();
			}
		}

		for (Patient hPatient : histoPatients) {
			result.add(hPatient);
			hPatient.setInDatabase(true);

			Iterator<Patient> i = clinicPatients.iterator();
			while (i.hasNext()) {
				Patient cPatient = i.next();
				if (hPatient.getPiz() != null && hPatient.getPiz().equals(cPatient.getPiz())) {
					logger.debug("found in local database " + cPatient.getPerson().getFullNameAndTitle());
					i.remove();
					// only save if update is performed
					copyPatientDataAndSave(hPatient, cPatient);
					break;
				}
			}

		}

		clinicPatients.stream().forEach(p -> p.setInDatabase(false));
		// adding other patients which are not in local database
		result.addAll(clinicPatients);

		return result;

	}

	/**
	 * Copies patient data from the source to the target.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public static boolean copyPatientData(Patient source, Patient target) {
		return copyPatientData(source, target, false);
	}

	/**
	 * Copies patient data from the source to the target, if forceAutoUpdate is true
	 * even if data shouln'd been copied.
	 * 
	 * @param source
	 * @param target
	 * @param forceAutoUpdate
	 * @return
	 */
	public static boolean copyPatientData(Patient source, Patient target, boolean forceAutoUpdate) {
		boolean change = false;

		if (HistoUtil.isStringDifferent(source.getPiz(), target.getPiz())) {
			change = true;
			target.setPiz(source.getPiz());
		}

		if (HistoUtil.isStringDifferent(source.getInsurance(), target.getInsurance())) {
			change = true;
			target.setInsurance(source.getInsurance());
		}

		// update person data if update is true
		change |= PersonService.copyPersonData(source.getPerson(), target.getPerson(), forceAutoUpdate);

		return change;
	}
}
