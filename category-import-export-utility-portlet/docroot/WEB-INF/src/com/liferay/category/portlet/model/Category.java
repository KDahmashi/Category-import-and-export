package com.liferay.category.portlet.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
/***
 * 
 * @author ketan.savaliya
 *
 */
@XmlRootElement(name = "category")
@XmlType(propOrder = { "title", "description", "category"})
public class Category {

	private String title;
	private String description;
	private List<Category> category;
	private int childCategoryCount;
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Category> getCategory() {
		return category;
	}

	public void setCategory(List<Category> category) {
		this.category = category;
	}
	
	@XmlAttribute
	public int getChildCategoryCount() {
		return childCategoryCount;
	}

	public void setChildCategoryCount(int childCategoryCount) {
		this.childCategoryCount = childCategoryCount;
	}
	
}
