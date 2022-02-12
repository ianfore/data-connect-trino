package com.dnastack.ga4gh.dataconnect.client.datadictregistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.dnastack.ga4gh.dataconnect.DataModelSupplier;
import com.dnastack.ga4gh.dataconnect.model.DataModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.JAXB;

import gov.nih.dbgap.dict.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConfigurationProperties("app.datadict-datamodel-supplier")
public class DataDictModelSupplier implements DataModelSupplier {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public DataModel supply(String tableName) {        
   
    	String model_file = "/models/xml/" + tableName + ".data_dict.xml";
    	
        try {
        	BufferedReader br = new BufferedReader(
        			     new FileReader(model_file));
        	DataTable dt = JAXB.unmarshal(br, DataTable.class);
        	
        	DataModel dm = new DataModel();
        	dm.setAdditionalProperty("description", dt.getDescription());
        	return dm;

        } catch (Exception ex) {
        	log.error("Failed to load or convert DataModel for {}", tableName, ex);
        	return null;
        }
    }    
}

