package com.patho.main.repository.jpa.custom;

import com.patho.main.model.person.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepositoryCustom {

    public Optional<Organization> findOptionalByName(String name, boolean initilizePerson);

    public Optional<Organization> findOptionalByID(long id, boolean initilizePerson);

    public List<Organization> findAll(boolean irgnoreArchived);

    public List<Organization> findAll(boolean loadPersons, boolean irgnoreArchived);

    public List<Organization> findAllOrderByIdAsc(boolean irgnoreArchived);

    public List<Organization> findAllOrderByIdAsc(boolean loadPersons, boolean irgnoreArchived);
}
