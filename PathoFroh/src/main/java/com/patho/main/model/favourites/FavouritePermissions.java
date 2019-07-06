package com.patho.main.model.favourites;

import com.patho.main.model.interfaces.ID;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

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
