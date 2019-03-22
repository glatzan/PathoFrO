package com.patho.main.model.dto;

import javax.persistence.*;

@NamedNativeQuery(
	    name = "favouriteListMenuItemDTO",
	    query =
	        "select " +
	        "flist.id AS id, " +
	        "flist.name AS name, " + 
	        "flist.usedumplist AS dumplist, " +
	        "flist.hidelist as hidden, "+
	        "coalesce(bool_or(fitem.task_id = :task_id),false) As containstask " +
	        "from favouritelist flist " +
	        "left join favouritepermissionsgroup fgroup on fgroup.favouritelist_id = flist.id "+
	        "left join favouritepermissionsuser fuser on fuser.favouritelist_id = flist.id "+
	        "left join favouritelistitem fitem on fitem.favouritelist_id = flist.id "+
	        "left join favouritelist_histouser hideList on hideList.favouritelist_id = flist.id "+
	        "where (owner_id = :user_id or fgroup.group_id = :group_id or fuser.user_id = :user_id) and (hideList.hidelistforuser_id IS NULL or hideList.hidelistforuser_id != :user_id) " +
	        "GROUP BY flist.id",
	    resultSetMapping = "FavouriteListMenuItem"
	)
	@SqlResultSetMapping(
	    name = "FavouriteListMenuItem",
	    classes = @ConstructorResult(
	        targetClass = FavouriteListMenuItem.class,
	        columns = {
	            @ColumnResult(name = "id", type=Long.class),
	            @ColumnResult(name = "name", type=String.class ),
	            @ColumnResult(name = "containstask", type=Boolean.class),
	            @ColumnResult(name = "dumplist", type=Boolean.class ),
	            @ColumnResult(name = "hidden", type=Boolean.class )
	        }
	    )
	)
@Entity
public class FavouriteListMenuItem {

	@Id
	private long tmp_id;
	
	private long id;
	private String name;
	private boolean containsTask;
	private boolean dumpList;
	private boolean hidden;
	
	public FavouriteListMenuItem() {
		
	}
	
	public FavouriteListMenuItem(long id, String name, boolean containsTask, boolean dumpList, boolean hidden) {
		this.id = id;
		this.name = name;
		this.containsTask = containsTask;
		this.dumpList = dumpList;
		this.hidden = hidden;
	}

	public long getTmp_id() {
		return this.tmp_id;
	}

	public long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public boolean isContainsTask() {
		return this.containsTask;
	}

	public boolean isDumpList() {
		return this.dumpList;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public void setTmp_id(long tmp_id) {
		this.tmp_id = tmp_id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContainsTask(boolean containsTask) {
		this.containsTask = containsTask;
	}

	public void setDumpList(boolean dumpList) {
		this.dumpList = dumpList;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
}
