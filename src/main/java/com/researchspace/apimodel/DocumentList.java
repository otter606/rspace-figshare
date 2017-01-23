package com.researchspace.apimodel;

import java.util.List;

import lombok.Data;

@Data
public class DocumentList {

	private List<Document> documents;
	private int totalHits, pageNumber;
	private List<Link> _links;
}
