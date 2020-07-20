package com.patho.main.config.excepion;

/**
 * Is thrown if to many search result are found in the clinic database
 *
 * @author glatza
 */

public class ToManyEntriesException extends Exception {

    private static final long serialVersionUID = 1276141793052183990L;

    public ToManyEntriesException() {
        super("To many search results found");
    }

}
