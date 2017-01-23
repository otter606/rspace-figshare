package com.researchspace.apimodel;

import lombok.Data;

@Data
public class Form {
 private Long id;
 
 private String name, globalId, stableId;
 private int version;
 
}
