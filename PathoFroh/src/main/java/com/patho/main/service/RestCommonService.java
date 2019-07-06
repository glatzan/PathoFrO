package com.patho.main.service;

import com.patho.main.model.user.HistoUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RestCommonService {

	public String generateRestToken(HistoUser user) {
		return  "";
	}
}
