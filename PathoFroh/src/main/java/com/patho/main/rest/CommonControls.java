package com.patho.main.rest;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.patho.main.model.PDFContainer;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.LDAPRepository;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.AuthenticationService;

@RestController
@RequestMapping(value = "/rest")
public class CommonControls {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LdapTemplate ldapTemplate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PatientRepository patientRepositroy;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private LDAPRepository ldapRepository;

	public static final String BASE_DN = "dc=ukl,dc=uni-freiburg,dc=de";

	private HistoUser u;

	@RequestMapping(value = "/test")
	// @Transactional
	public String test() {
		System.out.println("calledddasdsdddd");
		System.out.println(userRepository.findOptionalByUsername("glatza"));
		System.out.println(userRepository.findByGroupId(2));
		System.out.println("ttut2s");
		logger.debug("Test {}", "test");
		logger.info("Test {}", "test");
		Optional<Physician> p = ldapRepository.findByUid("glatza");

		if (p.isPresent())
			System.out.println(p.get().getClinicRole());

		if (u == null) {
			HistoUser user = new HistoUser();
			userRepository.save(user, "hallo das test");
			user.setLastLogin(System.currentTimeMillis());
			user.setPhysician(new Physician(new com.patho.main.model.Person()));
			user.setUsername("testAndiTest7");
			userRepository.save(user, "");

			Optional<HistoUser> tmp = userRepository.findOptionalByUsername("testAndiTest7");

			u = tmp.get();
			System.out.println(tmp.get().getId());
		} else {
			System.out.println("update");
			u.setUsername("tutututut" + System.currentTimeMillis());
			try {
				u = userRepository.save(u, "total save");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("tut-------sadddddddddddddddd------------------------------------------");
			}
			System.out.println("tut-------------------------------------------------");
		}

		// for ( Physician ps : pa) {
		// System.out.println(ps.getPerson().getFullName());
		// }

		return "hallo";
	}

	private static class PersonContextMapper implements ContextMapper {
		public Object mapFromContext(Object ctx) {
			DirContextAdapter context = (DirContextAdapter) ctx;
			printAllAttributes(context.getAttributes());
			System.out.println(context.getStringAttribute("cn"));
			return new Object();
		}
	}

	public Object findByPrimaryKey(String name) {
		Name dn = buildDn(name);
		return ldapTemplate.lookup(dn, new PersonContextMapper());
	}

	protected Name buildDn(String a) {
		return LdapNameBuilder.newInstance().add("ou", "people").add("uid", a).build();
	}

	@RequestMapping(value = "/test2")
	public String test2() {
		// getAllPersonNames();
		authenticationService.authenticateWithLDAP("glatza", "Asus1212?s");
		return "hallo";
	}

	// @RequestMapping(value = "/login")
	// public String login(@RequestBody ) {
	// if(authenticationService.authenticate(uuid, password))
	//
	//
	// return "hallo";
	// }

	public List<String> getAllPersonNames() {
		return ldapTemplate.search(query().where("objectclass").is("person"), new AttributesMapper<String>() {
			public String mapFromAttributes(Attributes attrs) throws NamingException {
				printAllAttributes(attrs);
				return "";
			}
		});
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public @ResponseBody String handleFileUpload(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "piz", required = true) String piz) {
		String name = "test11";
		if (!file.isEmpty()) {
			try {
				byte[] bytes = file.getBytes();
				BufferedOutputStream stream = new BufferedOutputStream(
						new FileOutputStream(new File("d:\\" + name + "-uploaded")));
				stream.write(bytes);
				stream.close();

				Optional<Patient> p = patientRepositroy.findOptionalByPiz(piz, false, true, true);

				if (p.isPresent()) {
					logger.error("Rest Upload: no patient was found");
					return "You failed to upload " + name + " patient not known.";
				}

				PDFContainer test = new PDFContainer();
				test.setData(bytes);
				test.setName("TEST");

				// pdfService.attachPDF(p, p, test);

				return "You successfully uploaded " + name + " into " + name + "-uploaded !";
			} catch (Exception e) {
				logger.error("Rest Upload: Failed to upload \" + name + \" because the file was empty.");
				return "You failed to upload " + name + " => " + e.getMessage();
			}
		} else {
			logger.error("Rest Upload: Failed to upload \" + name + \" because the file was empty.");
			return "You failed to upload " + name + " because the file was empty.";
		}
	}

	// public boolean authenticate(String userDn, String credentials) {
	// DirContext ctx = null;
	// try {
	// ctx = contextSource.getContext(userDn, credentials);
	// return true;
	// } catch (Exception e) {
	// // Context creation failed - authentication did not succeed
	// logger.error("Login failed", e);
	// return false;
	// } finally {
	// // It is imperative that the created DirContext instance is always closed
	// LdapUtils.closeContext(ctx);
	// }
	// }

	public static final void printAllAttributes(Attributes attrs) {
		for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMoreElements();) {
			Attribute attr;
			try {
				attr = (Attribute) ae.next();
				String attrId = attr.getID();
				for (Enumeration<?> vals = attr.getAll(); vals.hasMoreElements(); System.out
						.println(attrId + ": " + vals.nextElement()))
					;
			} catch (NamingException e) {
			}

		}
	}

}
