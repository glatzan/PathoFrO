package com.patho.main.model.favourites;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;

import com.patho.main.model.interfaces.ID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class FavouritePermissions implements ID {

	@Id
	@GeneratedValue(generator = "favouritepermission_sequencegenerator")
	@SequenceGenerator(name = "favouritepermission_sequencegenerator", sequenceName = "favouritepermission_sequence")
	@Column(unique = true, nullable = false)
	protected long id;

	@Column
	protected boolean admin;

	@Column
	protected boolean readable;

	@Column
	protected boolean editable;

	@ManyToOne(fetch = FetchType.LAZY)
	protected FavouriteList favouriteList;

}
