package com.patho.main.model;

import com.patho.main.common.ContactRole;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.interfaces.ListOrder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@SequenceGenerator(name = "diagnosisPreset_sequencegenerator", sequenceName = "diagnosisPreset_sequence")
public class DiagnosisPreset implements ListOrder<DiagnosisPreset>, ID, Serializable {

    private static final long serialVersionUID = 7345658902599657920L;

    @Id
    @GeneratedValue(generator = "diagnosisPreset_sequencegenerator")
    @Column(unique = true, nullable = false)
    private long id;

    @Column(columnDefinition = "VARCHAR")
    private String category;

    @Column(columnDefinition = "VARCHAR")
    private String icd10;

    @Column(columnDefinition = "VARCHAR")
    private boolean malign;

    @Column(columnDefinition = "text")
    private String diagnosis;

    @Column(columnDefinition = "text")
    private String extendedDiagnosisText;

    @Column(columnDefinition = "text")
    private String commentary;

    @Column
    private int indexInList;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL})
    private Set<ContactRole> diagnosisReportAsLetter;

    @Column
    private boolean archived;

    public DiagnosisPreset() {
    }

    public DiagnosisPreset(DiagnosisPreset diagnosisPreset) {
        this.id = diagnosisPreset.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiagnosisPreset) {
            if (getId() == ((DiagnosisPreset) obj).getId()) {
                return true;
            }
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }

    /**
     * Used for gui, can only handle arrays
     *
     * @return
     */
    @Transient
    public ContactRole[] getDiagnosisReportAsLetterAsArray() {
        return getDiagnosisReportAsLetter() != null
                ? (ContactRole[]) getDiagnosisReportAsLetter()
                .toArray(new ContactRole[getDiagnosisReportAsLetter().size()])
                : new ContactRole[0];
    }

    public void setDiagnosisReportAsLetterAsArray(ContactRole[] diagnosisReportAsLetter) {
        this.diagnosisReportAsLetter = new HashSet<>(Arrays.asList(diagnosisReportAsLetter));
    }

    public long getId() {
        return this.id;
    }

    public String getCategory() {
        return this.category;
    }

    public String getIcd10() {
        return this.icd10;
    }

    public boolean isMalign() {
        return this.malign;
    }

    public String getDiagnosis() {
        return this.diagnosis;
    }

    public String getExtendedDiagnosisText() {
        return this.extendedDiagnosisText;
    }

    public String getCommentary() {
        return this.commentary;
    }

    public int getIndexInList() {
        return this.indexInList;
    }

    public Set<ContactRole> getDiagnosisReportAsLetter() {
        return this.diagnosisReportAsLetter;
    }

    public boolean isArchived() {
        return this.archived;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIcd10(String icd10) {
        this.icd10 = icd10;
    }

    public void setMalign(boolean malign) {
        this.malign = malign;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void setExtendedDiagnosisText(String extendedDiagnosisText) {
        this.extendedDiagnosisText = extendedDiagnosisText;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public void setIndexInList(int indexInList) {
        this.indexInList = indexInList;
    }

    public void setDiagnosisReportAsLetter(Set<ContactRole> diagnosisReportAsLetter) {
        this.diagnosisReportAsLetter = diagnosisReportAsLetter;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
