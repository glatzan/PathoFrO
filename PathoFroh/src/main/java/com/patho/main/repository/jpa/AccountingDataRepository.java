package com.patho.main.repository.jpa;

import com.patho.main.model.dto.AccountingData;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AccountingDataRepository extends BaseRepository<AccountingData, Long> {
    public List<AccountingData> findAllBetweenDates(@Param("fromDate") LocalDate fromDate,
                                                    @Param("toDate") LocalDate toDate);
}
