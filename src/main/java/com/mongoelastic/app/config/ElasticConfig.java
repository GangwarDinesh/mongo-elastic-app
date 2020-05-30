package com.mongoelastic.app.config;

import java.net.InetAddress;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticConfig {

	@Value("${elastic.server.host}")
	private String hostName;
	
	@Value("${elastic.server.port}")
	private int portNumber;
	
	@Value("${elastic.server.cluster}")
	private String clusterName;
	
	@Value("${elastic.server.node}")
	private String nodeName;
	
	@Value("${elastic.server.sniff}")
	private boolean sniff;
	
	@Value("${elastic.server.ignoreCluster}")
	private boolean ignoreClusterName;
	
	@Bean
	public TransportClient client() {
		
		TransportClient client = null;
		
		try {
			Settings settings = Settings.builder()
					.put("cluster.name", clusterName)
					.put("client.transport.sniff", sniff)
					.put("client.transport.ignore_cluster_name", ignoreClusterName)
					.put("node.name", nodeName)
					.build();
			
			client = new PreBuiltTransportClient(settings);
			client.addTransportAddress(new TransportAddress(InetAddress.getByName(hostName), portNumber));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return client;
	}
	
}
