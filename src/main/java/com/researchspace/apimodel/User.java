package com.researchspace.apimodel;

import lombok.Data;

@Data
public class User {
 private Long id;
 private String email, firstName, lastName, username;
}
