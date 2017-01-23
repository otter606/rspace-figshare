package com.researchspace.apimodel;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class Document {

	private Long id;
	private User owner;
	private Date created, lastModified;
	private boolean signed;
	private String globalId, tags, name;
	private Form form;
	private List<Field> fields;
	private List<Link> _links;
}
