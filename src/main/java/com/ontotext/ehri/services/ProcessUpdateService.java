package com.ontotext.ehri.services;

import com.ontotext.ehri.mail.SendMail;
import com.ontotext.ehri.model.FileMetaModel;
import com.ontotext.ehri.model.ProviderConfigModel;
import com.ontotext.ehri.tools.Configuration;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

    @Autowired
    private ResourceService resourceService;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProcessUpdateService.class);
    private String rsFileLocation = Configuration.getString("rs-ead-input-dir");
    private String PMHFileLocation = Configuration.getString("pmh-ead-input-dir");

    private String inputValidatinFolder = Configuration.getString("initial-validation-folder");
    private final String USER_AGENT = "Mozilla/5.0";


    public Date prepareForValidation(Map<String, List<FileMetaModel>> files, Map<String, ProviderConfigModel> chiIngestConfig) {
        Date now = new Date();
        File file = new File(inputValidatinFolder, Configuration.DATE_FORMAT.format(now));
        file.mkdir();

        for (Map.Entry<String, List<FileMetaModel>> entry : files.entrySet()) {
//            String pr[] = entry.getKey().split("/");
//            String prName = pr[pr.length - 1];

            String prName = getCHIName(entry.getKey(), chiIngestConfig);

            File provider = new File(file.getAbsolutePath(), prName);
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

    public String getCHIName(String fileLocation, Map<String, ProviderConfigModel> chiIngestConfig) {
        String name = "";
        for (Map.Entry<String, ProviderConfigModel> entry : chiIngestConfig.entrySet()) {
            if (entry.getValue().getEadFileLocation().equals(fileLocation)) {
                name = entry.getValue().getName();
            }
        }

        return name;
    }

    public Map<String, List<FileMetaModel>> checkForChanges(Map<String, ProviderConfigModel> chiIngestConfig) throws IOException, URISyntaxException {

        Map<String, List<FileMetaModel>> syncMetaData = configurationService.loadSavedConfig();
        Map<String, List<FileMetaModel>> newChangesMap = new HashMap<>();

        for (Map.Entry<String, ProviderConfigModel> entry : chiIngestConfig.entrySet()) {
            Map<String, List<FileMetaModel>> providerChanges = checkChanges(entry.getValue().getEadFileLocation(), syncMetaData);
            deepMerge(newChangesMap, providerChanges);
        }

//        Map<String, List<FileMetaModel>> rsChanges = checkChanges(rsFileLocation, syncMetaData);
//        Map<String, List<FileMetaModel>> PMHChanges = checkChanges(PMHFileLocation, syncMetaData);
//        Map<String, List<FileMetaModel>> changes = deepMerge(rsChanges, PMHChanges);
        configurationService.saveMetaData(newChangesMap);

        return newChangesMap;
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
        List<File> eadFiles = new ArrayList<>();
        File file = new File(location);
        List<FileMetaModel> metaData;
        if (file.exists()) {
            File[] providers = file.listFiles();
//            providers = listAllFiles(Arrays.asList(file.listFiles()));
            if (providers != null) {
                for (File provider : providers) {
                    if (provider.isFile()) {
                        eadFiles.add(provider);
                    }
                    else if (provider.isDirectory()) {
                        List<File> files = readEADFiles(provider);
                        eadFiles.addAll(files);
                    }

                }
                metaData = collectMetaData(eadFiles);
                metaDataCollection.put(location, metaData);
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
                if (file.getAbsolutePath().contains("config")) continue;
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

        List<File> eadFiles = new ArrayList<>();
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
                    File[] eadFiles = eadFileDir.listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".xml");
                        }
                    });
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

    public static void decompress(String in, String out)  {
        try (TarArchiveInputStream fin = new TarArchiveInputStream(new FileInputStream(in))){
            TarArchiveEntry entry;
            while ((entry = fin.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File curfile = new File(out + entry.getName());
                File parent = curfile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                IOUtils.copy(fin, new FileOutputStream(curfile));
            }
        } catch (IOException e){e.printStackTrace();}
    }

    public void processIngest(Map<String, ProviderConfigModel> providerConfig, Map<String, ValidationResultModel> validaitonResults){

        for (Map.Entry<String, ProviderConfigModel> entry : providerConfig.entrySet()) {
            if ((entry.getValue().getEadFolderName() != null && validaitonResults.get(entry.getValue().getEadFolderName()) != null
                    && !validaitonResults.get(entry.getValue().getEadFolderName())) || Boolean.parseBoolean(entry.getValue().getTolerant())) {
                String url = entry.getValue().getRepository();
            String eadFolderName = entry.getValue().getEadFolderName();
            if (validaitonResults.containsKey(eadFolderName) && !validaitonResults.get(eadFolderName).getValid()) {
                String url =  entry.getValue().getRepository();


                String urlParameters = "scope=" + entry.getValue().getRepositoryName() + "&log=" + entry.getValue().getLog() +
                        "&properties=" + entry.getValue().getIngestPropertyFile() + "&commit=true" + "&allow-update=" + entry.getValue().getAllowUpdate() +
                        "&tolerant=" + entry.getValue().getTolerant();
                LOGGER.info("Ingesting: " + urlParameters);
                URL obj = null;
                try {
                    obj = new URL(url + "?" + urlParameters);

                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    //add reuqest header
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-type", "application/octet-stream");
                    con.setRequestProperty("X-User", "user000958");
                    con.setRequestProperty("Content-Length", String.valueOf(new File(entry.getValue().getEadFileLocation()).length()));

                    // Send post request
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    LOGGER.info("File location: " + entry.getValue().getEadFileLocation());
                    wr.write(readAndClose(new FileInputStream(new File(entry.getValue().getEadFileLocation()))));
                    wr.flush();
                    wr.close();

                    int responseCode = con.getResponseCode();

                    LOGGER.info("\nSending 'POST' request to URL : " + url);
                    LOGGER.info("Post parameters : " + urlParameters);
                    LOGGER.info("Response Code : " + responseCode);
                    LOGGER.info("Responce Message : " + con.getResponseMessage());

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    validaitonResults.get(eadFolderName).setHttpResponse(response.toString());

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

    public void reportDatasetsWithErrors(Map<String, ValidationResultModel> validaitonResults) {
        String failedDatasets = "";
        for (Map.Entry<String, ValidationResultModel> entry : validaitonResults.entrySet()) {
            if (!entry.getValue().getValid()) {
                failedDatasets += " " + entry.getKey() + " - Successfully ingested! \n";
            }
            else {
                failedDatasets += " " + entry.getKey() + " - Ingestion error! Please check the validation report!\n";
            }
            failedDatasets += " " + entry.getKey() + " - HTTP response: " + entry.getValue().getHttpResponse();
            failedDatasets += " " + entry.getKey() + " - validity: \n" + entry.getValue().getValidation() + "\n";
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

    public Map<String, File[]> pythonPreProcessing(Map<String, File[]> compressedCollections,
                                                   Map<String, ProviderConfigModel> chiIngestConfig){
        Map<String, File[]> preProcessed = compressedCollections;
        int i = 0;

        for (Map.Entry<String, ProviderConfigModel> conf : chiIngestConfig.entrySet()) {
            String preProcessing = conf.getValue().getPreProcessingScriptsDir();
            if (!compressedCollections.containsKey(conf.getKey()))
                continue;
            if (preProcessing == null) {
                preProcessed.put(conf.getKey(), compressedCollections.get(conf.getKey()));
            } else {
                for (String script : ResourceService.listDirContentsPaths(
                        conf.getValue().getPreProcessingScriptsDir())) {
                    preProcessed = pythonPreProcessing(preProcessed, script, i + "", conf.getKey());
                    i++;
                }
            }
        }
        //
        Set<String> missingInConfig = compressedCollections.keySet();
        missingInConfig.removeAll(chiIngestConfig.keySet());
        for(String missingKey : missingInConfig){
            preProcessed.put(missingKey, compressedCollections.get(missingKey));
        }
        return preProcessed;
    }

    public Map<String, File[]> pythonPreProcessing(Map<String, File[]> compressedCollections, String script,
                                                   String suffix, String key){
        Map<String, File[]> preProcessed = new HashMap<>();

        for (Map.Entry<String, File[]> files : compressedCollections.entrySet()) {
            List<File> newFiles = new ArrayList<>();
            for (File file: files.getValue()) {
                String newPath = file.getParentFile().getParentFile().getAbsolutePath()
                        + File.separator + "pre-processing-" + suffix + File.separator + file.getName();


                if (files.getKey().contains(key))

                    try {
                        String command = "python3.5 " + script + " "
                                + file.getAbsolutePath() + " "
                                + newPath;
                        Process p = Runtime.getRuntime().exec(command);
                        p.waitFor();
                    } catch (IOException|InterruptedException e) {e.printStackTrace();}
                else {
                    try {
                        FileUtils.copyFile(file, new File(newPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                newFiles.add(new File(newPath));
            }

            preProcessed.put(files.getKey(), newFiles.toArray(new File[] {}));
        }
        return preProcessed;
    }
}
