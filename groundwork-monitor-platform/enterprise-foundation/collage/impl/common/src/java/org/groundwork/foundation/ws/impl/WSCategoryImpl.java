package org.groundwork.foundation.ws.impl;

import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import com.groundwork.collage.model.impl.EntityType;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.ws.api.WSCategory;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WSCategoryImpl extends WebServiceImpl implements WSCategory {

	public WSFoundationCollection getCategories(Filter filter, int startRange,
			int endRange, SortCriteria orderBy, boolean retrieveChildren,
			boolean namePropertyOnly) throws WSFoundationException,
			RemoteException {
		FilterCriteria filterCriteria = getConverter().convert(filter);
		FoundationQueryList list = this.getCategoryService().getCategories(
				filterCriteria, null, -1, -1);
		return new WSFoundationCollection(list.getTotalCount(), getConverter()
				.convertCategory((Collection<Category>) list.getResults()));
	}

	public WSFoundationCollection getCategoriesByEntityType(String entityName,
			int startRange, int endRange, SortCriteria orderBy,
			boolean retrieveChildren, boolean namePropertyOnly)
			throws WSFoundationException, RemoteException {
		if (entityName== null)
			throw new WSFoundationException(
					"Invalid Parameter for getCategoriesByEntityType",
					ExceptionType.WEBSERVICE);
        Collection<Category> categories = this.getCategoryService().getCategoriesByEntityType(entityName);
		return new WSFoundationCollection(categories.size(), getConverter()
				.convertCategory(categories));
	}

	public WSFoundationCollection getCategoryById(int categoryId)
			throws WSFoundationException, RemoteException {
		if (categoryId > 0) {
			List<Category> categoryList = new ArrayList();
			categoryList.add(this.getCategoryService().getCategoryById(
					categoryId));
			// List contains only one category so prepare FoundationQueryList
			// for size 1.
			FoundationQueryList list = new FoundationQueryList(categoryList, 1);
			return new WSFoundationCollection(list.getTotalCount(),
					getConverter().convertCategory(
							(Collection<Category>) list.getResults()));
		} else {
			throw new WSFoundationException(
					"Invalid Parameter for getCategoryById",
					ExceptionType.WEBSERVICE);
		}
	}

	public WSFoundationCollection getCategoryByName(String categoryName,
			String entityName) throws WSFoundationException, RemoteException {
		if(categoryName == null || entityName==null)
			throw new WSFoundationException(
					"Invalid Parameter for getCategoryByName",
					ExceptionType.WEBSERVICE);	
		List<Category> categoryList = new ArrayList();
		categoryList.add(this.getCategoryService().getCategoryByName(categoryName, entityName));
		FoundationQueryList list = new FoundationQueryList(categoryList, 1);
		return new WSFoundationCollection(list.getTotalCount(), getConverter()
				.convertCategory((Collection<Category>) list.getResults()));
	}

	public WSFoundationCollection getCategoryEntities(String categoryName,
			String entityName, int startRange, int endRange,
			SortCriteria orderBy, boolean retrieveChildren,
			boolean namePropertyOnly) throws WSFoundationException,
			RemoteException {
		if(categoryName == null || entityName==null)
			throw new WSFoundationException(
					"Invalid Parameter for getCategoryByName",
					ExceptionType.WEBSERVICE);	
		Category category = this.getCategoryService().getCategoryByName(categoryName, entityName);
		Collection<CategoryEntity> col = category.getCategoryEntities();
		return new WSFoundationCollection(col.size(), getConverter()
				.convertCategoryEntity(col));
	}

	public WSFoundationCollection getRootCategories(String entityName,
			int startRange, int endRange, SortCriteria orderBy,
			boolean retrieveChildren, boolean namePropertyOnly)
			throws WSFoundationException, RemoteException {
		if(entityName==null)
			throw new WSFoundationException(
					"Invalid Parameter for getRootCategories",
					ExceptionType.WEBSERVICE);	
		Collection<Category> col = this.getCategoryService().getRootCategories(entityName);
		return new WSFoundationCollection(col.size(), getConverter()
				.convertCategory(col));
	}

}
