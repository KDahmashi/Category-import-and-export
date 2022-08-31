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
@XmlRootElement(name = "vocabulary")
// If you want you can define the order in which the fields are written
// Optional
@XmlType(propOrder = { "title", "description", "category"})
public class Vocabulary {

	private String title;
	private String description;
	private List<Category> category;
	private int totalChildCount;

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
	public int getTotalChildCount() {
		return totalChildCount;
	}

	public void setTotalChildCount(int totalChildCount) {
		this.totalChildCount = totalChildCount;
	}

}