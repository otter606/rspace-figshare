package com.researchspace.apimodel;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class Field {
 private Long id;
 private String name, type, content;
 private List<Link> _links;
 private List<File> files;
 private Date lastModified;
}
