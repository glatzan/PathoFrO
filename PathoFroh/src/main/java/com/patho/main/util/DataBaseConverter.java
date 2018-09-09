package com.patho.main.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.patho.main.config.PathoConfig;
import com.patho.main.model.BioBank;
import com.patho.main.model.Council;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.BioBankRepository;
import com.patho.main.repository.PDFRepository;
import com.patho.main.repository.PatientRepository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class DataBaseConverter {

	@PersistenceContext
	protected EntityManager em;

	protected Session getSession() {
		return em.unwrap(Session.class);
	}

	protected CriteriaBuilder getCriteriaBuilder() {
		return getSession().getCriteriaBuilder();
	}

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientRepository patientRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private BioBankRepository bankRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFRepository pdfRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PathoConfig pathoConfig;
	
	public void start() {
		List<Patient> p = patientRepository.findAll();

		for (Patient patient : p) {
			System.out.println("Loading patient " + patient.getId() + " Name: " + patient.getPerson().getFullName());
			Hibernate.initialize(patient.getAttachedPdfs());
			for (PDFContainer container : patient.getAttachedPdfs()) {
				File file = new File(pathoConfig.getFileSettings().getBasePath() + "/" + patient.getId() + "/" + container.getId() + ".pdf");
				File thumbnail = new File(pathoConfig.getFileSettings().getBasePath() + "/" + patient.getId() + "/" + container.getId() + ".png");
				File fil2e = new File(pathoConfig.getFileSettings().getBasePath() + "/" + patient.getId() + "/");
				fil2e.mkdirs();

				System.out.println("-> writing file: " + container.getName());

				try {
					FileUtils.writeByteArrayToFile(file, container.getData());
					gerateThumbnail(container.getData(),thumbnail);
					container.setPath(patient.getId() + "/" + container.getId() + ".pdf");
					container.setThumbnail(patient.getId() + "/" + container.getId() + ".png");
					pdfRepository.save(container);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			for (Task task : patient.getTasks()) {
				Hibernate.initialize(task.getAttachedPdfs());

				if (!task.getAttachedPdfs().isEmpty()) {
					System.out.println("--> TASK " + task.getTaskID());
					for (PDFContainer c : task.getAttachedPdfs()) {
						System.out.println("--> writing file: " + c.getName());
						File fileT = new File(pathoConfig.getFileSettings().getBasePath() + "/" + patient.getId() + "/" + c.getId() + ".pdf");
						File thumbnail = new File(pathoConfig.getFileSettings().getBasePath() + "/" + patient.getId() + "/" + c.getId() + ".png");
						try {
							FileUtils.writeByteArrayToFile(fileT, c.getData());
							gerateThumbnail(c.getData(),thumbnail);
							c.setPath(patient.getId() + "/" + c.getId() + ".pdf");
							c.setThumbnail(patient.getId() + "/" + c.getId() + ".png");
							pdfRepository.save(c);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}

				Hibernate.initialize(task.getCouncils());

				for (Council council : task.getCouncils()) {
					Hibernate.initialize(council.getAttachedPdfs());

					if (!council.getAttachedPdfs().isEmpty()) {
						System.out.println("---> Council" + council.getName());
						for (PDFContainer c2 : council.getAttachedPdfs()) {
							System.out.println("---> writing file: " + c2.getName());
							File fileT = new File(pathoConfig.getFileSettings().getBasePath() + "/" + patient.getId() + "/" + c2.getId() + ".pdf");
							try {
								FileUtils.writeByteArrayToFile(fileT, c2.getData());
								c2.setPath(patient.getId() + "/" + c2.getId() + ".pdf");
								c2.setThumbnail(patient.getId() + "/" + c2.getId() + ".png");
								pdfRepository.save(c2);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}

				}

				Optional<BioBank> b = bankRepository.findOptionalByTask(task);

				if (b.isPresent()) {
					Hibernate.initialize(b.get().getAttachedPdfs());

					if (!b.get().getAttachedPdfs().isEmpty()) {
						System.out.println("---> Biobank");
						for (PDFContainer c2 : b.get().getAttachedPdfs()) {
							System.out.println("---> writing file: " + c2.getName());
							File fileT = new File(pathoConfig.getFileSettings().getBasePath() + "/" + patient.getId() + "/" + c2.getId() + ".pdf");
							try {
								FileUtils.writeByteArrayToFile(fileT, c2.getData());
								c2.setPath(patient.getId() + "/" + c2.getId() + ".pdf");
								c2.setThumbnail(patient.getId() + "/" + c2.getId() + ".png");
								pdfRepository.save(c2);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				}
			}

		}
	}

	public void gerateThumbnail(byte[] pdf, File path) {
		PDDocument document = null;
		try {
			document = PDDocument.load(pdf);

			PDFRenderer pdfRenderer = new PDFRenderer(document);

			for (PDPage page : document.getPages()) {
				// note that the page number parameter is zero based
				BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 50, ImageType.RGB);
				// suffix in filename will be used as the file format
				ImageIO.write(bim, "png", path);
				break;
			}

		} catch (Exception e) {
			System.err.println(e);
		} finally {
			try {
				document.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
