package com.patho.main.model.favourites;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import com.patho.main.model.interfaces.ID;

import lombok.Getter;
import lombok.Setter;

//@Entity
@Getter
@Setter
@MappedSuperclass
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class FavouritePermissions implements ID {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
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
