package com.patho.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.user.HistoUser;

@Service
@Transactional
public class RestCommonService {

	public String generateRestToken(HistoUser user) {
		return  "";
	}
}
