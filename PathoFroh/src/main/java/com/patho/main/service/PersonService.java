package com.patho.main.service;

import com.patho.main.model.person.Contact;
import com.patho.main.model.person.Person;
import com.patho.main.repository.PersonRepository;
import com.patho.main.util.helper.HistoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PersonService extends AbstractService {

	@Autowired
	private PersonRepository personRepository;

	public Person copyPersonDataAndSave(Person source, Person target) {
		return copyPersonDataAndSave(source, target, false);
	}

	public Person copyPersonDataAndSave(Person source, Person target, boolean forceAutoUpdate) {
		if (copyPersonData(source, target, forceAutoUpdate))
			return personRepository.save(target, resourceBundle.get("log.person.copyData", target));
		return target;
	}

	public static boolean copyPersonData(Person source, Person target) {
		return copyPersonData(source, target, false);
	}

	public static boolean copyPersonData(Person source, Person target, boolean forceAutoUpdate) {
		boolean change = false;

		if (target.getAutoUpdate() || forceAutoUpdate) {
			if (HistoUtil.isStringDifferent(source.getTitle(), target.getTitle())) {
				change = true;
				target.setTitle(source.getTitle());
			}

			if (HistoUtil.isStringDifferent(source.getLastName(), target.getLastName())) {
				change = true;
				target.setLastName(source.getLastName());
			}

			if (HistoUtil.isStringDifferent(source.getFirstName(), target.getFirstName())) {
				change = true;
				target.setFirstName(source.getFirstName());
			}

			if (HistoUtil.isStringDifferent(source.getBirthday(), target.getBirthday())) {
				change = true;
				target.setBirthday(source.getBirthday());
			}

			if (HistoUtil.isStringDifferent(source.getGender(), target.getGender())) {
				change = true;
				target.setGender(source.getGender());
			}

			if (HistoUtil.isStringDifferent(source.getNote(), target.getNote())) {
				change = true;
				target.setNote(source.getNote());
			}

			if (HistoUtil.isStringDifferent(source.getLanguage(), target.getLanguage())) {
				change = true;
				target.setLanguage(source.getLanguage());
			}
			
			change |= copyContactData(source.getContact(), target.getContact());
		}
		
		if(source.getDefaultAddress() != target.getDefaultAddress()) {
			target.setDefaultAddress(source.getDefaultAddress());
			change = true;
		}

		return change;
	}

	public static boolean copyContactData(Contact source, Contact target) {
		boolean change = false;

		if (HistoUtil.isStringDifferent(source.getBuilding(), target.getBuilding())) {
			change = true;
			target.setBuilding(source.getBuilding());
		}

		if (HistoUtil.isStringDifferent(source.getStreet(), target.getStreet())) {
			change = true;
			target.setStreet(source.getStreet());
		}

		if (HistoUtil.isStringDifferent(source.getTown(), target.getTown())) {
			change = true;
			target.setTown(source.getTown());
		}
		
		if (HistoUtil.isStringDifferent(source.getAddressadditon(), target.getAddressadditon())) {
			change = true;
			target.setAddressadditon(source.getAddressadditon());
		}
		
		if (HistoUtil.isStringDifferent(source.getAddressadditon2(), target.getAddressadditon2())) {
			change = true;
			target.setAddressadditon2(source.getAddressadditon2());
		}
		
		if (HistoUtil.isStringDifferent(source.getPostcode(), target.getPostcode())) {
			change = true;
			target.setPostcode(source.getPostcode());
		}

		if (HistoUtil.isStringDifferent(source.getCountry(), target.getCountry())) {
			change = true;
			target.setCountry(source.getCountry());
		}

		if (HistoUtil.isStringDifferent(source.getPhone(), target.getPhone())) {
			change = true;
			target.setPhone(source.getPhone());
		}

		if (HistoUtil.isStringDifferent(source.getMobile(), target.getMobile())) {
			change = true;
			target.setMobile(source.getMobile());
		}

		if (HistoUtil.isStringDifferent(source.getEmail(), target.getEmail())) {
			change = true;
			target.setEmail(source.getEmail());
		}

		if (HistoUtil.isStringDifferent(source.getHomepage(), target.getHomepage())) {
			change = true;
			target.setHomepage(source.getHomepage());
		}

		if (HistoUtil.isStringDifferent(source.getFax(), target.getFax())) {
			change = true;
			target.setFax(source.getFax());
		}
		
		if (HistoUtil.isStringDifferent(source.getPager(), target.getPager())) {
			change = true;
			target.setPager(source.getPager());
		}
		
		return change;
	}

}
