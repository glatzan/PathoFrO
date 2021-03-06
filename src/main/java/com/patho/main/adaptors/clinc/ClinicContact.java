package com.patho.main.adaptors.clinc;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.patho.main.model.patient.notification.ReportIntent;
import com.patho.main.model.person.Contact;
import com.patho.main.model.person.Person;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * "geschlecht":"Herr",
 * "tel":"07622/1313",
 * "fax":"",
 * "fachrichtung":"Innere Medizin",
 * "name":"Althof^Dennis",
 * "address":"Hauptstr. 17^^Schopfheim^^79650^D",
 * "type":"HAUS-ARZT"
 * <p>
 * <p>
 * "geschlecht":"Herrn",
 * "tel":"07623/2647",
 * "fax":"07623/747029",
 * "fachrichtung":"Augenheilkunde",
 * "name":"Schrenk^Matthias^^^^Dr. med.",
 * "address":"Friedrichstr. 23^^Rheinfelden^^79618^D",
 * "type":"EINW-ARZT"
 *
 * @author andi
 */
@Getter
@Setter
public class ClinicContact {

    /**
     * "geschlecht":"Herr"
     */
    @SerializedName(value = "geschlecht")
    private String gender;

    /**
     * "tel":"xxxx",
     */
    @SerializedName(value = "tel")
    private String phone;

    /**
     * "fax":"xxxx",
     */
    private String fax;

    /**
     * "fachrichtung":"Augenheilkunde",
     */
    @SerializedName(value = "fachrichtung")
    private String speciality;

    /**
     * "name":"name^surname^^^^title",
     */
    private String name;

    /**
     * "address":"street + number^^town^^plz^D"
     */
    private String address;

    /**
     * "type":"EINW-ARZT"
     */
    private String type;


    public static List<ClinicContact> factory(String jsonArray) {
        Type listType = new TypeToken<ArrayList<ClinicContact>>() {
        }.getType();
        List<ClinicContact> list = new Gson().fromJson(jsonArray, listType);

        return list;
    }

    public ReportIntent getContact() {
        ReportIntent reportIntent = new ReportIntent();

        Person person = new Person(new Contact());
        reportIntent.setPerson(person);

        String[] nameArr = getName().split("^|^^^^");

        if (nameArr.length < 2)
            throw new IllegalArgumentException("Erro");

        person.setLastName(nameArr[0]);
        person.setFirstName(nameArr[1]);

        if (nameArr.length == 3)
            person.setTitle(nameArr[2]);


        String[] addressArr = getAddress().split("^^|^");

        if (addressArr.length != 4)
            throw new IllegalArgumentException("Erro");

        person.getContact().setStreet(addressArr[0]);
        person.getContact().setTown(addressArr[1]);
        person.getContact().setPostcode(addressArr[1]);
        person.getContact().setCountry(addressArr[2]);

        return null;


    }
}
