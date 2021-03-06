package com.patho.main.util;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;

public class DataBaseConverter {

    @PersistenceContext
    protected EntityManager em;

    protected Session getSession() {
        return em.unwrap(Session.class);
    }

    protected CriteriaBuilder getCriteriaBuilder() {
        return getSession().getCriteriaBuilder();
    }

    public void start() {
//        val pipe = ArrayChannel<Deferred<ImageFile>>(20)
//        launch {
//            while (!(pipe.isEmpty && pipe.isClosedForSend)) {
//                imageFiles.add(pipe.receive().await())
//            }
//            println("pipe closed")
//        }
//        File("/Users/me/").walkTopDown()
//                .onFail { file, ex -> println("ERROR: $file caused $ex") }
//        .forEach { pipe.send(async { ImageFile.fromFile(it) }) }
//        pipe.close()

//		List<PDFContainer> ignoredContainer = new ArrayList<PDFContainer>();
//		List<Patient> p = patientRepository.findAll();
//
//		for (Patient patient : p) {
//			System.out.println("Loading patient " + patient.getId() + " Name: " + patient.getPerson().getFullName());
//			Hibernate.initialize(patient.getAttachedPdfs());
//
//			String folder = pathoConfig.getFileSettings().getFileRepository() + "/" + patient.getId();
//
//			for (int i = 0; i < patient.getAttachedPdfs().size(); i++) {
//				PDFContainer container = HistoUtil.getNElement(patient.getAttachedPdfs(), i);
//
//				if (container.getName() != null && !container.getName().matches("^((.*\\.pdf)|([A-Za-z0-9 ]*))$")) {
//					System.out.println("ignoring " + container.getName());
//					ignoredContainer.add(container);
//					continue;
//				}
//
//				File uniqueFile = new File(folder, mediaRepository.getUniqueName(folder, ".pdf"));
//				container.setPath(uniqueFile.getPath());
//				container.setThumbnail(uniqueFile.getPath().replace(".pdf", ".png"));
//				byte[] data = container.getData();
//				container.setData(null);
//				patient = (Patient) pdfService.createAndAttachPDF(patient, container, data, true)
//						.getDataList();
//
//				System.out.println("-> writing file: " + container.getName());
//			}
//
//			for (int i = 0; i < patient.getTasks().size(); i++) {
//
//				Task task = HistoUtil.getNElement(patient.getTasks(), i);
//
//				Hibernate.initialize(task);
//
//				if (!task.getAttachedPdfs().isEmpty()) {
//					System.out.println("--> TASK " + task.getTaskID());
//
//					for (int y = 0; y < task.getAttachedPdfs().size(); y++) {
//
//						PDFContainer c = HistoUtil.getNElement(task.getAttachedPdfs(), y);
//
//						if (c.getName() != null && !c.getName().matches("^((.*\\.pdf)|([A-Za-z0-9 ]*))$")) {
//							System.out.println("ignoring " + c.getName());
//							ignoredContainer.add(c);
//							continue;
//						}
//
//						File uniqueFile = new File(folder, mediaRepository.getUniqueName(folder, ".pdf"));
//						c.setPath(uniqueFile.getPath());
//						c.setThumbnail(uniqueFile.getPath().replace(".pdf", ".png"));
//						byte[] data = c.getData();
//						c.setData(null);
//						task = (Task) pdfService.createAndAttachPDF(task, c,data, true).getDataList();
//
//						System.out.println("--> writing file: " + c.getName());
//					}
//				}
//
//				Hibernate.initialize(task.getCouncils());
//
//				for (Council council : task.getCouncils()) {
//					Hibernate.initialize(council.getAttachedPdfs());
//
//					if (!council.getAttachedPdfs().isEmpty()) {
//						System.out.println("---> Council" + council.getName());
//						for (int y = 0; y < council.getAttachedPdfs().size(); y++) {
//
//							PDFContainer c2 = HistoUtil.getNElement(council.getAttachedPdfs(), y);
//
//							if (c2.getName() != null && !c2.getName().matches("^((.*\\.pdf)|([A-Za-z0-9 ]*))$")) {
//								System.out.println("ignoring " + c2.getName());
//								ignoredContainer.add(c2);
//								continue;
//							}
//
//							File uniqueFile = new File(folder, mediaRepository.getUniqueName(folder, ".pdf"));
//							c2.setPath(uniqueFile.getPath());
//							c2.setThumbnail(uniqueFile.getPath().replace(".pdf", ".png"));
//							byte[] data = c2.getData();
//							c2.setData(null);
//							council = (Council) pdfService.createAndAttachPDF(council, c2, data, true)
//									.getDataList();
//
//							System.out.println("---> writing file: " + c2.getName());
//						}
//					}
//
//				}
//
//				Optional<BioBank> b = bankRepository.findOptionalByTask(task);
//
//				if (b.isPresent()) {
//					Hibernate.initialize(b.get().getAttachedPdfs());
//
//					if (!b.get().getAttachedPdfs().isEmpty()) {
//						System.out.println("---> Biobank");
//						BioBank bio = b.get();
//						for (int y = 0; y < b.get().getAttachedPdfs().size(); y++) {
//
//							PDFContainer c2 = HistoUtil.getNElement(bio.getAttachedPdfs(), y);
//
//							if (c2.getName() != null && !c2.getName().matches("^((.*\\.pdf)|([A-Za-z0-9 ]*))$")) {
//								System.out.println("ignoring " + c2.getName());
//								ignoredContainer.add(c2);
//								continue;
//							}
//
//							File uniqueFile = new File(folder, mediaRepository.getUniqueName(folder, ".pdf"));
//							c2.setPath(uniqueFile.getPath());
//							c2.setThumbnail(uniqueFile.getPath().replace(".pdf", ".png"));
//							byte[] data = c2.getData();
//							c2.setData(null);
//							bio = (BioBank) pdfService.createAndAttachPDF(bio, c2, data, true).getDataList();
//
//							System.out.println("---> writing file: " + c2.getName());
//						}
//					}
//				}
//			}

//		}
//
//		System.out.println("Container ignored " + ignoredContainer.size());
//		for (PDFContainer pdfContainer : ignoredContainer) {
//			System.out.println("-> " + pdfContainer.getName());
//		}
    }
}
