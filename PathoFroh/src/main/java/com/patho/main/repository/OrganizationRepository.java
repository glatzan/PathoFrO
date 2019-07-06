package com.patho.main.repository;

import com.patho.main.model.person.Organization;
import com.patho.main.repository.service.OrganizationRepositoryCustom;

import java.util.Optional;

public interface OrganizationRepository extends BaseRepository<Organization, Long>, OrganizationRepositoryCustom {
	
	public Optional<Organization> findOptionalByName(String name);
	
}
