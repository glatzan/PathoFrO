package com.patho.main.model.favourites;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SelectBeforeUpdate;

import com.patho.main.model.interfaces.ID;
import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;

import lombok.Getter;
import lombok.Setter;

@Entity
@SelectBeforeUpdate(true)
@Getter
@Setter
@SequenceGenerator(name = "favouritelistitem_sequencegenerator", sequenceName = "favouritelistitem_sequence")
public class FavouriteListItem implements ID {

	@Id
	@GeneratedValue(generator = "favouritelistitem_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private FavouriteList favouriteList;

	@OneToOne
	private Task task;

	@Column
	private String commentary;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Slide> slides;

	public FavouriteListItem() {
	}

	public FavouriteListItem(FavouriteList favouriteList, Task task) {
		this.task = task;
		this.favouriteList = favouriteList;
	}
}
