package com.patho.main.model.user;

import com.patho.main.common.View;
import com.patho.main.common.WorklistSortOrder;
import com.patho.main.util.worklist.search.WorklistSimpleSearch.SimpleSearchOption;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(HistoSettings.class)
public abstract class HistoSettings_ {

	public static volatile SingularAttribute<HistoSettings, Boolean> alternatePatientAddMode;
	public static volatile SingularAttribute<HistoSettings, String> inputFieldColor;
	public static volatile SingularAttribute<HistoSettings, Boolean> autoSelectedPreferedLabelPrinter;
	public static volatile SingularAttribute<HistoSettings, SimpleSearchOption> worklistToLoad;
	public static volatile ListAttribute<HistoSettings, View> availableViews;
	public static volatile SingularAttribute<HistoSettings, Boolean> worklistAutoUpdate;
	public static volatile SingularAttribute<HistoSettings, Long> version;
	public static volatile SingularAttribute<HistoSettings, Boolean> worklistSortOrderAsc;
	public static volatile SingularAttribute<HistoSettings, Boolean> worklistHideNoneActiveTasks;
	public static volatile SingularAttribute<HistoSettings, View> startView;
	public static volatile SingularAttribute<HistoSettings, View> defaultView;
	public static volatile SingularAttribute<HistoSettings, Boolean> addTaskWithSingelClick;
	public static volatile ListAttribute<HistoSettings, SimpleSearchOption> availableWorklists;
	public static volatile SingularAttribute<HistoSettings, Boolean> autoSelectedPreferedPrinter;
	public static volatile SingularAttribute<HistoSettings, Long> id;
	public static volatile SingularAttribute<HistoSettings, Long> preferedPrinter;
	public static volatile SingularAttribute<HistoSettings, String> preferedLabelPritner;
	public static volatile SingularAttribute<HistoSettings, String> inputFieldFontColor;
	public static volatile SingularAttribute<HistoSettings, WorklistSortOrder> worklistSortOrder;

}

