package com.patho.main.service;

import com.patho.main.model.ListItem;
import com.patho.main.model.ListItem_;
import com.patho.main.model.interfaces.ListOrder;
import com.patho.main.repository.ListItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Service
@Transactional
public class ListItemService extends AbstractService {

	@Autowired
	private ListItemRepository listItemRepository;

	public ListItem addOrUpdate(ListItem listItem) {
		ListItem.StaticList type = listItem.getListType();
		if (listItem.getId() == 0) {

			long count = listItemRepository.count(new Specification<ListItem>() {
				@Override
				public Predicate toPredicate(Root<ListItem> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.equal(root.get(ListItem_.listType), type);
				}
			});

			System.out.println("lenght " + count);

			listItem.setIndexInList((int) count);

			logger.debug("Creating new ListItem " + listItem.getValue() + " for " + listItem.getListType().toString());

			listItem = listItemRepository.save(listItem, resourceBundle.get("log.settings.staticList.new",
					listItem.getValue(), listItem.getListType().toString()));

		} else {
			logger.debug("Updating ListItem " + listItem.getValue());
			listItem = listItemRepository.save(listItem, resourceBundle.get("log.settings.staticList.update",
					listItem.getValue(), listItem.getListType().toString()));
		}

		return listItem;
	}

	/**
	 * Tries to delete the old listItem
	 * 
	 * @param p
	 */
	@Transactional(propagation = Propagation.NEVER)
	public boolean deleteOrArchive(ListItem listItem) {
		try {
			listItemRepository.delete(listItem,
					resourceBundle.get("log.settings.staticList.deleted", listItem.getValue(), listItem.getListType()));
			return true;
		} catch (Exception e) {
			archive(listItem, true);
			return false;
		}
	}

	/**
	 * Archived or dearchives a ListItem
	 * 
	 * @param p
	 * @param archive
	 * @return
	 */
	public ListItem archive(ListItem listItem, boolean archive) {
		listItem.setArchived(archive);
		return listItemRepository.save(listItem,
				resourceBundle.get(archive ? "log.settings.staticList.archived" : "log.settings.staticList.dearchived",
						listItem.getValue(), listItem.getListType()));
	}

	public List<ListItem> updateReoderedList(List<ListItem> staticListContent, ListItem.StaticList type) {
		ListOrder.reOrderList(staticListContent);
		return listItemRepository.saveAll(staticListContent,
				resourceBundle.get("log.settings.staticList.list.reoder", type));
	}
}
