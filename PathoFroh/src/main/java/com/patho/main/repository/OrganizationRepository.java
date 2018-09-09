package com.patho.main.repository;

import java.util.Optional;

import com.patho.main.model.Organization;
import com.patho.main.repository.service.OrganizationRepositoryCustom;

public interface OrganizationRepository extends BaseRepository<Organization, Long>, OrganizationRepositoryCustom {
	
	public Optional<Organization> findOptionalByName(String name);
	
}
