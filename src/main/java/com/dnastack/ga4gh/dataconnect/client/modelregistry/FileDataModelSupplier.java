package com.dnastack.ga4gh.dataconnect.client.modelregistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import com.dnastack.ga4gh.dataconnect.DataModelSupplier;
import com.dnastack.ga4gh.dataconnect.model.DataModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileDataModelSupplier implements DataModelSupplier {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public DataModel supply(String tableName) {        
   
    	String model_file = "/models/" + tableName + ".data_dict.json";
        try {
        	BufferedReader br = new BufferedReader(
        			     new FileReader(model_file));
        	DataModel dm = objectMapper.readValue(br, DataModel.class);
        	return dm;

        } catch (Exception ex) {
        	log.error("Failed to load or convert DataModel for {}", tableName, ex);
        	return null;
        }
    }
    
    private File getFileFromResource(String fileName) throws URISyntaxException{

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {

            // failed if files have whitespaces or special characters
            //return new File(resource.getFile());

            return new File(resource.toURI());
        }

    }
}
