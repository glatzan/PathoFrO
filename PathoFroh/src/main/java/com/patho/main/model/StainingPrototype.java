package com.patho.main.model;

import com.patho.main.model.interfaces.ID;
import org.hibernate.annotations.SelectBeforeUpdate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@SelectBeforeUpdate(true)
@SequenceGenerator(name = "stainingPrototype_sequencegenerator", sequenceName = "stainingPrototype_sequence")
public class StainingPrototype implements ID {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_IMMUN = 1;

    @Id
    @GeneratedValue(generator = "stainingPrototype_sequencegenerator")
    @Column(unique = true, nullable = false)
    private long id;

    @Column(columnDefinition = "VARCHAR")
    private String name;

    @Column(columnDefinition = "VARCHAR")
    private String commentary;

    @Enumerated(EnumType.STRING)
    private StainingType type;

    @Column
    private boolean archived;

    /**
     * On every selection of the staining, this number will be increased. The
     * staining can be ordered according to this value. So often used stainings
     * will be displayed first.
     */
    @Column
    private int priorityCount;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "staining")
    @OrderColumn(name = "INDEX")
    private List<StainingPrototypeDetails> batchDetails = new ArrayList<StainingPrototypeDetails>();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StainingPrototype && ((StainingPrototype) obj).getId() == getId())
            return true;
        return super.equals(obj);
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCommentary() {
        return this.commentary;
    }

    public StainingType getType() {
        return this.type;
    }

    public boolean isArchived() {
        return this.archived;
    }

    public int getPriorityCount() {
        return this.priorityCount;
    }

    public List<StainingPrototypeDetails> getBatchDetails() {
        return this.batchDetails;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public void setType(StainingType type) {
        this.type = type;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public void setPriorityCount(int priorityCount) {
        this.priorityCount = priorityCount;
    }

    public void setBatchDetails(List<StainingPrototypeDetails> batchDetails) {
        this.batchDetails = batchDetails;
    }

    public static enum StainingType {
        NORMAL, IMMUN;
    }

}
