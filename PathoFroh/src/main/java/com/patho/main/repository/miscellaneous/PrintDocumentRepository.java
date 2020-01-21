package com.patho.main.repository.miscellaneous;

import com.patho.main.dialog.print.documentUi.AbstractDocumentUi;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocumentType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public interface PrintDocumentRepository {

    public List<PrintDocument> findAllByTypes(PrintDocumentType... types);

    public List<PrintDocument> findAllByTypes(List<PrintDocumentType> types);

    public List<PrintDocument> findAll();

    public Optional<PrintDocument> findByID(long id);

    public Optional<PrintDocument> findByTypeAndDefault(PrintDocumentType type);

    /**
     * Loads documents with the correct document classes, copies content form
     * document to the new object
     */
    public static PrintDocument loadDocument(PrintDocument document) {
        PrintDocument copy;

        if (document.getTemplateName() == null)
            copy = (PrintDocument) document.clone();
        else {
            try {
                Class<?> myClass = Class.forName(document.getTemplateName());
                Constructor<?> constructor = myClass.getConstructor(new Class[]{PrintDocument.class});
                copy = (PrintDocument) constructor.newInstance(new Object[]{document});
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                copy = (PrintDocument) document.clone();
            }
        }
        return copy;
    }

    /**
     * Loads the ui class for a template
     */
    public static AbstractDocumentUi<? extends PrintDocument, ? extends AbstractDocumentUi.SharedData> loadUiClass(
            PrintDocument document) {
        return loadUiClass(document, null);
    }

    /**
     * Loads the ui class for a template, if a sharedData object is passed the new
     * object will share data with the an other one
     */
    public static AbstractDocumentUi<? extends PrintDocument, ? extends AbstractDocumentUi.SharedData> loadUiClass(
            PrintDocument document, AbstractDocumentUi.SharedData sharedData) {

        if (document.getUiClass() == null) {
            return new AbstractDocumentUi<PrintDocument, AbstractDocumentUi.SharedData>(document,
                    new AbstractDocumentUi.SharedData());
        } else {
            try {
                Class<?> myClass = Class.forName(document.getUiClass()).asSubclass(AbstractDocumentUi.class);

                if (sharedData == null) {
                    Constructor<?> constructor = myClass.getConstructor(new Class[]{document.getClass()});
                    return (AbstractDocumentUi<?, ?>) constructor.newInstance(new Object[]{document});
                } else {
                    Constructor<?> constructor = myClass
                            .getConstructor(new Class[]{document.getClass(), sharedData.getClass()});
                    return (AbstractDocumentUi<?, ?>) constructor.newInstance(new Object[]{document, sharedData});
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException
                    | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return new AbstractDocumentUi<PrintDocument, AbstractDocumentUi.SharedData>(document,
                new AbstractDocumentUi.SharedData());
    }

    /**
     * Returns the default document of a given type.
     */
    public static Optional<PrintDocument> getByTypAndDefault(List<PrintDocument> documents, PrintDocumentType type) {

        for (PrintDocument printDocument : documents) {
            if (printDocument.getDocumentType() == type && printDocument.getDefaultOfType())
                return Optional.ofNullable(printDocument);
        }
        return Optional.empty();
    }

    /**
     * Loads a gui object for a print template
     */
    public static AbstractDocumentUi<? extends PrintDocument, ? extends AbstractDocumentUi.SharedData> factory(PrintDocument document) {
        return factory(Arrays.asList(document)).get(0);
    }

    /**
     * Loads for a list of print templates all ui objects, if a sharedData context
     * exsists the data will be shared between objects.
     *
     * @param documents
     * @return
     */
    public static List<AbstractDocumentUi<? extends PrintDocument, ? extends AbstractDocumentUi.SharedData>> factory(List<PrintDocument> documents) {
        HashMap<Integer, AbstractDocumentUi.SharedData> sharedGroups = new HashMap<Integer, AbstractDocumentUi.SharedData>();

        List<AbstractDocumentUi<? extends PrintDocument, ? extends AbstractDocumentUi.SharedData>> result = new ArrayList<AbstractDocumentUi<? extends PrintDocument, ? extends AbstractDocumentUi.SharedData>>(documents.size());

        for (PrintDocument printDocument : documents) {
            // shared group exsist
            if (printDocument.getSharedContextGroup() != 0) {
                AbstractDocumentUi.SharedData sharedData = sharedGroups.get(printDocument.getSharedContextGroup());

                if (sharedData != null) {
                    result.add(PrintDocumentRepository.loadUiClass(printDocument, sharedData));
                } else {
                    AbstractDocumentUi<? extends PrintDocument, ? extends AbstractDocumentUi.SharedData> tmp = PrintDocumentRepository.loadUiClass(printDocument);
                    sharedGroups.put(printDocument.getSharedContextGroup(), tmp.getSharedData());
                    result.add(tmp);
                }
            } else {
                result.add(PrintDocumentRepository.loadUiClass(printDocument));
            }
        }

        return result;
    }

}