package com.patho.main.model.user;

import com.patho.main.model.Physician;
import com.patho.main.model.interfaces.ArchivAble;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.user.HistoGroup.AuthRole;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "user_sequencegenerator", sequenceName = "user_sequence")
@Getter
@Setter
public class HistoUser implements UserDetails, Serializable, ID, ArchivAble {

    private static final long serialVersionUID = 8292898827966568346L;

    @Id
    @GeneratedValue(generator = "user_sequencegenerator")
    @Column(unique = true, nullable = false)
    private long id;

    @Version
    private long version;

    @Column
    private long lastLogin;

    @Column(columnDefinition = "boolean default false")
    private boolean archived;

    @Column(columnDefinition = "boolean default false")
    private boolean localUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotAudited
    private HistoGroup group;

    @OneToOne(cascade = CascadeType.ALL)
    @NotAudited
    private HistoSettings settings;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Physician physician;

    @Transient
    private boolean accountNonExpired = true;
    @Transient
    private boolean accountNonLocked = true;
    @Transient
    private boolean credentialsNonExpired = true;
    @Transient
    private boolean enabled = true;

    /**
     * Constructor for Hibernate
     */
    public HistoUser() {
    }

    public HistoUser(Physician physician) {
        this(physician, null);
    }

    public HistoUser(Physician physician, HistoSettings settings) {
        this.physician = physician;
        this.settings = settings;
    }

    @Transient
    public String getUsername() {
        return getPhysician().getUid();
    }

    @Transient
    public void setUsername(String username) {
        getPhysician().setUid(username);
    }

    @Transient
    public List<HistoGroup> getAuthorities() {
        List<HistoGroup> result = new ArrayList<HistoGroup>();

        if (group == null)
            result.add(new HistoGroup(null, AuthRole.ROLE_NONEAUTH));
        else {
            result.add(group);
        }

        return result;
    }

    @Transient
    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String toString() {
        return "HistoUser [id=" + id + ", username=" + getUsername() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HistoUser && ((HistoUser) obj).getId() == getId())
            return true;
        return super.equals(obj);
    }
}
