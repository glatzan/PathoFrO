package com.patho.main.repository.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.patho.main.config.excepion.ToManyEntriesException;
import com.patho.main.model.dto.json.JSONPatientMapper;
import com.patho.main.model.patient.Patient;
import com.patho.main.repository.JSONPatientRepository;
import com.patho.main.util.helper.TimeUtil;

import lombok.Setter;

//{
//"vorname":"Andreas",
//"mode":null,
//"status":null,
//"piz":"20366346",
//"sonderinfo":null,
//"iknr":null,
//"kvnr":null,
//"titel":null,
//"versichertenstatus":" ",
//"tel":null,
//"anschrift":"Tannenweg 34",
//"wop":null,
//"plz":"79183",
//"name":"Glatz",
//"geburtsdatum":"1988-10-04",
//"gueltig_bis":null,
//"krankenkasse":null,
//"versnr":null,
//"land":"D",
//"weiblich":"",
//"ort":"Waldkirch",
//"status2":null
//}

//{
//
//+      "vorname":"Ioana Maria",
//->       "mode":"K",
//->       "status":"1000",
//ok      "piz":"29017379",
//?      "sonderinfo":"",
//->      "iknr":"61125",
//->      "kvnr":"108018121",
//ok      "titel":null,
//->      "versichertenstatus":"1 ",
//ok     "tel":"0176 62346167",
//ok      "anschrift":"Habsburgerstr. 25",
//?      "wop":"",
//ok      "plz":"79104",
//ok      "name":"Cazana",
//ok      "geburtsdatum":"1989-10-09",
//?     "gueltig_bis":null,
//ok      "krankenkasse":"AOK Baden-W�rttemberg",
//?     "versnr":"U367703198",
//ok      "land":"D",
//ok      "weiblich":"1",
//ok      "ort":"Freiburg",
//"status2":"1"
//
//}

@Service
@ConfigurationProperties(prefix = "patho.json")
@Setter
public class JSONPatientRepositoryImpl implements JSONPatientRepository {

	private String patientByPizUrl;

	private String patientByNameSurnameBirthday;

	public Optional<Patient> findByPIZ(String piz) {
		return find(patientByPizUrl.replace("$piz", piz));
	}

	public List<Patient> findByNameAndSurNameAndBirthday(String name, String surname, Date birthday)
			throws ToManyEntriesException {
		String result = patientByNameSurnameBirthday.replace("$name", name == null ? "" : name)
				.replace("$surname", surname == null ? "" : surname)
				.replace("$birthday", birthday != null ? TimeUtil.formatDate(birthday, "yyyy-MM-dd") : "");
		return findAll(result);
	}

	public Optional<Patient> find(String url) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JSONPatientMapper userMapper = mapper.readValue(new URL(url), JSONPatientMapper.class);
			return Optional.ofNullable(userMapper.getPatient());
		} catch (IOException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	public List<Patient> findAll(String url) throws ToManyEntriesException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			System.out.println(url);
			TypeReference<List<JSONPatientMapper>> typeRef = new TypeReference<List<JSONPatientMapper>>() {
			};
			List<JSONPatientMapper> userMapper = mapper.readValue(new URL(url), typeRef);
			return userMapper.stream().map(p -> p.getPatient()).collect(Collectors.toList());
		} catch (UnrecognizedPropertyException e) {
			throw new ToManyEntriesException();
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<Patient>();
		}
	}
}
