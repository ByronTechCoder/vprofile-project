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
	
	private final Components components;

	@Autowired
	public ElasticsearchUtil(Components components) {
		this.components = components;
	}

    public TransportClient transportClient() {
	    LOGGER.info("elasticsearch client");
	    String elasticsearchHost = components.getElasticsearchHost();
	    String elasticsearchPort = components.getElasticsearchPort(); 
	    String elasticsearchCluster = components.getElasticsearchCluster();
	    String elasticsearchNode = components.getElasticsearchNode();
	    LOGGER.info("elasticsearchHost ........{}", elasticsearchHost);
	    LOGGER.info("elasticsearchPort ........{}", elasticsearchPort);
	    try {
	    	Settings settings = Settings.builder()
	    			.put("cluster.name", elasticsearchCluster)
	    			.put("node.name", elasticsearchNode)
	    			.build();
	    	return new PreBuiltTransportClient(settings)
	    			.addTransportAddress(
	    					new InetSocketTransportAddress(
	    							new InetSocketAddress(elasticsearchHost, Integer.parseInt(elasticsearchPort))));
	    } catch (NumberFormatException e) {
	    	throw new IllegalStateException("Invalid Elasticsearch port", e);
	    } catch (RuntimeException e) {
	    	throw new IllegalStateException("Failed to create Elasticsearch client", e);
	    }
      }
}
