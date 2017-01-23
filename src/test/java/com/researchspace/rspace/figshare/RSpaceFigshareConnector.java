package com.researchspace.rspace.figshare;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.web.client.RestTemplate;

import com.researchspace.apimodel.Document;
import com.researchspace.apimodel.DocumentList;
import com.researchspace.apimodel.Field;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = RSpaceFigshareConnector.class)
@Slf4j
// @Configuration
public class RSpaceFigshareConnector extends AbstractJUnit4SpringContextTests {

	@Autowired
	Environment env;

	String figshareToken = "";
	String rspaceToken = "";
	String rspaceUrl = "";
	RestTemplate template;

	@Test
	public void test() throws InterruptedException {
		template = new RestTemplate();
		// these properties can be defined on command line,
		// in gradle.properties as systemProp.properties or in test.properties
		// file.
		this.rspaceUrl = env.getProperty("rspace.url");
		this.rspaceToken = env.getProperty("rspaceToken");
		this.figshareToken = env.getProperty("figshareToken");
		// FigshareTemplate figshare = new FigshareTemplate(figshareToken);
		// figshare.setPersonalToken(figshareToken);
		// Account acc = figshare.account();
		// System.out.println(acc.getEmail());
		getDocuments();
	}

	void getDocuments() throws InterruptedException {
		HttpHeaders headers = createHeadersWithAPiKey();
		String url = rspaceUrl + "/documents?pageSize=5";
		HttpEntity<String> ent = new HttpEntity<>(headers);
		ResponseEntity<DocumentList> resp = template.exchange(url, HttpMethod.GET, ent, DocumentList.class);

		log.debug(resp.getBody().toString());

		for (Document doc : resp.getBody().getDocuments()) {
			Long id = doc.getId();
			Thread.sleep(1000);

			Document fullDoc = null;
			try {
				fullDoc = getDocument(id);
			} catch (Exception e) {
				log.warn("Error retrieving doc " + id);
				log.warn(e.getMessage());
				continue;
			}

			for (Field f : fullDoc.getFields()) {
				if (f.getFiles().size() > 0) {
					log.info(String.format("Field %s in document %s has %d files", f.getName(), doc.getName(),
							f.getFiles().size()));
				}
			}
		}
	}

	private HttpHeaders createHeadersWithAPiKey() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", rspaceToken);
		return headers;
	}

	private Document getDocument(Long id) {
		log.info("Retrieving doc " + id);
		String url = rspaceUrl + "/documents/" + id;
		HttpEntity<String> ent = new HttpEntity<>(createHeadersWithAPiKey());
		ResponseEntity<Document> resp = template.exchange(url, HttpMethod.GET, ent, Document.class);
		return resp.getBody();
	}

}
