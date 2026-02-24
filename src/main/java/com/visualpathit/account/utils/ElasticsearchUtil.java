package com.visualpathit.account.utils;

import java.net.InetSocketAddress;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.visualpathit.account.beans.Components;
@Service
public class ElasticsearchUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchUtil.class);
	
	private static Components object;
    @Autowired
    public void setComponents(Components object){
    	ElasticsearchUtil.object = object;    	
    	
    }
    public static TransportClient trannsportClient() {
	    LOGGER.info("elasticsearch client");
    	String elasticsearchHost =object.getElasticsearchHost();
    	String elasticsearchPort =object.getElasticsearchPort(); 
    	String elasticsearchCluster =object.getElasticsearchCluster();
    	String elasticsearchNode =object.getElasticsearchNode();
	    LOGGER.info("elasticsearchHost ........{}", elasticsearchHost);
	    LOGGER.info("elasticsearchPort ........{}", elasticsearchPort);
    	TransportClient client = null;
    	try {    	
    	Settings settings = Settings.builder()    			
    			.put("cluster.name",elasticsearchCluster)
    			.put("node.name",elasticsearchNode)
    			.build();
    	client = new PreBuiltTransportClient(settings)
                .addTransportAddress(
                new InetSocketTransportAddress(
                		new InetSocketAddress(elasticsearchHost, Integer.parseInt(elasticsearchPort))));

        
    	}
    	catch (Exception e) {
			LOGGER.error("Failed to create Elasticsearch client", e);
		}
    	return client;
      }
}
