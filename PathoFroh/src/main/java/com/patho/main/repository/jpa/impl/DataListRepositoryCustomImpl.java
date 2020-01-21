package com.patho.main.repository.jpa.impl;

import com.patho.main.model.interfaces.DataList;
import com.patho.main.repository.jpa.DataListRepository;
import com.patho.main.repository.jpa.custom.impl.AbstractRepositoryCustom;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataListRepositoryCustomImpl extends AbstractRepositoryCustom implements DataListRepository {

    public void initialize(DataList datalist) {
        Hibernate.initialize(datalist.getAttachedPdfs());
    }

    public void initialize(List<DataList> datalists) {
        for (DataList dataList : datalists) {
            initialize(dataList);
        }
    }
}
