package com.patho.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.patho.main.model.ListItem;
import com.patho.main.model.ListItem_;
import com.patho.main.model.Physician;
import com.patho.main.model.interfaces.ListOrder;
import com.patho.main.repository.ListItemRepository;

public class ListItemService extends AbstractService {

	@Autowired
	private ListItemRepository listItemRepository;

//	public Physician addOrUpdate(ListItem listItem) {
//		if (listItem.getId() == 0) {
//
//			listItemRepository.count(new Specification<ListItem> { };);
//			
//			logger.debug("Creating new ListItem " + listItem.getValue() + " for " + listItem.getListType().toString());
//
//			listItemRepository.save(listItem, resourceBundle.get("log.settings.staticList.new", listItem.getValue(),
//					listItem.getListType().toString()));
//
//			ListOrder.reOrderList(getStaticListContent());
//
//			listItemRepository.saveAll(getStaticListContent(),
//					resourceBundle.get("log.settings.staticList.list.reoder", getSelectedStaticList()));
//
//		} else {
//			logger.debug("Updating ListItem " + listItem.getValue());
//			// case edit: update an save
//
//			listItemRepository.save(listItem, resourceBundle.get("log.settings.staticList.update", listItem.getValue(),
//					getSelectedStaticList().toString()));
//		}
//	}

	public static Specification<ListItem> isLongTermCustomer() {
		    return new Specification<?> {
		    	 	
		      public Predicate toPredicate(Root<ListItem> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		        return cb.lessThan(root.get(ListItem_.archived), true);
		      }
		   
		    };
	}
}
