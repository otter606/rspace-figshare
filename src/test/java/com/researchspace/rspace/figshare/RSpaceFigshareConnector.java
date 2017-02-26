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
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
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

@TestPropertySource(
locations ={ "classpath:/test.properties","classpath:/test-hidden.properties"})
@ContextConfiguration(classes = RSpaceFigshareConnector.class)
@Slf4j
public class RSpaceFigshareConnector extends AbstractJUnit4SpringContextTests {

    private @Autowired Environment env;

    private String figshareToken = "";
    private String rspaceToken = "";
    private String rspaceUrl = "";
    private RestTemplate template;
    private FigshareTemplate figshare;
    
    @Before
    public void setup () {
    	template = new RestTemplate();
        // these properties can be defined on command line,
        // in gradle.properties as systemProp.properties or in test-hidden.properties
        // file.
        this.rspaceUrl = env.getProperty("rspace.url");
        this.rspaceToken = env.getProperty("rspaceToken");
        this.figshareToken = env.getProperty("figshareToken");
        figshare = new FigshareTemplate(figshareToken);
        figshare.setPersonalToken(figshareToken);
    }

    @Test
    public void fromRSpaceToFigshare () throws InterruptedException, IOException {
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
            // don't spam with too many requests.
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
            for (Field field : fullDoc.getFields()) {
                upload = true;
                if (field.getFiles().size() > 0) {
                    log.info(String.format("Field %s in document %s has %d files", field.getName(), doc.getName(),
                            field.getFiles().size()));
                }
            }
            if (upload) {
                uploadToFigshare(fullDoc);
                break;
            }
        }
    }

    private void uploadToFigshare(Document doc) throws IOException {
        ArticlePost post = ArticlePost.builder()
            // when we create an article, author id can be null.
            .author(new Author(ownerFullname(doc), null))
            .title(doc.getName())
            .tags(tags(doc))
            // you can get Categories from Figshare API to set your own.
            .categories(Arrays.asList(new Integer[] { 21, 23 }))
            .description("test deposit fom RSpace API").build();
        Location created = figshare.createArticle(post);

        // concatenate data from all fields into a single HTML file.
        File metadataFile = concatenateFieldsToFile(doc);
        log.info("Uploading metadata file {} ", metadataFile.getName());
        figshare.uploadFile(created.getId(), metadataFile);

        // now iterate over Fields, downloadin any associated file to local
        // machine
        // then uploading into Figshare
        for (Field fd : doc.getFields()) {
            List<com.researchspace.apimodel.File> files = fd.getFiles();
            for (com.researchspace.apimodel.File file : files) {
                log.info("downloading  file {}  from RSpace", file.getName());
                File fromRSpace = downloadFile(file);
                log.info("uploading to Figshare");
                figshare.uploadFile(created.getId(), fromRSpace);
            }
        }
    }

    private File concatenateFieldsToFile(Document doc) throws IOException {
        String content = "";
        for (Field f : doc.getFields()) {
            content += f.getContent();
        }

        File metadataFile = createTempFile(doc.getName() + "-data", ".html");
        FileUtils.write(metadataFile, content, Charset.forName("UTF-8"));
        return metadataFile;
    }

    private File downloadFile(com.researchspace.apimodel.File file) throws IOException {
        log.info("Retrieving file " + file.getId());
        String url = rspaceUrl + "/files/" + file.getId() + "/file";
        HttpHeaders headers = createHeadersWithAPiKey();
        headers.setAccept(MediaType.parseMediaTypes(file.getContentType()));
        HttpEntity<String> ent = new HttpEntity<>(headers);
        // for production usage with large files you'd probably want to stream
        // the response
        // to avoid OOM errors.
        ResponseEntity<byte[]> resp = template.exchange(url, HttpMethod.GET, ent, byte[].class);
        File tempFile = createTempFile(getBaseName(file.getName()), getExtension(file.getName()));

        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(resp.getBody()), tempFile);
        return tempFile;

    }

    private List<String> tags(Document doc) {
        if (!StringUtils.isBlank(doc.getTags())) {
            return Arrays.asList(doc.getTags().split(","));
        } else {
            return Collections.emptyList();
        }
    }

    private String ownerFullname(Document doc) {
        return doc.getOwner().getLastName() + "," + doc.getOwner().getFirstName();
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
