package com.patho.main.repository.miscellaneous.impl;

import com.patho.main.repository.miscellaneous.MediaRepository;
import com.patho.main.repository.miscellaneous.PrintDocumentRepository;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocumentType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
@ConfigurationProperties(prefix = "patho.settings")
public class PrintDocumentRepositoryImpl implements PrintDocumentRepository {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private MediaRepository mediaRepository;

    private PrintDocument[] documents;

    @PostConstruct
    private void initializeDocuments() {
        for (PrintDocument printDocument : documents) {
            logger.debug("Initilizing " + printDocument.getName() + " - " + printDocument.getType() + " - Default "
                    + printDocument.getDefaultOfType());
            printDocument.setLoadedContent(mediaRepository.getString(printDocument.getContent()));
        }
    }

    public List<PrintDocument> findAllByTypes(PrintDocumentType... types) {
        return findAllByTypes(Arrays.asList(types));
    }

    public List<PrintDocument> findAllByTypes(List<PrintDocumentType> types) {
        List<PrintDocument> result = new ArrayList<PrintDocument>();

        if (types == null || types.size() == 0)
            return result;

        // return a copy of the printDocument
        for (PrintDocument printDocument : documents) {
            for (PrintDocumentType documentType : types) {
                if (printDocument.getDocumentType() == documentType) {
                    result.add(PrintDocumentRepository.loadDocument(printDocument));
                }
            }
        }

        return result;
    }

    public List<PrintDocument> findAll() {
        return Arrays.asList(documents).stream().map(p -> PrintDocumentRepository.loadDocument(p)).collect(Collectors.toList());
    }

    public Optional<PrintDocument> findByID(long id) {
        for (PrintDocument printDocument : documents) {
            if (printDocument.getId() == id)
                return Optional.ofNullable(PrintDocumentRepository.loadDocument(printDocument));
        }

        return Optional.empty();
    }

    public Optional<PrintDocument> findByTypeAndDefault(PrintDocumentType type) {
        List<PrintDocument> documents = findAllByTypes(type);
        for (PrintDocument printDocument : documents) {
            if (printDocument.getDefaultOfType())
                return Optional.ofNullable(PrintDocumentRepository.loadDocument(printDocument));
        }

        return Optional.empty();
    }

}
