package com.dnastack.ga4gh.dataconnect.client.datadictregistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.dnastack.ga4gh.dataconnect.DataModelSupplier;
import com.dnastack.ga4gh.dataconnect.model.ColumnSchema;
import com.dnastack.ga4gh.dataconnect.model.DataModel;
//import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;

import gov.nih.dbgap.dict.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConfigurationProperties("app.datadict-datamodel-supplier")
public class DataDictModelSupplier implements DataModelSupplier {

    //private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final String base = "https://ftp.ncbi.nlm.nih.gov/dbgap/studies/";
    
    private final String idPrefix = "dbgap:";
    
    private Map<String, String> dictMap;
    
    private Pattern pattern;
    
    public DataDictModelSupplier() {
    	this.dictMap = new HashMap<String, String>(); 
    	this.dictMap.put("dbgap_demo.scr_gecco_susceptibility.subject_phenotypes_multi", 
    			"phs001554/phs001554.v1.p1/pheno_variable_summaries/phs001554.v1.pht007609.v1.GECCO_CRC_Susceptibility_Subject_Phenotypes.data_dict.xml");
    	
    	this.pattern = Pattern.compile(".*\\.(pht\\d*\\.v\\d*)\\..*");
    }

    private BufferedReader getDictReader(String tableName) {
    	BufferedReader reader = null;
    	try{
            URL url = new URL(this.base + this.dictMap.get(tableName));
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
          }catch(Exception ex){
            System.out.println(ex);
          }
        return reader;
    }
    
    private String getTableId(String tableName) {
        String dict_path = this.dictMap.get(tableName);
        Matcher matcher = this.pattern.matcher(dict_path);
        if (matcher.find()) {
        	return (this.idPrefix + matcher.group(1));
        }
        else {
        	return dict_path;
        }
    }
    
    @Override
    public DataModel supply(String tableName) {        
   
        try {
        	BufferedReader br = this.getDictReader(tableName);
        	DataTable dt = JAXB.unmarshal(br, DataTable.class);
        	
        	DataModel dm = new DataModel();
        	dm.setAdditionalProperty("description", dt.getDescription());
        	dm.setAdditionalProperty("$id", this.getTableId(tableName));
        	
        	List<Variable> variables = dt.getVariable();
            Iterator<Variable> it = variables.iterator();
            
            Map<String, ColumnSchema> properties = new HashMap<String, ColumnSchema>();
            
            while (it.hasNext()) {
                Variable var = it.next();
                
                ColumnSchema col = new ColumnSchema();
                col.setAdditionalProperty("$id", this.idPrefix + var.getId());
                col.setAdditionalProperty("description", var.getDescription());
                col.setAdditionalProperty("type", var.getType());
                col.setAdditionalProperty("$unit", var.getUnit());
                
                List<gov.nih.dbgap.dict.Value> values = var.getValue();
                Iterator<Value> valueIterator = values.iterator();
                while (valueIterator.hasNext()) {
                	Value val = valueIterator.next();
                	val.getCode();
                	val.getContent();
                }
                
                //col.setAdditionalProperty('type'), var.getType());
                properties.put(var.getName(), col);
            }
            
        	dm.setAdditionalProperty("properties", properties);
        	return dm;

        } catch (Exception ex) {
        	log.error("Failed to load or convert DataModel for {}", tableName, ex);
        	return null;
        }
    }    
}

