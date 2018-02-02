package com.ontotext.ehri.services;

import com.ontotext.ehri.mail.SendMail;
import com.ontotext.ehri.model.FileMetaModel;
import com.ontotext.ehri.model.ProviderConfigModel;
import com.ontotext.ehri.tools.Configuration;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by Boyan on 10-Aug-17.
 */
@Service
public class ProcessUpdateService {

    @Autowired
    private ConfigurationService configurationService;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProcessUpdateService.class);
    private String rsFileLocation = Configuration.getString("rs-ead-input-dir");
    private String PMHFileLocation = Configuration.getString("pmh-ead-input-dir");

    private String inputValidatinFolder = Configuration.getString("initial-validation-folder");
    private final String USER_AGENT = "Mozilla/5.0";


    public Date prepareForValidation(Map<String, List<FileMetaModel>> files) {
        Date now = new Date();
        File file = new File(inputValidatinFolder, Configuration.DATE_FORMAT.format(now));
        file.mkdir();

        for (Map.Entry<String, List<FileMetaModel>> entry : files.entrySet()) {
            File provider = new File(file.getAbsolutePath(), entry.getKey());
            if (!provider.exists()) provider.mkdir();
            List<FileMetaModel> metaData = entry.getValue();
            for (FileMetaModel model : metaData) {
                try {
                    copyFiles(new File(model.getFilePath()), new File(provider + File.separator + "ead", model.getFileName()));
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }
        }

        return now;
    }

    public Map<String, List<FileMetaModel>> checkForChanges() throws IOException, URISyntaxException {

        Map<String, List<FileMetaModel>> syncMetaData = configurationService.loadSavedConfig();

        Map<String, List<FileMetaModel>> rsChanges = checkChanges(rsFileLocation, syncMetaData);
        Map<String, List<FileMetaModel>> PMHChanges = checkChanges(PMHFileLocation, syncMetaData);
        Map<String, List<FileMetaModel>> changes = deepMerge(rsChanges, PMHChanges);
        configurationService.saveMetaData(changes);

        return changes;
    }

    public Map deepMerge(Map original, Map newMap) {
        for (Object key : newMap.keySet()) {
            if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
                Map originalChild = (Map) original.get(key);
                Map newChild = (Map) newMap.get(key);
                original.put(key, deepMerge(originalChild, newChild));
            } else {
                original.put(key, newMap.get(key));
            }
        }
        return original;
    }

    private Map<String, List<FileMetaModel>> checkChanges(String fileLocation, Map<String, List<FileMetaModel>> syncMetaData) throws URISyntaxException {
        Map<String, List<FileMetaModel>> changes;
        if (syncMetaData != null && !syncMetaData.isEmpty()){
            Map<String, List<FileMetaModel>> metaDataCollectionNew = initiateFileStatus(fileLocation);
            changes = detectChanges(syncMetaData, metaDataCollectionNew);
        }
        else {
            changes = initiateFileStatus(fileLocation);

        }
        return changes;
    }

    private Map<String, List<FileMetaModel>> detectChanges(Map<String, List<FileMetaModel>> metaDataCollection, Map<String, List<FileMetaModel>> metaDataCollectionNew) {
        Set<String> providers = metaDataCollectionNew.keySet();
        Map<String, List<FileMetaModel>> changeMap = new HashMap<>();
        for (String provider : providers) {
            List<FileMetaModel> metaCollection = metaDataCollection.get(provider);
            List<FileMetaModel> metaCollectionNew = metaDataCollectionNew.get(provider);
            List<FileMetaModel> changes = compareMetaDataLists(metaCollection, metaCollectionNew);
            changeMap.put(provider, changes);
        }

        return changeMap;
    }

    private List<FileMetaModel> compareMetaDataLists(List<FileMetaModel> metaCollection, List<FileMetaModel> metaCollectionNew) {
        List<FileMetaModel> changes = new ArrayList<>();
        for (FileMetaModel model : metaCollection) {
            String fileName = model.getFileName();
            for (FileMetaModel modelNew : metaCollectionNew) {
                if (fileName.equals(modelNew.getFileName())) {
                    if (model.getLastModified() > modelNew.getLastModified()) {
                        changes.add(modelNew);
                    }

                    metaCollectionNew.remove(modelNew);
                    break;
                }
            }
        }

        if (metaCollectionNew.size() > 0) {
            for (FileMetaModel model : metaCollectionNew) {
                changes.add(model);
            }
        }
        return changes;
    }

    private Map<String, List<FileMetaModel>> initiateFileStatus(String location) {
        Map<String, List<FileMetaModel>> metaDataCollection = new HashMap<>();
        File file = new File(location);
        if (file.exists()) {
            File[] providers = file.listFiles();
//            providers = listAllFiles(Arrays.asList(file.listFiles()));
            if (providers != null) {
                for (File provider : providers) {
                    List<File> eadFiles = readEADFiles(provider);
                    List<FileMetaModel> metaData = collectMetaData(eadFiles);
                    metaDataCollection.put(provider.getName(), metaData);
                }
            }
        }

        return metaDataCollection;
    }

    private ArrayList<File> listAllFiles(List<File> locations) {
       ArrayList<File> result = new ArrayList();
        for (File location : locations) {
            if (location.exists() && location.isDirectory()) {
                result.addAll(Arrays.asList(location.listFiles()));
                result.remove(location);
            }
            else {
                result.add(location);
            }
        }

        if (checkLocationForDirectories(result)) {
            result = listAllFiles(result);
        }

        return result;
    }

    private boolean checkLocationForDirectories(ArrayList<File> results) {
        for (File result : results) {
            if (result.exists() && result.isDirectory()) return true;
        }
        return false;
    }

    private List<FileMetaModel> collectMetaData(List<File> fileCollection) {
        List<FileMetaModel> metaDataCollection = new ArrayList<>();
        if (fileCollection != null && fileCollection.size() > 0) {
            for (File file : fileCollection) {
                FileMetaModel metaModel = new FileMetaModel();
                metaModel.setFileName(file.getName());
                metaModel.setLastModified(file.lastModified());
                metaModel.setFileSize(file.length());
                metaModel.setFilePath(file.getAbsolutePath());

                metaDataCollection.add(metaModel);
            }
        }

        return metaDataCollection;
    }

    private List<File> readEADFiles(File providerDir) {
        File contentDir = null;
        if (providerDir.exists() && providerDir.isDirectory()) {
            contentDir = new File(providerDir.getAbsolutePath() + "/" + "/metadata/__SOR__/ead");
            if (!contentDir.exists()) {
                contentDir = new File(providerDir.getAbsolutePath());
            }
        }

        List<File> eadFiles = null;
        if (contentDir != null && contentDir.isDirectory()) {
            eadFiles = (List<File>) FileUtils.listFiles(contentDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        }

        return eadFiles;
    }



    /** Copy Files for validation */
    public void copyFiles(File source, File destination) throws IOException {
        FileUtils.copyFile(source, destination);
    }

    public void fixInputValidationFolder(Map<String, File[]> filesPerProvider) {
        for (Map.Entry<String, File[]> entry : filesPerProvider.entrySet()) {
            File[] eadFiles = entry.getValue();
            for (File ead : eadFiles) {
                if (ead.isFile()) {
                    removeDocTypeFromFile(ead);
                }
            }
        }
    }

    public Map<String, File[]> listValidationFolderFiles(Date now) {
        File inputFolder = new File(inputValidatinFolder + File.separator + Configuration.DATE_FORMAT.format(now));
        Map<String, File[]> filesPerProvider = new HashMap<>();
        if (inputFolder.exists()) {
            File[] providers = inputFolder.listFiles();
            for (File file : providers) {
                File eadFileDir = new File(file.getAbsolutePath() + File.separator + "ead");
                if (eadFileDir.exists()) {
                    File[] eadFiles = eadFileDir.listFiles();
                    filesPerProvider.put(file.getName(), eadFiles);
                }
            }
        }
        return filesPerProvider;
    }

    public void removeDocTypeFromFile(File file) {
        try {
            List<String> lines = FileUtils.readLines(file, "UTF-8");
            List<String> fixedFile = new ArrayList<>();
            if (lines.size() == 0){
                file.delete();
                LOGGER.warn("File " + file.getName() + " is deleted because is empty!");
                return;
            }
            for (String line : lines) {
                if (line.contains("<!DOCTYPE") || line.contains("ead.dtd")) continue;
                else fixedFile.add(line);
            }

            FileUtils.writeLines(file, "UTF-8", fixedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, File> compressFileCollection(Map<String, File[]> eadFileCollections, Date now) throws FileNotFoundException {
        Map<String, File> compressedCollection = new HashMap<>();
        for (Map.Entry<String, File[]> entry : eadFileCollections.entrySet()) {
            File archiveFile = compress(entry.getValue(), entry.getKey(), now);
            compressedCollection.put(entry.getKey(), archiveFile);
        }
        return compressedCollection;
    }

    public File compress(File[] files, String collection, Date now) throws FileNotFoundException {
        TarArchiveEntry t = new TarArchiveEntry(new File(""));
        List<TarArchiveEntry> tarArchiveEntries = new ArrayList<>();
        File archiveDir = new File(Configuration.getString("pre-ingest-dir") + File.separator + Configuration.DATE_FORMAT.format(now) + File.separator + collection + File.separator + "compressed");
        if (!archiveDir.exists()) archiveDir.mkdir();
        File compressedCollection = new File(archiveDir.getPath() + File.separator + collection + ".tar");
        TarArchiveOutputStream s = new TarArchiveOutputStream(new FileOutputStream(compressedCollection));
        s.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        try {
            for (File file : files) {
                TarArchiveEntry entry = new TarArchiveEntry(file.getName());
                byte[] content = Files.readAllBytes(file.toPath());
                entry.setSize(content.length);
                s.putArchiveEntry(entry);
                s.write(content);
                s.closeArchiveEntry();
            }
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressedCollection;
    }

    public void processIngest(Map<String, ProviderConfigModel> providerConfig, Map<String, Boolean> validaitonResults){

        for (Map.Entry<String, ProviderConfigModel> entry : providerConfig.entrySet()) {
            if (!validaitonResults.get(entry.getValue().getEadFolderName())) {
                String url =  entry.getValue().getRepository();


                String urlParameters = "scope=" + entry.getValue().getRepositoryName() + "&log=" + entry.getValue().getLog() + "&properties=" + entry.getValue().getIngestPropertyFile() + "&commit=false";
                URL obj = null;
                try {
                    obj = new URL(url + "?" + urlParameters);

                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    //add reuqest header
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-type", "application/octet-stream");
                    con.setRequestProperty("X-User", "user000958");

                    // Send post request
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.write(readAndClose(new FileInputStream(new File(entry.getValue().getEadFileLocation()))));
                    wr.flush();
                    wr.close();

                    int responseCode = con.getResponseCode();
                    System.out.println("\nSending 'POST' request to URL : " + url);
                    System.out.println("Post parameters : " + urlParameters);
                    System.out.println("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //print result
                    System.out.println(response.toString());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void reportDatasetsWithErrors(Map<String, Boolean> validaitonResults) {
        String failedDatasets = "";
        for (Map.Entry<String, Boolean> entry : validaitonResults.entrySet()) {
            if (!entry.getValue()) {
                if (!failedDatasets.isEmpty()) failedDatasets += ", " + entry.getKey();
            }
            else {
                failedDatasets = entry.getKey();
            }
        }
        new SendMail().send(failedDatasets);
    }

    byte[] readAndClose(InputStream aInput){
        //carries the data from input to output :
        byte[] bucket = new byte[32*1024];
        ByteArrayOutputStream result = null;
        try  {
            try {
                //Use buffering? No. Buffering avoids costly access to disk or network;
                //buffering to an in-memory stream makes no sense.
                result = new ByteArrayOutputStream(bucket.length);
                int bytesRead = 0;
                while(bytesRead != -1){
                    //aInput.read() returns -1, 0, or more :
                    bytesRead = aInput.read(bucket);
                    if(bytesRead > 0){
                        result.write(bucket, 0, bytesRead);
                    }
                }
            }
            finally {
                aInput.close();
                //result.close(); this is a no-operation for ByteArrayOutputStream
            }
        }
        catch (IOException ex){
            System.out.println(ex.getMessage());
        }
        return result.toByteArray();
    }

    public void addEADFileLocation(Map<String, ProviderConfigModel> providerConfig, Map<String, File> compressedCollections) {
        Set<String> compressedCollectionsKeys = compressedCollections.keySet();
        Set<String> providerConf = providerConfig.keySet();
        for (String collection : compressedCollectionsKeys) {
            for(String providerC : providerConf) {
                if (StringUtils.containsIgnoreCase(collection, providerC, Locale.US)) {
                    providerConfig.get(providerC).setEadFileLocation(compressedCollections.get(collection).getAbsolutePath());
                    providerConfig.get(providerC).setEadFolderName(collection);
                }
            }
        }
    }
}
