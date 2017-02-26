
Welcome to this project, which shows some example code  as how data can be retrieved from RSpace and added to Figshare programmatically.

### What it does:

The class `com.researchspace.rspace.figshare.RSpaceFigshareConnector` contains code to:

* Search over a page of RSpace documents, finding ones that have some attached files
* Download files to our machine
* Create an article in Figshare, uploading the files, and the content of our RSpace document as a FileSet.



### Setup

If you want to run this code yourself, you'll require an account on Figshare and RSpace, and to have
 created personal API tokens for the 2 sites.
 
Next, create a file called `test-hidden.properties` in `src/test/resources` and add the following properties as key value pairs:

    rspace.url=https://my.researchspace.com/api/v1
    rspaceToken=<myrspacetoken>
    figshareToken=<myfigsharetoken>

If you're using our Community site, then `rspace.url` doesn't need to set - it's the default value.

### Running 

    ./gradlew clean test
    
will invoke the tests. If you have at least one recently modified document in RSpace with an attachment, you should see that a new private Dataset is created in your Figshare account.

Here is an example of the sort of output you should  see from the program:

    com.researchspace.rspace.figshare.RSpaceFigshareConnector > test STANDARD_ERROR
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - Retrieving doc 4245
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - Field Objective in document ExampleWithAttachments has 1 files
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - Field Method in document ExampleWithAttachments has 3 files
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - Uploading metadata file ExampleWithAttachments-data7771839323598197153.html 
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - downloading  file RSpaceoperationalrequirements.docx  from RSpace
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - Retrieving file 4246
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - uploading to Figshare
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - downloading  file export.PNG  from RSpace
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - Retrieving file 2269
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - uploading to Figshare
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - downloading  file word5024436821782801992.001.jpeg  from RSpace
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - Retrieving file 3319
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - uploading to Figshare
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - downloading  file AnyImage.jpg  from RSpace
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - Retrieving file 3927
    [Test worker] INFO com.researchspace.rspace.figshare.RSpaceFigshareConnector - uploading to Figshare

    BUILD SUCCESSFUL
    

If you prefer, you can add the  properties to the command line, or your IDE launch configuration:

    ./gradlew clean test -Drspace.url=https://my.researchspace.com/api/v1 \
    -DrspaceToken=<myrspacetoken> \
    -DfigshareToken=<myfigsharetoken>

**Please be aware that this example code may fail if you have enormous files that exhaust your Java heap **

## Adapting the program

This program is not very selective about what it uploads - you could make this more useful by:

* Performing a search to retrieve a subset of documents: perhaps a tag 
       such as a grant number or project Id?
* Including all documents, not just those with attachments.
* If you have structured data in your RSpace documents, with well defined fields, then you could upload the RSpace documents to Figshare in CSV format, facilitating future import into Spreadsheets or databases.

## Future work

RSpace web application already has an integration with Figshare. Currently using this example code, files must be downloaded to your machine, then uploaded into Figshare, which is a little unwieldy and timeconsuming for many files.

 If there is sufficient interest, we at ResearchSpace could consider making Figshare deposit directly accessible from our API.
    
