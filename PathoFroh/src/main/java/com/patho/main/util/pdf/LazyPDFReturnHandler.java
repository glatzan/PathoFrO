package com.patho.main.util.pdf;

import com.patho.main.model.PDFContainer;

public interface LazyPDFReturnHandler {
    public void returnPDFContent(PDFContainer container, String uuid);
}
