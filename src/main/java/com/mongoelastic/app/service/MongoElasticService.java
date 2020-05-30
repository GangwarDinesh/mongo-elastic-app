package com.mongoelastic.app.service;

import java.util.List;
import java.util.Map;

import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;

import com.mongoelastic.app.entity.SampleData;

public interface MongoElasticService {

	Map<String, Integer> createData(List<SampleData> dataList);
	
	ShardInfo updateData(SampleData sampleData);
	
	List<Map<String, Object>> readData(int pageSize, int pageNumber, String inputText);
	
	ShardInfo deleteData(Long id, boolean deletedById);
}
