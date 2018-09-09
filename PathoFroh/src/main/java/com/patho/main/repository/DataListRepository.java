package com.patho.main.repository;

import java.util.List;

import com.patho.main.model.interfaces.DataList;

public interface DataListRepository{
	public void initialize(DataList datalist);
	public void initialize(List<DataList> datalists);
}
