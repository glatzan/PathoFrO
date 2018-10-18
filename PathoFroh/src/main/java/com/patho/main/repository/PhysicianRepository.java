package com.patho.main.repository;

import java.util.List;
import java.util.Optional;

import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.repository.service.PhysicianRepositoryCustom;

public interface PhysicianRepository extends BaseRepository<Physician, Long>, PhysicianRepositoryCustom {

	Optional<Physician> findOptionalByPerson(Person person);

	Optional<Physician> findOptionalByUid(String uid);

	Optional<Physician> findOptionalByPersonLastName(String name);
	
	List<Physician> findAllByPersonLastName(String name);

}
