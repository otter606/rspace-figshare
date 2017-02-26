package com.researchspace.rspace.figshare;

import static java.io.File.createTempFile;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.web.client.RestTemplate;

import com.researchspace.apimodel.Document;
import com.researchspace.apimodel.DocumentList;
import com.researchspace.apimodel.Field;
import com.researchspace.figshare.impl.FigshareTemplate;
import com.researchspace.figshare.model.ArticlePost;
import com.researchspace.figshare.model.Author;
import com.researchspace.figshare.model.Location;

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
	 FigshareTemplate figshare;

	@Test
	public void test() throws InterruptedException, IOException {
		template = new RestTemplate();
		// these properties can be defined on command line,
		// in gradle.properties as systemProp.properties or in test.properties
		// file.
		this.rspaceUrl = env.getProperty("rspace.url");
		this.rspaceToken = env.getProperty("rspaceToken");
		this.figshareToken = env.getProperty("figshareToken");
		figshare = new FigshareTemplate(figshareToken);
		figshare.setPersonalToken(figshareToken);
		getDocuments();
	}

	void getDocuments() throws InterruptedException, IOException {
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
			boolean upload = false;
			for (Field f : fullDoc.getFields()) {
				upload = true;
				if (f.getFiles().size() > 0) {
					log.info(String.format("Field %s in document %s has %d files", f.getName(), doc.getName(),
							f.getFiles().size()));
				}
			}
			if (upload) {
				uploadToFigshare(fullDoc);
				break;
			}
		}
	}

	private void uploadToFigshare(Document doc) throws IOException {
		ArticlePost post = ArticlePost.builder().author(new Author(ownerFullname(doc), null))
		.title(doc.getName())
		.tags(tags(doc))
		.categories(Arrays.asList(new Integer[]{21,23}))
		.description("test deposit fom RSpace API").build();
		Location created = figshare.createArticle(post);
		
		String content = "";
		for (Field f: doc.getFields()) {
			content += f.getContent();
		}
		
		File metadataFile = File.createTempFile(doc.getName() + "-data", ".html");
		
		FileUtils.write(metadataFile, content, Charset.forName("UTF-8"));
		log.info("Uploading metadata file {} ", metadataFile.getName());
		figshare.uploadFile(created.getId(), metadataFile);
		
		for (Field fd: doc.getFields()) {
			List<com.researchspace.apimodel.File> files = fd.getFiles();
			for (com.researchspace.apimodel.File file: files) {
				log.info("downloading  file {}  from RSpace", file.getName());
				File fromRSpace = downloadFile(file);
				log.info("uploading to Figshare");
				figshare.uploadFile(created.getId(), fromRSpace);
			}
		}
	}

	private File downloadFile(com.researchspace.apimodel.File file) throws IOException {
		log.info("Retrieving file " + file.getId());
		String url = rspaceUrl + "/files/" + file.getId() + "/file";
		HttpHeaders headers = createHeadersWithAPiKey();
		headers.setAccept(MediaType.parseMediaTypes(file.getContentType()));
		HttpEntity<String> ent = new HttpEntity<>(headers);
		ResponseEntity<byte []> resp = template.exchange(url, HttpMethod.GET, ent, byte [].class);
		File tempFile = createTempFile(getBaseName(file.getName()), 
				getExtension(file.getName()));
 
		FileUtils.copyInputStreamToFile(new ByteArrayInputStream(resp.getBody()), tempFile);
		return tempFile;
		
	}

	private List<String> tags(Document doc) {
		if(!StringUtils.isBlank(doc.getTags())) {
			return Arrays.asList(doc.getTags().split(","));
		} else {
			return Collections.EMPTY_LIST;
		}
		
	}

	private String ownerFullname(Document doc) {
		return doc.getOwner().getUsername() + "," + doc.getOwner().getFirstName();
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
