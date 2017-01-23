package com.researchspace.apimodel;

import java.util.List;

import lombok.Data;

@Data
public class File {
 private Long id;
 private String globalId, name;
 private long size;
 private String contentType;
 private List<Link> _links;

}
