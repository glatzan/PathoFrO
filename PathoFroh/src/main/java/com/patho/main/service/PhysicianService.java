package com.patho.main.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.ContactRole;
import com.patho.main.model.Organization;
import com.patho.main.model.Physician;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.repository.LDAPRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.util.helper.HistoUtil;

@Service
@Transactional
public class PhysicianService extends AbstractService {

	@Autowired
	private PhysicianRepository physicianRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private LDAPRepository ldapRepository;

	/**
	 * Updates the data of a physician from the ldap backend
	 * 
	 * @param user
	 */
	public Physician updatePhysicianWithLdapData(Physician physician) {
		Optional<Physician> oPhysician = ldapRepository.findByUid(physician.getUid());

		if (oPhysician.isPresent())
			return mergePhysicians(oPhysician.get(), physician);
		else
			throw new IllegalStateException("No user was found");
	}

	/**
	 * Checks if physician is saved in database, if so the saved physician will be
	 * updated, otherwise a new physician will be created.
	 * 
	 * @param physician
	 * @return
	 */
	public Physician addOrMergePhysician(Physician physician) {
		// if the physician was added as surgeon the useracc an the
		// physician will be merged
		Optional<Physician> physicianFromDatabase = physician.getUid() != null
				? physicianRepository.findOptionalByUid(physician.getUid())
				: Optional.empty();

		// undating the foud physician
		if (physicianFromDatabase.isPresent()) {
			logger.info("Physician already in database " + physician.getPerson().getFullName());

			Set<ContactRole> tmpRoles = physician.getAssociatedRoles();

			physician = mergePhysicians(physician, physicianFromDatabase.get());

			physician.setArchived(false);

			// overwriting roles for passed physician
			physician.setAssociatedRoles(tmpRoles);

			return physicianRepository.save(physicianFromDatabase.get(), resourceBundle.get(
					"log.settings.physician.patho.ldap.update", physicianFromDatabase.get().getPerson().getFullName()));
		} else {
			logger.info("Creating new phyisician " + physician.getPerson().getFullName());

			// removing physicians temp id
			// TODO crate container object
			physician.setId(0);

			organizationService.synchronizeOrganizations(physician.getPerson().getOrganizsations());

			return physicianRepository.save(physician,
					resourceBundle.get("log.settings.physician.patho.ldap.save", physician.getPerson().getFullName()));
		}
	}

	/**
	 * Simple save, sets a default role if no role was set
	 * 
	 * @param physician
	 * @return
	 */
	public Physician savePhysican(Physician physician) {
		if (physician.hasNoAssociateRole())
			physician.addAssociateRole(ContactRole.OTHER_PHYSICIAN);

		return physicianRepository.save(physician,
				resourceBundle.get("log.settings.physician.physician.edit", physician.getPerson().getFullName()));

	}

	/**
	 * Merges two physicians an updates their organizations
	 * 
	 * @param source
	 * @param destination
	 */
	public Physician mergePhysicians(Physician source, Physician target) {
		organizationService.synchronizeOrganizations(target.getPerson().getOrganizsations());
		return copyPhysicianDataAndSave(source, target);
	}

	/**
	 * Tries to delete the old group
	 * 
	 * @param p
	 */
	@Transactional(propagation = Propagation.NEVER)
	public boolean deleteOrArchive(Physician physician) {
		try {
			physicianRepository.delete(physician,
					resourceBundle.get("log.settings.physician.deleted", physician.getPerson().getFullName()));
			return true;
		} catch (Exception e) {
			archive(physician, true);
			return false;
		}
	}

	/**
	 * Archived or dearchives a physician
	 * 
	 * @param p
	 * @param archive
	 * @return
	 */
	public Physician archive(Physician physician, boolean archive) {
		physician.setArchived(archive);
		return physicianRepository.save(physician,
				resourceBundle.get(archive ? "log.settings.physician.archived" : "log.settings.physician.dearchived",
						physician.getPerson().getFullName()));
	}

	/**
	 * Copies patient data from the source to the target. Saves the target.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public Physician copyPhysicianDataAndSave(Physician source, Physician target) {
		return copyPhysicianDataAndSave(source, target, false);
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
	public Physician copyPhysicianDataAndSave(Physician source, Physician target, boolean forceAutoUpdate) {
		if (copyPhysicianData(source, target, forceAutoUpdate))
			return physicianRepository.save(target, resourceBundle.get("log.patient.copyData", target));
		return target;
	}

	public static boolean copyPhysicianData(Physician source, Physician target) {
		return copyPhysicianData(source, target, false);
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
	public static boolean copyPhysicianData(Physician source, Physician target, boolean forceAutoUpdate) {
		boolean change = false;

		if (HistoUtil.isStringDifferent(source.getEmployeeNumber(), target.getEmployeeNumber())) {
			change = true;
			target.setEmployeeNumber(source.getEmployeeNumber());
		}

		if (HistoUtil.isStringDifferent(source.getUid(), target.getUid())) {
			change = true;
			target.setUid(source.getUid());
		}

		if (HistoUtil.isStringDifferent(source.getClinicRole(), target.getClinicRole())
				&& (target.getPerson().isAutoUpdate() || forceAutoUpdate)) {
			change = true;
			target.setClinicRole(source.getClinicRole());
		}

		// update person data if update is true
		change |= PersonService.copyPersonData(source.getPerson(), target.getPerson(), forceAutoUpdate);

		return change;
	}

}
