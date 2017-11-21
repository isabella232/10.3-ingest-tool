package com.ontotext.ehri.services;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.ontotext.ehri.model.FileMetaModel;
import com.ontotext.ehri.model.ProviderConfigModel;
import com.thoughtworks.xstream.XStream;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Created by Boyan on 15-Aug-17.
 */
@Service
public class ConfigurationService {

    private static final Type REVIEW_TYPE = new TypeToken<Map<String,FileMetaModel>>() {}.getType();
    private static final Type CONFIG_TYPE = new TypeToken<Map<String,ProviderConfigModel>>() {}.getType();

    public Map<String, List<FileMetaModel>> loadSavedConfig(){
        XStream xStream = new XStream();
        xStream.alias("savedConfig", FileMetaModel.class);
        File file = new File(System.getProperty("user.home") + "/.EHRI/sync.xml");
        Map<String, List<FileMetaModel>> data = null;
        if (file.exists()) {
            data = (Map<String, List<FileMetaModel>>) xStream.fromXML(file);
        }

        return data;
    }

    public Map<String, ProviderConfigModel>  loadCHIIngestConfig() throws IOException {

        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(new File(System.getProperty("user.home") + "/.EHRI/chi-config.json")));

        return gson.fromJson(reader, CONFIG_TYPE);
    }

    public void saveMetaData(Map<String, List<FileMetaModel>> savedRankings) throws URISyntaxException {
        XStream xstream = new XStream();
        xstream.alias("syncMeta", ProcessUpdateService.class);
        String xml = xstream.toXML(savedRankings);

        writeToFile(xml, "sync.xml");
    }

    private void writeToFile(String data, String resource) {
        File file = new File(System.getProperty("user.home") + "/.EHRI");
        if (!file.exists()) {
            file.mkdir();
        }

        File f = new File(file.getAbsolutePath() + "/" + resource);
        BufferedWriter writer;
        try(FileWriter fileWriter = new FileWriter(f)) {
            writer = new BufferedWriter(fileWriter);
            writer.write(data + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
