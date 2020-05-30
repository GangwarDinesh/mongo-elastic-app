package com.mongoelastic.app.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongoelastic.app.entity.SampleData;
import com.mongoelastic.app.repository.SampeDataRepository;

@Service
public class MongoElasticServiceImpl implements MongoElasticService {
	
	@Autowired
	private Client client;
	
	@Autowired
	private SampeDataRepository sampeDataRepository;

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Integer> createData(List<SampleData> dataList) {
		int successCount = 0;
		int failedCount = 0;
		
		dataList = sampeDataRepository.insert(dataList);
		
		ObjectMapper mapper = new ObjectMapper();
		List<IndexRequestBuilder> indexRequestBuilders = new ArrayList<>();
		Map<String, Integer> result = new HashMap<>();
		
		for(int count = 0; count< dataList.size(); count++) {
			SampleData sampleData = dataList.get(count);
			Map<String, Object> sampleDataMap = mapper.convertValue(sampleData, Map.class);
			try {
				indexRequestBuilders.add(client.prepareIndex("dataset", "sampledata", String.valueOf(sampleData.getId())).setSource(sampleDataMap));
				successCount = successCount +indexRequestBuilders.get(count).get().getShardInfo().getSuccessful(); 
				failedCount = failedCount + indexRequestBuilders.get(count).get().getShardInfo().getFailed();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		result.put("total_count", successCount+failedCount);
		result.put("success_count", successCount);
		result.put("failed_count", failedCount);
		
		return result;
		
	}

	@Override
	public ShardInfo updateData(SampleData sampleData) {
		SampleData updatedSampleData = sampeDataRepository.save(sampleData);
		ShardInfo shardInfo = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest.index("dataset")
							.type("sampledata")
								.id(String.valueOf(updatedSampleData.getId()))
									.doc(mapper.convertValue(updatedSampleData, Map.class));
			ActionFuture<UpdateResponse> actionFuture = client.update(updateRequest);
			shardInfo = actionFuture.actionGet().getShardInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return shardInfo;
		
	}

	@Override
	public List<Map<String, Object>> readData(int pageSize, int pageNumber, String inputText) {
		List<Map<String, Object>> response = new ArrayList<>();
		SearchResponse searchResponse = null;
		
		try {
			int fromIndex = -1;
			if(pageNumber == 0) {
				fromIndex = 0;
			}else {
				fromIndex = pageSize * pageNumber + 1;
			}
			
			if(null != inputText && !inputText.isEmpty()) {
				searchResponse = client.prepareSearch("dataset")
						.setTypes("sampledata")
						.setSearchType(SearchType.QUERY_THEN_FETCH)
						.setQuery(QueryBuilders.wildcardQuery("name", "*"+inputText.toLowerCase()+"*"))
						.setSize(pageSize)
						.get();	
			}else {
				searchResponse = client.prepareSearch("dataset")
						.setTypes("sampledata")
						.setQuery(QueryBuilders.matchAllQuery())
						.setFrom(fromIndex)
						.setSize(pageSize)
						.get();
				
			}
			if( null != searchResponse) {
				List<SearchHit> hitsList = Arrays.asList(searchResponse.getHits().getHits());
				hitsList.forEach(hits->{
					response.add(hits.getSourceAsMap());
				});
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		return response;
		
	}

	@Override
	public ShardInfo deleteData(Long id, boolean deletedById) {
		ShardInfo shardInfo = null;
		try {

			if(deletedById) {
				sampeDataRepository.deleteById(id);
				try {
					DeleteResponse deleteResponse = client.prepareDelete("dataset", "sampledata", String.valueOf(id)).get();
					shardInfo = deleteResponse.getShardInfo();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else {
				sampeDataRepository.deleteAll();
				AcknowledgedResponse resp = client.admin().indices().prepareDelete("dataset").execute().actionGet();
				System.out.println("All indexes delete : "+resp.isAcknowledged());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return shardInfo;
		
	}

}
