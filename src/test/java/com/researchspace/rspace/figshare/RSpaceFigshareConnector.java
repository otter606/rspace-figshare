package com.researchspace.rspace.figshare;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.researchspace.figshare.impl.FigshareTemplate;
import com.researchspace.figshare.model.Account;

public class RSpaceFigshareConnector  {
	
	String figshareToken = "e21a4264ee6f69061563b44bd13cffc69db41f19e5f6b4a02aa46f9831dd3ee587d392ffd0b8d6c83a1e85f976d098620c15264f820da59060b7b5d206739659";
    String rspaceToken = "nAkK4Ce71LhNhNOpkksMn7rwuEIvSect";
    String rspaceUrl = "https://demo.researchspace.com/api/v1";
    RestTemplate template ;
	@Test
	public void test() {
		template = new RestTemplate();
//		FigshareTemplate figshare = new FigshareTemplate(figshareToken);
//		figshare.setPersonalToken(figshareToken);
//		Account acc = figshare.account();
//		System.out.println(acc.getEmail());
		getDocuments();
	}
	
	void getDocuments() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", rspaceToken);
		String url = rspaceUrl + "/documents";
		HttpEntity<String> ent = new HttpEntity<>(headers);
		ResponseEntity<String > resp =template.exchange(url,HttpMethod.GET,ent,String.class);

	    System.err.println(resp.getBody());
	}

}
