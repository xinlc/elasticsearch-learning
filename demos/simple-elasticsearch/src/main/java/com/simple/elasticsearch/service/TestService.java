package com.simple.elasticsearch.service;

import com.simple.elasticsearch.entity.Topic;
import com.simple.elasticsearch.util.EsService;
import org.elasticsearch.common.unit.Fuzziness;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zcw
 * @version 1.0
 * @date 2021/1/14 11:59
 */
@Service
public class TestService extends EsService<Topic> {

    public List<Topic> test() {
        esLambdaQuery().notIn(Topic::getId, 1, 2).delete();
        List<Topic> topics = esLambdaQuery().between(Topic::getId, 2, 3).query();
        return topics;
    }

    public List<Topic> match() {
        return esLambdaQuery().fuzzyAll(Topic::getContent, Fuzziness.TWO, "abc").query();
    }

    public void delete() {
        esLambdaQuery().eq(Topic::getId,1).delete();
    }

}
