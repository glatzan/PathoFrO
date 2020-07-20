package com.patho.main.repository.jpa;

import com.patho.main.model.interfaces.DataList;

import java.util.List;

public interface DataListRepository {
    public void initialize(DataList datalist);

    public void initialize(List<DataList> datalists);
}
