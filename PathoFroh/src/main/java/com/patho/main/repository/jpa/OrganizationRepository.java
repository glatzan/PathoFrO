package com.patho.main.repository.jpa;

import com.patho.main.model.person.Organization;
import com.patho.main.repository.jpa.custom.OrganizationRepositoryCustom;

import java.util.Optional;

public interface OrganizationRepository extends BaseRepository<Organization, Long>, OrganizationRepositoryCustom {

    public Optional<Organization> findOptionalByName(String name);

}
