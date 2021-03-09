package com.simple.elasticsearch.controller;

import com.simple.elasticsearch.entity.Topic;
import com.simple.elasticsearch.service.TestService;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author zcw
 * @version 1.0
 * @date 2021/1/14 11:19
 */
@RestController
@RequestMapping(value = "/es")
public class TestController {

    @Autowired
    private TestService testService;

    @PostMapping(value = "insert")
    public void insertOrUpdateOne(@RequestBody Topic entity) {
        testService.insertOrUpdateOne(entity);
    }

    @GetMapping(value = "/get")
    public List<Topic> get() {
        return testService.search(new SearchSourceBuilder());
    }

    @GetMapping(value = "/test")
    public List<Topic> test(){
        return testService.test();
    }

}
