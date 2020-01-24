package com.patho.main.rest;

import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.model.person.Person;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.jpa.PatientRepository;
import com.patho.main.repository.jpa.TaskRepository;
import com.patho.main.repository.jpa.UserRepository;
import com.patho.main.repository.miscellaneous.LDAPRepository;
import com.patho.main.service.AuthenticationService;
import com.patho.main.service.PDFService;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocumentType;
import com.patho.main.util.exceptions.TaskNotFoundException;
import com.patho.main.util.helper.HistoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@RestController
@RequestMapping(value = "/rest")
public class CommonControls {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ResourceBundle resourceBundle;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepositroy;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private LDAPRepository ldapRepository;

    @Autowired
    private PDFService pdfService;

    public static final String BASE_DN = "dc=ukl,dc=uni-freiburg,dc=de";

    private HistoUser u;

    @RequestMapping(value = "/test")
    // @Transactional
    public String test() {
        System.out.println("calledddasdsdddd");
        System.out.println(userRepository.findOptionalByPhysicianUid("glatza"));
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
            user.setLastLogin(Instant.now());
            user.setPhysician(new Physician(new Person()));
            user.setUsername("testAndiTest7");
            userRepository.save(user, "");

            Optional<HistoUser> tmp = userRepository.findOptionalByPhysicianUid("testAndiTest7");

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

    public List<String> getAllPersonNames() {
        return ldapTemplate.search(query().where("objectclass").is("person"), new AttributesMapper<String>() {
            public String mapFromAttributes(Attributes attrs) throws NamingException {
                printAllAttributes(attrs);
                return "";
            }
        });
    }

    /**
     * Uploads a file to a task or a patient. <br>
     * Header requires: Authorization with content Bearer ..... (is obtainable via
     * /rest/login) <br>
     * Body should contain: <br>
     * file, the file as a pdf <br>
     * piz (optional), piz of the patient, is ignored if taskID is present<br>
     * taskID (optional), taskID of the task <br>
     * fileName (optional), name of the file which is uploaded <br>
     * documentType (optional), type from enum DocumentType, if not provided UNKNOWN
     * is used.
     *
     * @param file
     * @param piz
     * @param taskID
     * @param fileName
     * @param documentType
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public @ResponseBody
    String handleFileUpload(@RequestParam("file") MultipartFile file,
                            @RequestParam(value = "piz", required = false) String piz,
                            @RequestParam(value = "taskID", required = false) String taskID,
                            @RequestParam(value = "fileName", required = false) String fileName,
                            @RequestParam(value = "documentType", required = false) PrintDocumentType documentType) {

        logger.debug("Calling updloag with piz {}, taskID {}, filename {}, document type {}", piz, taskID, fileName,
                documentType);

        if (!file.isEmpty()) {
            try {
                if (HistoUtil.isNullOrEmpty(piz) && HistoUtil.isNullOrEmpty(taskID)) {
                    logger.debug("Error: No Target for uploading the file was provided!");
                    return resourceBundle.get("rest.upload.error.noTarget");
                }

                if (HistoUtil.isNotNullOrEmpty(taskID)) {
                    try {
                        Task task = taskRepository.findByTaskID(taskID, false, false, true, false, false);
                        String name = fileName != null ? fileName : new PrintDocument(documentType).getGeneratedFileName();

                        pdfService.createPDFAndAddToDataList(task, file.getBytes(), true, name, documentType == null ? PrintDocumentType.UNKNOWN : documentType, "", "", task.getFileRepositoryBase().getPath());

                        logger.debug("Success: Uploading to task successful. (Task ID: {}, Filename: {})", taskID, name);
                        return resourceBundle.get("rest.upload.success.task", taskID, name);
                    } catch (TaskNotFoundException e) {
                        logger.error("Error: Task was not found. (Task ID: {})", taskID);
                        return resourceBundle.get("rest.upload.error.taskNotFound", taskID);
                    }


                }

                if (HistoUtil.isNotNullOrEmpty(piz)) {
                    Optional<Patient> p = patientRepositroy.findOptionalByPiz(piz, false, true, true);

                    if (!p.isPresent()) {
                        logger.error("Error: Patient was not found. (Patient PIZ: {})", piz);
                        return resourceBundle.get("rest.upload.error.patientNotFound", piz);
                    }

                    String name = fileName != null ? fileName : new PrintDocument(documentType).getGeneratedFileName();

                    pdfService.createPDFAndAddToDataList(p.get(), file.getBytes(), true, name, documentType == null ? PrintDocumentType.UNKNOWN : documentType, "", "", p.get().getFileRepositoryBase().getPath());

                    logger.debug("Success: Uploading to patient successful. (Patient PIZ: {}, Filename: {})", piz,
                            name);
                    return resourceBundle.get("rest.upload.success.patient", piz, name);
                }

            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Error: File could not be saved, try again.");
                return resourceBundle.get("rest.upload.error.internal");
            }

        }

        logger.error("Error: Upload faild, file is empty.");
        return resourceBundle.get("rest.upload.error.fileEmpty");

    }

    public static final void printAllAttributes(Attributes attrs) {
        for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMoreElements(); ) {
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
