package com.patho.main.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SelectBeforeUpdate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.patho.main.model.interfaces.EditAbleEntity;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.interfaces.LogAble;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@SelectBeforeUpdate(true)
@SequenceGenerator(name = "materialPreset_sequencegenerator", sequenceName = "materialPreset_sequence")
public class MaterialPreset implements EditAbleEntity<MaterialPreset>, LogAble, ID, Serializable {

	private static final long serialVersionUID = 442137465482608899L;

	@Id
	@GeneratedValue(generator = "materialPreset_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	@Column
	private String name;

	@Column(columnDefinition = "text")
	private String commentary;

	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<StainingPrototype> stainingPrototypes = new ArrayList<StainingPrototype>();

	/**
	 * On every selection of the material, this number will be increased. The
	 * materials can be ordered according to this value. So often used materials
	 * will be displayed first.
	 */
	@Column
	private int priorityCount;

	@Column
	private boolean archived;
	
	public MaterialPreset() {
	}

	public MaterialPreset(MaterialPreset stainingPrototypeList) {
		this.id = stainingPrototypeList.getId();
		update(stainingPrototypeList);
	}

	@Override
	public String toString() {
		return "ID: " + getId() + " Name: " + getName();
	}

	@Transient
	public void update(MaterialPreset stainingPrototypeList) {
		this.name = stainingPrototypeList.getName();
		this.commentary = stainingPrototypeList.getCommentary();
		this.stainingPrototypes = new ArrayList<StainingPrototype>(stainingPrototypeList.getStainingPrototypes());
	}

	@Transient
	public String asGson() {
		final GsonBuilder builder = new GsonBuilder();
		builder.excludeFieldsWithoutExposeAnnotation();
		final Gson gson = builder.create();
		return gson.toJson(this);
	}

}
