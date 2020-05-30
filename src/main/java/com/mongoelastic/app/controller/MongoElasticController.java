package com.mongoelastic.app.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongoelastic.app.entity.SampleData;
import com.mongoelastic.app.service.MongoElasticService;

@RestController
@RequestMapping("/home")
@CrossOrigin("*")
public class MongoElasticController {

	@Autowired
	private MongoElasticService mongoElasticService;
	
	@PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createData(@RequestBody List<SampleData> dataList) {
		
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("timestamp", LocalDateTime.now());
		
		Map<String, Integer> result = mongoElasticService.createData(dataList);
		if(null != result && !result.isEmpty()) {
			responseMap.put("status", HttpStatus.OK);
			responseMap.put("message", "Data created and indexed successfully.");
			responseMap.put("reponse", result);
		}else {
			responseMap.put("status", HttpStatus.EXPECTATION_FAILED);
			responseMap.put("message", "Technical error occurred.");
			responseMap.put("reponse", "{}");
		}
		
		return new ResponseEntity<Map<String, Object>>(responseMap, new HttpHeaders(), HttpStatus.OK);
	}
	
	@PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateData(@RequestBody SampleData sampleData) {
		
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("timestamp", LocalDateTime.now());
		
		ShardInfo result = mongoElasticService.updateData(sampleData);
		if(null != result) {
			responseMap.put("status", HttpStatus.OK);
			responseMap.put("message", "Data updated and re-indexed successfully.");
			responseMap.put("reponse", result);
		}else {
			responseMap.put("status", HttpStatus.EXPECTATION_FAILED);
			responseMap.put("message", "Technical error occurred.");
			responseMap.put("reponse", "{}");
		}
		
		return new ResponseEntity<Map<String, Object>>(responseMap, new HttpHeaders(), HttpStatus.OK);
	}
	
	@GetMapping(value = "/fetch", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> readData(@RequestParam("pageSize") int pageSize, @RequestParam("pageNumber") int pageNumber, @RequestParam("inputText") String inputText) {
		
		List<Map<String, Object>> responseList = mongoElasticService.readData(pageSize, pageNumber, inputText);
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("timestamp", LocalDateTime.now());
		responseMap.put("response", responseList);
		if(null != responseList && !responseList.isEmpty()) {
			responseMap.put("status", HttpStatus.FOUND);
		}else {
			responseMap.put("status", HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<Map<String, Object>>(responseMap, new HttpHeaders(), HttpStatus.OK);
	}
	
	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> deleteData(@RequestParam("id") Long id, @RequestParam("deletedById") boolean deletedById) {
		
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("timestamp", LocalDateTime.now());
		
		ShardInfo result = mongoElasticService.deleteData(id, deletedById);
		if(null != result) {
			responseMap.put("status", HttpStatus.OK);
			responseMap.put("message", "Data deleted and index removed successfully.");
			responseMap.put("result", result);
		}
		else {
			responseMap.put("status", HttpStatus.EXPECTATION_FAILED);
			responseMap.put("message", "Technical error occurred.");
			responseMap.put("reponse", "{}");
		}
		return new ResponseEntity<Map<String, Object>>(responseMap, new HttpHeaders(), HttpStatus.OK);
	}
	
}
