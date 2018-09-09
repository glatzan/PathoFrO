package com.patho.main.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.query.Param;

import com.patho.main.model.dto.AccountingData;

public interface AccountingDataRepository extends BaseRepository<AccountingData, Long> {
	public List<AccountingData> findAllBetweenDates(@Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate);
}
