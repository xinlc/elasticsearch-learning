package org.zxp.esclientrhl.repository;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGeneratorBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestion;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.config.ElasticsearchProperties;
import org.zxp.esclientrhl.enums.AggsType;
import org.zxp.esclientrhl.enums.DataType;
import org.zxp.esclientrhl.enums.SqlFormat;
import org.zxp.esclientrhl.index.ElasticsearchIndex;
import org.zxp.esclientrhl.repository.response.ScrollResponse;
import org.zxp.esclientrhl.repository.response.UriResponse;
import org.zxp.esclientrhl.util.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * program: esdemo
 * description: Elasticsearch基础功能组件实现类
 * author: X-Pacific zhang
 * create: 2019-01-18 16:04
 **/
@Component
public class ElasticsearchTemplateImpl<T, M> implements ElasticsearchTemplate<T, M> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ElasticsearchIndex elasticsearchIndex;


    @Override
    public Response request(Request request) throws Exception {
        Response response = client.getLowLevelClient().performRequest(request);
        return response;
    }

    @Override
    public boolean save(T t) throws Exception {
        return save(t,null);
    }

    @Override
    public boolean save(T t, String routing) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        String id = Tools.getESId(t);
        IndexRequest indexRequest = null;
        if (StringUtils.isEmpty(id)) {
            indexRequest = new IndexRequest(indexname, indextype);
        } else {
            indexRequest = new IndexRequest(indexname, indextype, id);
        }
        String source = JsonUtils.obj2String(t);
        indexRequest.source(source, XContentType.JSON);
        if(!StringUtils.isEmpty(routing)){
            indexRequest.routing(routing);
        }
        IndexResponse indexResponse = null;
        indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            logger.info("INDEX CREATE SUCCESS");
            //异步执行rollover
            elasticsearchIndex.rollover(t.getClass(),true);
        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            logger.info("INDEX UPDATE SUCCESS");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public BulkResponse save(List<T> list) throws Exception {
        if (list == null || list.size() == 0) {
            return null;
        }
        T t = list.get(0);
        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        return savePart(list,indexname,indextype);
    }

    @Override
    public BulkResponse[] saveBatch(List<T> list) throws Exception {
        if (list == null || list.size() == 0) {
            return null;
        }
        T t = list.get(0);
        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        List<List<T>> lists = Tools.splitList(list, true);
        BulkResponse[] bulkResponses = new BulkResponse[lists.size()];
        for (int i = 0; i < lists.size(); i++) {
            bulkResponses[i] = savePart(lists.get(i),indexname,indextype);
        }
        return bulkResponses;
    }

    private BulkResponse savePart(List<T> list,String indexname,String indextype) throws Exception {
        BulkRequest rrr = new BulkRequest();
        Class clazz = null;
        for (int i = 0; i < list.size(); i++) {
            T tt = list.get(i);
            clazz = tt.getClass();
            String id = Tools.getESId(tt);
            String sourceJsonStr = JsonUtils.obj2String(tt);
            rrr.add(new IndexRequest(indexname, indextype, id)
//                    .source(BeanTools.objectToMap(tt)));
                    .source(sourceJsonStr, XContentType.JSON));
        }
        BulkResponse bulkResponse = client.bulk(rrr, RequestOptions.DEFAULT);
        //异步执行rollover
        elasticsearchIndex.rollover(clazz,true);
        return bulkResponse;
    }

    @Override
    public BulkResponse bulkUpdate(List<T> list) throws Exception {
        if (list == null || list.size() == 0) {
            return null;
        }
        T t = list.get(0);
        if(Tools.checkNested(t)){
            throw new Exception("nested对象更新，请使用覆盖更新");
        }
        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        return updatePart(list, indexname, indextype);
    }

    @Override
    public BulkResponse[] bulkUpdateBatch(List<T> list) throws Exception {
        if (list == null || list.size() == 0) {
            return null;
        }
        T t = list.get(0);
        if(Tools.checkNested(t)){
            throw new Exception("nested对象更新，请使用覆盖更新");
        }
        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        List<List<T>> lists = Tools.splitList(list, true);
        BulkResponse[] bulkResponses = new BulkResponse[lists.size()];
        for (int i = 0; i < lists.size(); i++) {
            bulkResponses[i] = updatePart(lists.get(i),indexname,indextype);
        }
        return bulkResponses;
    }

    private BulkResponse updatePart(List<T> list,String indexname,String indextype) throws Exception {
        BulkRequest rrr = new BulkRequest();
        for (int i = 0; i < list.size(); i++) {
            T tt = list.get(i);
            String id = Tools.getESId(tt);
            rrr.add(new UpdateRequest(indexname, indextype, id)
                    .doc(Tools.getFieldValue(tt)));
        }
        BulkResponse bulkResponse = client.bulk(rrr, RequestOptions.DEFAULT);
        return bulkResponse;
    }

    @Override
    public boolean update(T t) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        String id = Tools.getESId(t);
        if (StringUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        if(Tools.checkNested(t)){
            throw new Exception("nested对象更新，请使用覆盖更新");
        }
        UpdateRequest updateRequest = new UpdateRequest(indexname, indextype, id);
        updateRequest.doc(Tools.getFieldValue(t));
        UpdateResponse updateResponse = null;
        updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            logger.info("INDEX CREATE SUCCESS");
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            logger.info("INDEX UPDATE SUCCESS");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public BulkResponse batchUpdate(QueryBuilder queryBuilder, T t, Class clazz, int limitcount, boolean asyn) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        if (queryBuilder == null) {
            throw new NullPointerException();
        }
        if(Tools.checkNested(t)){
            throw new Exception("nested对象更新，请使用覆盖更新");
        }
        if(Tools.getESId(t) == null || "".equals(Tools.getESId(t))) {
            PageSortHighLight psh = new PageSortHighLight(1, limitcount);
            psh.setHighLight(null);
            PageList pageList = this.search(queryBuilder, psh, clazz, indexname);
            if (pageList.getTotalElements() > limitcount) {
                throw new Exception("beyond the limitcount");
            }
            if (asyn) {
                new Thread(() -> {
                    try {
                        batchUpdate(pageList.getList(), indexname, indextype, t);
                        logger.info("asyn batch finished update");
                    } catch (Exception e) {
                        logger.error("asyn batch update fail", e);
                    }
                }).start();
                return null;
            } else {
                return batchUpdate(pageList.getList(), indexname, indextype, t);
            }
        }else{
            throw new Exception("批量更新请不要给主键传值");
        }
    }

    private BulkResponse batchUpdate(List<T> list, String indexname, String indextype,T tot) throws Exception {
        Map map = Tools.getFieldValue(tot);
        BulkRequest rrr = new BulkRequest();
        for (int i = 0; i < list.size(); i++) {
            T tt = list.get(i);
            rrr.add(new UpdateRequest(indexname, indextype, Tools.getESId(tt))
                    .doc(map));
        }
        BulkResponse bulkResponse = client.bulk(rrr, RequestOptions.DEFAULT);
        return bulkResponse;
    }


    @Override
    public boolean updateCover(T t) throws Exception {
        return save(t);
    }

    @Override
    public boolean delete(T t) throws Exception {
        return delete(t,null);
    }

    @Override
    public boolean delete(T t, String routing) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        String id = Tools.getESId(t);
        if (StringUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        DeleteRequest deleteRequest = new DeleteRequest(indexname, indextype, id);
        if(!StringUtils.isEmpty(routing)){
            deleteRequest.routing(routing);
        }
        DeleteResponse deleteResponse = null;
        deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
            logger.info("INDEX DELETE SUCCESS");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public BulkByScrollResponse deleteByCondition(QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexname);
        request.setQuery(queryBuilder);
        BulkByScrollResponse bulkResponse = client.deleteByQuery(request, RequestOptions.DEFAULT);
        return bulkResponse;
    }


    @Override
    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse;
    }

    @Override
    public List<T> search(QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return search(queryBuilder, clazz, indexname);
    }

    @Override
    public List<T> search(QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String indextype = metaData.getIndextype();
        List<T> list = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(indexs);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(Constant.DEFALT_PAGE_SIZE);
        searchRequest.source(searchSourceBuilder);
        if (metaData.isPrintLog()) {
            logger.info(searchSourceBuilder.toString());
        }
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            T t = JsonUtils.string2Obj(hit.getSourceAsString(), clazz);
            //将_id字段重新赋值给@ESID注解的字段
            correctID(clazz, t, (M)hit.getId());
            list.add(t);
        }
        return list;
    }

    @Override
    public List<T> searchMore(QueryBuilder queryBuilder,int limitSize, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return searchMore(queryBuilder,limitSize,clazz,indexname);
    }

    @Override
    public List<T> searchMore(QueryBuilder queryBuilder,int limitSize, Class<T> clazz, String... indexs) throws Exception {
        PageSortHighLight pageSortHighLight = new PageSortHighLight(1, limitSize);
        PageList pageList = search(queryBuilder, pageSortHighLight, clazz, indexs);
        if(pageList != null){
            return pageList.getList();
        }
        return null;
    }

    @Override
    public List<T> searchUri(String uri, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        List<T> list = new ArrayList<>();
        Request request = new Request("GET","/"+indexname+"/"+indextype+"/_search/?"+uri);
        Response response = request(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        if (metaData.isPrintLog()) {
            logger.info("searchUri请求报文："+"/"+indexname+"/"+indextype+"/_search/?"+uri);
            logger.info("searchUri返回报文："+responseBody);
        }
        UriResponse uriResponse = JsonUtils.string2Obj(responseBody, UriResponse.class);

        T[] ts = (T[]) Array.newInstance(clazz, uriResponse.getHits().getHits().size());
        for (int i = 0; i < uriResponse.getHits().getHits().size(); i++) {
            T t = (T) clazz.newInstance();
            //先将LinkedHashMap（json解析后是Map类型）转化成Object
            Object obj = BeanTools.mapToObject((Map) uriResponse.getHits().getHits().get(i).get_source(),clazz);
            //将Object属性拷贝
            BeanUtils.copyProperties(obj, t);
            //将_id字段重新赋值给@ESID注解的字段
            correctID(clazz, t, (M)uriResponse.getHits().getHits().get(i).get_id());
            ts[i] = t;
        }
        return Arrays.asList(ts);
    }

    @Autowired
    ElasticsearchProperties elasticsearchProperties;

    @Override
    public String queryBySQL(String sql, SqlFormat sqlFormat) throws Exception {
        String host = elasticsearchProperties.getHost();
        if(StringUtils.isEmpty(host)){
            host = Constant.DEFAULT_ES_HOST;
        }
        String ipport = "";
        String[] hosts = host.split(",");
        if(hosts.length == 1){
            ipport = hosts[0];
        }else{//随机选择配置的地址
            int randomindex = new Random().nextInt(hosts.length);
            ipport = hosts[randomindex];
        }
        ipport = "http://"+ipport;
        logger.info(ipport+"/_sql?format="+sqlFormat.getFormat());
        logger.info("{\"query\":\""+sql+"\"}");

        String username = elasticsearchProperties.getUsername();
        String password = elasticsearchProperties.getPassword();
        if(!StringUtils.isEmpty(username)) {
            return HttpClientTool.execute(ipport+"/_sql?format="+sqlFormat.getFormat(),"{\"query\":\""+sql+"\"}",username,password);
        }
        return HttpClientTool.execute(ipport+"/_sql?format="+sqlFormat.getFormat(),"{\"query\":\""+sql+"\"}");
    }


    @Override
    public long count(QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return count(queryBuilder, clazz, indexname);
    }

    @Override
    public long count(QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        CountRequest countRequest = new CountRequest(indexs);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        countRequest.source(searchSourceBuilder);
        CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
        long count = countResponse.getCount();
        return count;
    }

    @Override
    public T getById(M id, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        if (StringUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        GetRequest getRequest = new GetRequest(indexname, indextype, id.toString());
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        if (getResponse.isExists()) {
            return JsonUtils.string2Obj(getResponse.getSourceAsString(), clazz);
        }
        return null;
    }

    @Override
    public List<T> mgetById(M[] ids, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        MultiGetRequest request = new MultiGetRequest();
        for (int i = 0; i < ids.length; i++) {
            request.add(new MultiGetRequest.Item(indexname, indextype, ids[i].toString()));
        }
        MultiGetResponse response = client.mget(request, RequestOptions.DEFAULT);
        List<T> list = new ArrayList<>();
        for (int i = 0; i < response.getResponses().length; i++) {
            MultiGetItemResponse item = response.getResponses()[i];
            GetResponse getResponse = item.getResponse();
            if (getResponse.isExists()) {
                list.add(JsonUtils.string2Obj(getResponse.getSourceAsString(), clazz));
            }
        }
        return list;
    }

    @Override
    public boolean exists(M id, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        if (StringUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        GetRequest getRequest = new GetRequest(indexname, indextype, id.toString());
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        if (getResponse.isExists()) {
            return true;
        }
        return false;
    }


    @Override
    public Map aggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return aggs(metricName, aggsType, queryBuilder, clazz, bucketName, indexname);
    }

    @Override
    public Map aggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, String... indexs) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = indexs;
        Field f_metric = clazz.getDeclaredField(metricName.replaceAll(keyword, ""));
        Field f_bucket = clazz.getDeclaredField(bucketName.replaceAll(keyword, ""));
        if (f_metric == null) {
            throw new Exception("metric field is null");
        }
        if (f_bucket == null) {
            throw new Exception("bucket field is null");
        }
        metricName = genKeyword(f_metric, metricName);
        bucketName = genKeyword(f_bucket, bucketName);

        //定义聚合临时变量不需要加keyword
        String by = "by_" + bucketName.replaceAll(keyword, "");
        String me = aggsType.toString() + "_" + metricName.replaceAll(keyword, "");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermsAggregationBuilder aggregation = AggregationBuilders.terms(by)
                .field(bucketName);
        //默认按照聚合结果降序排序
        aggregation.order(BucketOrder.aggregation(me, false));
//        aggregation.order(BucketOrder.key(false));
        if (AggsType.count == aggsType) {
            aggregation.subAggregation(AggregationBuilders.count(me).field(metricName)).size(Constant.AGG_RESULT_COUNT);
        } else if (AggsType.min == aggsType) {
            aggregation.subAggregation(AggregationBuilders.min(me).field(metricName)).size(Constant.AGG_RESULT_COUNT);
        } else if (AggsType.max == aggsType) {
            aggregation.subAggregation(AggregationBuilders.max(me).field(metricName)).size(Constant.AGG_RESULT_COUNT);
        } else if (AggsType.sum == aggsType) {
            aggregation.subAggregation(AggregationBuilders.sum(me).field(metricName)).size(Constant.AGG_RESULT_COUNT);
        } else if (AggsType.avg == aggsType) {
            aggregation.subAggregation(AggregationBuilders.avg(me).field(metricName)).size(Constant.AGG_RESULT_COUNT);
        }
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregation);


        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        if (metaData.isPrintLog()) {
            logger.info(searchSourceBuilder.toString());
        }
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        Aggregations aggregations = searchResponse.getAggregations();
        Terms by_risk_code = aggregations.get(by);
        Map map = new LinkedHashMap();
        for (Terms.Bucket bucket : by_risk_code.getBuckets()) {
            if (AggsType.count == aggsType) {
                ValueCount count = bucket.getAggregations().get(me);
                long value = count.getValue();
                map.put(bucket.getKey(), value);
            } else if (AggsType.min == aggsType) {
                ParsedMin min = bucket.getAggregations().get(me);
                double value = min.getValue();
                map.put(bucket.getKey(), value);
            } else if (AggsType.max == aggsType) {
                ParsedMax max = bucket.getAggregations().get(me);
                double value = max.getValue();
                map.put(bucket.getKey(), value);
            } else if (AggsType.sum == aggsType) {
                ParsedSum sum = bucket.getAggregations().get(me);
                double value = sum.getValue();
                map.put(bucket.getKey(), value);
            } else if (AggsType.avg == aggsType) {
                ParsedAvg avg = bucket.getAggregations().get(me);
                double value = avg.getValue();
                map.put(bucket.getKey(), value);
            }
        }
        return map;
    }

    @Override
    public List<Down> aggswith2level(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String[] bucketNames) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return aggswith2level(metricName, aggsType, queryBuilder, clazz, bucketNames, indexname);
    }

    @Override
    public List<Down> aggswith2level(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String[] bucketNames, String... indexs) throws Exception {
        String[] indexname = indexs;
        Field f_metric = clazz.getDeclaredField(metricName.replaceAll(keyword, ""));
        if (bucketNames == null) {
            throw new NullPointerException();
        }
        if (bucketNames.length != 2) {
            throw new Exception("仅支持两层下钻聚合!");
        }
        Field[] f_buckets = new Field[bucketNames.length];
        for (int i = 0; i < bucketNames.length; i++) {
            f_buckets[i] = clazz.getDeclaredField(bucketNames[i].replaceAll(keyword, ""));
            if (f_buckets[i] == null) {
                throw new Exception("bucket field is null");
            }
        }
        if (f_metric == null) {
            throw new Exception("metric field is null");
        }
        metricName = genKeyword(f_metric, metricName);
        String me = aggsType.toString() + "_" + metricName.replaceAll(keyword, "");

        String[] bys = new String[bucketNames.length];
        for (int i = 0; i < f_buckets.length; i++) {
            bucketNames[i] = genKeyword(f_buckets[i], bucketNames[i]);
            bys[i] = "by_" + bucketNames[i].replaceAll(keyword, "");
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermsAggregationBuilder[] termsAggregationBuilders = new TermsAggregationBuilder[bucketNames.length];
        for (int i = 0; i < bucketNames.length; i++) {
            TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms(bys[i]).field(bucketNames[i]);
            termsAggregationBuilders[i] = aggregationBuilder;
        }
        for (int i = 0; i < termsAggregationBuilders.length; i++) {
            if (i != termsAggregationBuilders.length - 1) {
                termsAggregationBuilders[i].subAggregation(termsAggregationBuilders[i + 1]).size(Constant.AGG_RESULT_COUNT);
            }
        }
        if (AggsType.count == aggsType) {
            termsAggregationBuilders[termsAggregationBuilders.length - 1]
                    .subAggregation(AggregationBuilders.count(me).field(metricName)).size(Constant.AGG_RESULT_COUNT);
        } else if (AggsType.min == aggsType) {
            termsAggregationBuilders[termsAggregationBuilders.length - 1]
                    .subAggregation(AggregationBuilders.min(me).field(metricName)).size(Constant.AGG_RESULT_COUNT);
        } else if (AggsType.max == aggsType) {
            termsAggregationBuilders[termsAggregationBuilders.length - 1]
                    .subAggregation(AggregationBuilders.max(me).field(metricName)).size(Constant.AGG_RESULT_COUNT);
        } else if (AggsType.sum == aggsType) {
            termsAggregationBuilders[termsAggregationBuilders.length - 1]
                    .subAggregation(AggregationBuilders.sum(me).field(metricName)).size(Constant.AGG_RESULT_COUNT);
        } else if (AggsType.avg == aggsType) {
            termsAggregationBuilders[termsAggregationBuilders.length - 1]
                    .subAggregation(AggregationBuilders.avg(me).field(metricName)).size(Constant.AGG_RESULT_COUNT);
        }
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(termsAggregationBuilders[0]);
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        //下面不支持2层以上的下钻
        List<Down> downList = new ArrayList<>();
        Terms terms1 = searchResponse.getAggregations().get(bys[0]);
        Terms terms2;
        for (Terms.Bucket bucket : terms1.getBuckets()) {
            terms2 = bucket.getAggregations().get(bys[1]);
            for (Terms.Bucket bucket2 : terms2.getBuckets()) {
                Down down = new Down();
                down.setLevel_1_key(bucket.getKey().toString());
                down.setLevel_2_key(bucket2.getKey().toString());
                if (AggsType.count == aggsType) {
                    ValueCount count = bucket2.getAggregations().get(me);
                    long value = count.getValue();
                    down.setValue(value);
                } else if (AggsType.min == aggsType) {
                    ParsedMin min = bucket2.getAggregations().get(me);
                    double value = min.getValue();
                    down.setValue(value);
                } else if (AggsType.max == aggsType) {
                    ParsedMax max = bucket2.getAggregations().get(me);
                    double value = max.getValue();
                    down.setValue(value);
                } else if (AggsType.sum == aggsType) {
                    ParsedSum sum = bucket2.getAggregations().get(me);
                    double value = sum.getValue();
                    down.setValue(value);
                } else if (AggsType.avg == aggsType) {
                    ParsedAvg avg = bucket2.getAggregations().get(me);
                    double value = avg.getValue();
                    down.setValue(value);
                }
                downList.add(down);
            }
        }
        return downList;
    }


    private static final String keyword = ".keyword";

    /**
     * 组织字段是否带有.keyword
     *
     * @param field
     * @param name
     * @return
     */
    private String genKeyword(Field field, String name) {
        ESMapping esMapping = field.getAnnotation(ESMapping.class);
        //带着.keyword直接忽略
        if (name == null || name.indexOf(keyword) > -1) {
            return name;
        }
        //只要keyword是true就要拼接
        //没配注解，但是类型是字符串，默认keyword是true
        if (esMapping == null) {
            if (field.getType() == String.class) {
                return name + keyword;
            }
        }
        //配了注解，但是类型是字符串，默认keyword是true
        else {
            if (esMapping.datatype() == DataType.text_type && esMapping.keyword() == true) {
                return name + keyword;
            }
        }
        return name;
    }

    @Override
    public double aggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return aggs(metricName, aggsType, queryBuilder, clazz, indexname);
    }

    @Override
    public double aggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception {
        String[] indexname = indexs;
        String me = aggsType.toString() + "_" + metricName.replaceAll(keyword, "");
        Field f_metric = clazz.getDeclaredField(metricName.replaceAll(keyword, ""));
        if (f_metric == null) {
            throw new Exception("metric field is null");
        }
        metricName = genKeyword(f_metric, metricName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.size(0);
        if (AggsType.count == aggsType) {
            searchSourceBuilder.aggregation(AggregationBuilders.count(me).field(metricName));
        } else if (AggsType.min == aggsType) {
            searchSourceBuilder.aggregation(AggregationBuilders.min(me).field(metricName));
        } else if (AggsType.max == aggsType) {
            searchSourceBuilder.aggregation(AggregationBuilders.max(me).field(metricName));
        } else if (AggsType.sum == aggsType) {
            searchSourceBuilder.aggregation(AggregationBuilders.sum(me).field(metricName));
        } else if (AggsType.avg == aggsType) {
            searchSourceBuilder.aggregation(AggregationBuilders.avg(me).field(metricName));
        }
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (AggsType.count == aggsType) {
            ValueCount count = searchResponse.getAggregations().get(me);
            long value = count.getValue();
            return Double.parseDouble(String.valueOf(value));
        } else if (AggsType.min == aggsType) {
            ParsedMin min = searchResponse.getAggregations().get(me);
            double value = min.getValue();
            return value;
        } else if (AggsType.max == aggsType) {
            ParsedMax max = searchResponse.getAggregations().get(me);
            double value = max.getValue();
            return value;
        } else if (AggsType.sum == aggsType) {
            ParsedSum sum = searchResponse.getAggregations().get(me);
            double value = sum.getValue();
            return value;
        } else if (AggsType.avg == aggsType) {
            ParsedAvg avg = searchResponse.getAggregations().get(me);
            double value = avg.getValue();
            return value;
        }
        return 0d;
    }

    @Override
    public Stats statsAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return statsAggs(metricName, queryBuilder, clazz, indexname);
    }

    @Override
    public Stats statsAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception {
        String[] indexname = indexs;
        String me = "stats";
        Field f_metric = clazz.getDeclaredField(metricName.replaceAll(keyword, ""));
        if (f_metric == null) {
            throw new Exception("metric field is null");
        }
        metricName = genKeyword(f_metric, metricName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.size(0);
        StatsAggregationBuilder aggregation = AggregationBuilders.stats(me).field(metricName);
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Stats stats = searchResponse.getAggregations().get(me);
        return stats;
    }

    @Override
    public Map<String, Stats> statsAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, String bucketName) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return statsAggs(metricName, queryBuilder, clazz, bucketName, indexname);
    }

    @Override
    public Map<String, Stats> statsAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, String... indexs) throws Exception {
        String[] indexname = indexs;
        Field f_metric = clazz.getDeclaredField(metricName.replaceAll(keyword, ""));
        Field f_bucket = clazz.getDeclaredField(bucketName.replaceAll(keyword, ""));
        if (f_metric == null) {
            throw new Exception("metric field is null");
        }
        if (f_bucket == null) {
            throw new Exception("bucket field is null");
        }
        metricName = genKeyword(f_metric, metricName);
        bucketName = genKeyword(f_bucket, bucketName);

        String by = "by_" + bucketName.replaceAll(keyword, "");
        String me = "stats" + "_" + metricName.replaceAll(keyword, "");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermsAggregationBuilder aggregation = AggregationBuilders.terms(by)
                .field(bucketName);
        //默认按照count的降序排序
        aggregation.order(BucketOrder.count(false));
        aggregation.subAggregation(AggregationBuilders.stats(me).field(metricName)).size(Constant.AGG_RESULT_COUNT);
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        Aggregations aggregations = searchResponse.getAggregations();
        Terms by_risk_code = aggregations.get(by);
        Map<String, Stats> map = new LinkedHashMap<>();
        for (Terms.Bucket bucket : by_risk_code.getBuckets()) {
            Stats stats = bucket.getAggregations().get(me);
            map.put(bucket.getKey().toString(), stats);
        }
        return map;
    }

    @Override
    public Aggregations aggs(AggregationBuilder aggregationBuilder, QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return aggs(aggregationBuilder, queryBuilder, clazz, indexname);
    }

    @Override
    public Aggregations aggs(AggregationBuilder aggregationBuilder, QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = indexs;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregationBuilder);
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        if (metaData.isPrintLog()) {
            logger.info(searchSourceBuilder.toString());
        }
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse.getAggregations();
    }

    @Override
    public long cardinality(String metricName, QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return cardinality(metricName, queryBuilder, Constant.DEFAULT_PRECISION_THRESHOLD , clazz , indexname);
    }

    @Override
    public long cardinality(String metricName, QueryBuilder queryBuilder, long precisionThreshold, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return cardinality(metricName, queryBuilder,precisionThreshold, clazz, indexname);
    }

    @Override
    public long cardinality(String metricName, QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception {
        return cardinality(metricName, queryBuilder, Constant.DEFAULT_PRECISION_THRESHOLD , clazz , indexs);
    }

    @Override
    public long cardinality(String metricName, QueryBuilder queryBuilder, long precisionThreshold, Class<T> clazz, String... indexs) throws Exception {
        String[] indexname = indexs;
        Field f_metric = clazz.getDeclaredField(metricName.replaceAll(keyword, ""));
        if (f_metric == null) {
            throw new Exception("metric field is null");
        }
        metricName = genKeyword(f_metric, metricName);
        String me = "cardinality_" + metricName.replaceAll(keyword, "");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        CardinalityAggregationBuilder aggregation = AggregationBuilders
                .cardinality(me)
                .field(metricName)
                .precisionThreshold(precisionThreshold)
                ;
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Cardinality agg = searchResponse.getAggregations().get(me);
        return agg.getValue();
    }

    @Override
    public Map<Double, Double> percentilesAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return percentilesAggs(metricName, queryBuilder, clazz, Constant.DEFAULT_PERCSEGMENT, indexname);
    }

    @Override
    public Map percentilesAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, double[] customSegment, String... indexs) throws Exception {
        Field f_metric = clazz.getDeclaredField(metricName.replaceAll(keyword, ""));
        if (f_metric == null) {
            throw new Exception("metric field is null");
        }
        if (customSegment == null) {
            throw new Exception("customSegment is null");
        } else if (customSegment.length == 0) {
            throw new Exception("customSegment is null");
        }
        metricName = genKeyword(f_metric, metricName);
        String me = "percentiles_" + metricName.replaceAll(keyword, "");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        PercentilesAggregationBuilder aggregation = AggregationBuilders.percentiles(me).field(metricName).percentiles(customSegment);
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexs);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Map<Double, Double> map = new LinkedHashMap<>();
        Percentiles agg = searchResponse.getAggregations().get(me);
        for (Percentile entry : agg) {
            double percent = entry.getPercent();
            double value = entry.getValue();
            map.put(percent, value);
        }
        return map;
    }

    @Override
    public Map percentileRanksAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, double... customSegment) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return percentileRanksAggs(metricName, queryBuilder, clazz, customSegment, indexname);
    }

    @Override
    public Map percentileRanksAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, double[] customSegment, String... indexs) throws Exception {
        String[] indexname = indexs;
        Field f_metric = clazz.getDeclaredField(metricName.replaceAll(keyword, ""));
        if (f_metric == null) {
            throw new Exception("metric field is null");
        }
        if (customSegment == null || customSegment.length == 0) {
            throw new Exception("customSegment is null");
        }
        metricName = genKeyword(f_metric, metricName);
        String me = "percentiles_" + metricName.replaceAll(keyword, "");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        PercentileRanksAggregationBuilder aggregation = AggregationBuilders.percentileRanks(me, customSegment).field(metricName);
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Map<Double, Double> map = new LinkedHashMap<>();
        PercentileRanks agg = searchResponse.getAggregations().get(me);
        for (Percentile entry : agg) {
            double percent = entry.getPercent();
            double value = entry.getValue();
            map.put(percent, value);
        }
        return map;
    }


    @Override
    public Map filterAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, FiltersAggregator.KeyedFilter... filters) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return filterAggs(metricName, aggsType, queryBuilder, clazz, filters, indexname);
    }

    @Override
    public Map filterAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, FiltersAggregator.KeyedFilter[] filters, String... indexs) throws Exception {
        String[] indexname = indexs;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (filters == null) {
            throw new NullPointerException();
        }
        Field f_metric = clazz.getDeclaredField(metricName.replaceAll(keyword, ""));
        if (f_metric == null) {
            throw new Exception("metric field is null");
        }
        metricName = genKeyword(f_metric, metricName);
        String me = aggsType.toString() + "_" + metricName.replaceAll(keyword, "");
        AggregationBuilder aggregation = AggregationBuilders.filters("filteragg", filters);
        searchSourceBuilder.size(0);
        if (AggsType.count == aggsType) {
            aggregation.subAggregation(AggregationBuilders.count(me).field(metricName));
        } else if (AggsType.min == aggsType) {
            aggregation.subAggregation(AggregationBuilders.min(me).field(metricName));
        } else if (AggsType.max == aggsType) {
            aggregation.subAggregation(AggregationBuilders.max(me).field(metricName));
        } else if (AggsType.sum == aggsType) {
            aggregation.subAggregation(AggregationBuilders.sum(me).field(metricName));
        } else if (AggsType.avg == aggsType) {
            aggregation.subAggregation(AggregationBuilders.avg(me).field(metricName));
        }
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Filters agg = searchResponse.getAggregations().get("filteragg");
        Map map = new LinkedHashMap();
        for (Filters.Bucket entry : agg.getBuckets()) {
            if (AggsType.count == aggsType) {
                ValueCount count = entry.getAggregations().get(me);
                long value = count.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.min == aggsType) {
                ParsedMin min = entry.getAggregations().get(me);
                double value = min.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.max == aggsType) {
                ParsedMax max = entry.getAggregations().get(me);
                double value = max.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.sum == aggsType) {
                ParsedSum sum = entry.getAggregations().get(me);
                double value = sum.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.avg == aggsType) {
                ParsedAvg avg = entry.getAggregations().get(me);
                double value = avg.getValue();
                map.put(entry.getKey(), value);
            }
        }
        return map;
    }

    @Override
    public Map histogramAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, double interval) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return histogramAggs(metricName, aggsType, queryBuilder, clazz, bucketName, interval, indexname);
    }

    @Override
    public Map histogramAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, double interval, String... indexs) throws Exception {
        String[] indexname = indexs;
        Field f_metric = clazz.getDeclaredField(metricName.replaceAll(keyword, ""));
        Field f_bucket = clazz.getDeclaredField(bucketName.replaceAll(keyword, ""));
        if (f_metric == null) {
            throw new Exception("metric field is null");
        }
        if (f_bucket == null) {
            throw new Exception("bucket field is null");
        }
        metricName = genKeyword(f_metric, metricName);
        bucketName = genKeyword(f_bucket, bucketName);
        String by = "by_" + bucketName.replaceAll(keyword, "");
        String me = aggsType.toString() + "_" + metricName.replaceAll(keyword, "");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        AggregationBuilder aggregation = AggregationBuilders.histogram(by).field(bucketName).interval(interval);
        searchSourceBuilder.size(0);
        if (AggsType.count == aggsType) {
            aggregation.subAggregation(AggregationBuilders.count(me).field(metricName));
        } else if (AggsType.min == aggsType) {
            aggregation.subAggregation(AggregationBuilders.min(me).field(metricName));
        } else if (AggsType.max == aggsType) {
            aggregation.subAggregation(AggregationBuilders.max(me).field(metricName));
        } else if (AggsType.sum == aggsType) {
            aggregation.subAggregation(AggregationBuilders.sum(me).field(metricName));
        } else if (AggsType.avg == aggsType) {
            aggregation.subAggregation(AggregationBuilders.avg(me).field(metricName));
        }
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        ParsedHistogram agg = searchResponse.getAggregations().get(by);
        Map map = new LinkedHashMap();
        for (Histogram.Bucket entry : agg.getBuckets()) {
            if (AggsType.count == aggsType) {
                ValueCount count = entry.getAggregations().get(me);
                long value = count.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.min == aggsType) {
                ParsedMin min = entry.getAggregations().get(me);
                double value = min.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.max == aggsType) {
                ParsedMax max = entry.getAggregations().get(me);
                double value = max.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.sum == aggsType) {
                ParsedSum sum = entry.getAggregations().get(me);
                double value = sum.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.avg == aggsType) {
                ParsedAvg avg = entry.getAggregations().get(me);
                double value = avg.getValue();
                map.put(entry.getKey(), value);
            }
        }
        return map;
    }

    @Override
    public Map dateHistogramAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, DateHistogramInterval interval) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return dateHistogramAggs(metricName, aggsType, queryBuilder, clazz, bucketName, interval, indexname);
    }

    @Override
    public Map dateHistogramAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, DateHistogramInterval interval, String... indexs) throws Exception {
        String[] indexname = indexs;
        Field f_metric = clazz.getDeclaredField(metricName.replaceAll(keyword, ""));
        Field f_bucket = clazz.getDeclaredField(bucketName.replaceAll(keyword, ""));
        if (f_metric == null) {
            throw new Exception("metric field is null");
        }
        if (f_bucket == null) {
            throw new Exception("bucket field is null");
        } else if (f_bucket.getType() != Date.class) {
            throw new Exception("bucket type is not support");
        }
        ESMapping esMapping = f_bucket.getAnnotation(ESMapping.class);
        if (esMapping != null && esMapping.datatype() != DataType.date_type) {
            throw new Exception("bucket type is not support");
        }
        metricName = genKeyword(f_metric, metricName);
        bucketName = genKeyword(f_bucket, bucketName);
        String by = "by_" + bucketName.replaceAll(keyword, "");
        String me = aggsType.toString() + "_" + metricName.replaceAll(keyword, "");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        AggregationBuilder aggregation = AggregationBuilders.dateHistogram(by).field(bucketName).dateHistogramInterval(interval);
        searchSourceBuilder.size(0);
        if (AggsType.count == aggsType) {
            aggregation.subAggregation(AggregationBuilders.count(me).field(metricName));
        } else if (AggsType.min == aggsType) {
            aggregation.subAggregation(AggregationBuilders.min(me).field(metricName));
        } else if (AggsType.max == aggsType) {
            aggregation.subAggregation(AggregationBuilders.max(me).field(metricName));
        } else if (AggsType.sum == aggsType) {
            aggregation.subAggregation(AggregationBuilders.sum(me).field(metricName));
        } else if (AggsType.avg == aggsType) {
            aggregation.subAggregation(AggregationBuilders.avg(me).field(metricName));
        }
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        ParsedDateHistogram agg = searchResponse.getAggregations().get(by);
        Map map = new LinkedHashMap();
        for (Histogram.Bucket entry : agg.getBuckets()) {
            if (AggsType.count == aggsType) {
                ValueCount count = entry.getAggregations().get(me);
                long value = count.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.min == aggsType) {
                ParsedMin min = entry.getAggregations().get(me);
                double value = min.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.max == aggsType) {
                ParsedMax max = entry.getAggregations().get(me);
                double value = max.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.sum == aggsType) {
                ParsedSum sum = entry.getAggregations().get(me);
                double value = sum.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.avg == aggsType) {
                ParsedAvg avg = entry.getAggregations().get(me);
                double value = avg.getValue();
                map.put(entry.getKey(), value);
            }
        }
        return map;
    }

    @Override
    public boolean deleteById(M id, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        if (StringUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        DeleteRequest deleteRequest = new DeleteRequest(indexname, indextype, id.toString());
        DeleteResponse deleteResponse = null;
        deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
            logger.info("INDEX DELETE SUCCESS");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public PageList<T> search(QueryBuilder queryBuilder, PageSortHighLight pageSortHighLight, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        if (pageSortHighLight == null) {
            throw new NullPointerException("PageSortHighLight不能为空!");
        }
        return search(queryBuilder, pageSortHighLight, clazz, indexname);
    }

    @Override
    public PageList<T> search(QueryBuilder queryBuilder, PageSortHighLight pageSortHighLight, Class<T> clazz, String... indexs) throws Exception {
        if (pageSortHighLight == null) {
            throw new NullPointerException("PageSortHighLight不能为空!");
        }
        Attach attach = new Attach();
        attach.setPageSortHighLight(pageSortHighLight);
        return search(queryBuilder,attach,clazz,indexs);
    }

    @Override
    public PageList<T> search(QueryBuilder queryBuilder, Attach attach, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        if (attach == null) {
            throw new NullPointerException("Attach不能为空!");
        }
        return search(queryBuilder, attach, clazz, indexname);
    }

    @Override
    public PageList<T> search(QueryBuilder queryBuilder, Attach attach, Class<T> clazz, String... indexs) throws Exception {
        if (attach == null) {
            throw new NullPointerException("Attach不能为空!");
        }
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        PageList<T> pageList = new PageList<>();
        List<T> list = new ArrayList<>();
        PageSortHighLight pageSortHighLight = attach.getPageSortHighLight();
        SearchRequest searchRequest = new SearchRequest(indexs);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        boolean highLightFlag = false;
        boolean idSortFlag= false;
        if(pageSortHighLight != null) {
            //设置当前页码
            pageList.setCurrentPage(pageSortHighLight.getCurrentPage());
            pageList.setPageSize(pageSortHighLight.getPageSize());
            //分页
            if (pageSortHighLight.getPageSize() != 0) {
                //search after不可指定from
                if(!attach.isSearchAfter()) {
                    searchSourceBuilder.from((pageSortHighLight.getCurrentPage() - 1) * pageSortHighLight.getPageSize());
                }
                searchSourceBuilder.size(pageSortHighLight.getPageSize());
            }
            //排序
            if (pageSortHighLight.getSort() != null) {
                Sort sort = pageSortHighLight.getSort();
                List<Sort.Order> orders = sort.listOrders();
                for (int i = 0; i < orders.size(); i++) {
                    if(orders.get(i).getProperty().equals("_id")){
                        idSortFlag = true;
                    }
                    searchSourceBuilder.sort(new FieldSortBuilder(orders.get(i).getProperty()).order(orders.get(i).getDirection()));
                }
            }
            //高亮
            HighLight highLight = pageSortHighLight.getHighLight();
            if (highLight != null && highLight.getHighLightList() != null && highLight.getHighLightList().size() != 0) {
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                if (!StringUtils.isEmpty(highLight.getPreTag()) && !StringUtils.isEmpty(highLight.getPostTag())) {
                    highlightBuilder.preTags(highLight.getPreTag());
                    highlightBuilder.postTags(highLight.getPostTag());
                }
                for (int i = 0; i < highLight.getHighLightList().size(); i++) {
                    highLightFlag = true;
                    // You can set fragment_size to 0 to never split any sentence.
                    //不对高亮结果进行拆分
                    highlightBuilder.field(highLight.getHighLightList().get(i), 0);
                }
                searchSourceBuilder.highlighter(highlightBuilder);
            }
        }
        //设定searchAfter
        if(attach.isSearchAfter()){
            if(pageSortHighLight == null || pageSortHighLight.getPageSize() == 0){
                searchSourceBuilder.size(10);
            }else{
                searchSourceBuilder.size(pageSortHighLight.getPageSize());
            }
            if(attach.getSortValues() != null && attach.getSortValues().length != 0) {
                searchSourceBuilder.searchAfter(attach.getSortValues());
            }
            //如果没拼_id的排序，自动添加保证排序唯一性
            if(!idSortFlag){
                Sort.Order order = new Sort.Order(SortOrder.ASC,"_id");
                pageSortHighLight.getSort().and(new Sort(order));
                searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));
            }
        }
        //TrackTotalHits设置为true，解除查询结果超出10000的限制
        if(attach.isTrackTotalHits()){
            searchSourceBuilder.trackTotalHits(attach.isTrackTotalHits());
        }

        //设定返回source
        if(attach.getExcludes()!= null || attach.getIncludes() != null){
            searchSourceBuilder.fetchSource(attach.getIncludes(),attach.getExcludes());
        }
        searchRequest.source(searchSourceBuilder);
        //设定routing
        if(!StringUtils.isEmpty(attach.getRouting())){
            searchRequest.routing(attach.getRouting());
        }
        if (metaData.isPrintLog()) {
            logger.info(searchSourceBuilder.toString());
        }
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            T t = JsonUtils.string2Obj(hit.getSourceAsString(), clazz);
            //将_id字段重新赋值给@ESID注解的字段
            correctID(clazz, t, (M)hit.getId());
            //替换高亮字段
            if (highLightFlag) {
                Map<String, HighlightField> hmap = hit.getHighlightFields();
                hmap.forEach((k, v) ->
                        {
                            try {
                                Object obj = mapToObject(hmap, clazz);
                                BeanUtils.copyProperties(obj, t, BeanTools.getNoValuePropertyNames(obj));
                            } catch (Exception e) {
                                logger.error("convert object error", e);
                            }
                        }
                );
            }
            list.add(t);
            //最后一条SearchAfter用于searchAfter
            pageList.setSortValues(hit.getSortValues());
        }

        pageList.setList(list);
        pageList.setTotalElements(hits.getTotalHits().value);
        if(pageSortHighLight != null && pageSortHighLight.getPageSize() != 0) {
            pageList.setTotalPages(getTotalPages(hits.getTotalHits().value, pageSortHighLight.getPageSize()));
        }
        return pageList;
    }

    private static Map<Class,String> classIDMap = new ConcurrentHashMap();

    /**
     * 将_id字段重新赋值给@ESID注解的字段
     * @param clazz
     * @param t
     * @param _id
     */
    private void correctID(Class clazz, T t, M _id){
        try{
            if(StringUtils.isEmpty(_id)){
                return;
            }
            if(classIDMap.containsKey(clazz)){
                Field field = clazz.getDeclaredField(classIDMap.get(clazz));
                field.setAccessible(true);
                //这里不支持非String类型的赋值，如果用默认的id，则id的类型一定是String类型的
                if(field.get(t) == null) {
                    field.set(t, _id);
                }
                return;
            }
            for (int i = 0; i < clazz.getDeclaredFields().length; i++) {
                Field field = clazz.getDeclaredFields()[i];
                field.setAccessible(true);
                if(field.getAnnotation(ESID.class) != null){
                    classIDMap.put(clazz,field.getName());
                    //这里不支持非String类型的赋值，如果用默认的id，则id的类型一定是String类型的
                    if(field.get(t) == null) {
                        field.set(t, _id);
                    }
                }
            }
        }catch (Exception e){
            logger.error("correctID error!",e);
        }
    }

    private Object mapToObject(Map map, Class<?> beanClass) throws Exception {
        if (map == null) {
            return null;
        }
        Object obj = beanClass.newInstance();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (map.get(field.getName()) != null && !StringUtils.isEmpty(map.get(field.getName()))) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                field.setAccessible(true);
                if (map.get(field.getName()) instanceof HighlightField && ((HighlightField) map.get(field.getName())).fragments().length > 0) {
                    field.set(obj, ((HighlightField) map.get(field.getName())).fragments()[0].string());
                }
            }
        }
        return obj;
    }

    @Override
    public List<T> searchTemplate(Map<String, Object> template_params, String templateName, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        SearchTemplateRequest request = new SearchTemplateRequest();
        request.setRequest(new SearchRequest(indexname));

        request.setScriptType(ScriptType.STORED);
        request.setScript(templateName);
        Map<String, Object> params = new HashMap<>();
        if(template_params != null){
            template_params.forEach((k,v) -> {
                params.put(k, v);
            });
        }
        request.setScriptParams(params);
        SearchTemplateResponse response = client.searchTemplate(request, RequestOptions.DEFAULT);
        SearchResponse searchResponse = response.getResponse();
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        List<T> list = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            T t = JsonUtils.string2Obj(hit.getSourceAsString(), clazz);
            list.add(t);
        }
        return list;
    }

    @Override
    public List<T> searchTemplateBySource(Map<String, Object> template_params, String templateSource, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String indexname = metaData.getIndexname();
        SearchTemplateRequest request = new SearchTemplateRequest();
        request.setRequest(new SearchRequest(indexname));
        request.setScriptType(ScriptType.INLINE);
        request.setScript(templateSource);
        Map<String, Object> scriptParams = new HashMap<>();
        if(template_params != null){
            template_params.forEach((k,v) -> {
                scriptParams.put(k, v);
            });
        }
        request.setScriptParams(scriptParams);
        SearchTemplateResponse response = client.searchTemplate(request, RequestOptions.DEFAULT);
        SearchResponse searchResponse = response.getResponse();
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        List<T> list = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            T t = JsonUtils.string2Obj(hit.getSourceAsString(), clazz);
            list.add(t);
        }
        return list;
    }

    @Override
    public Response saveTemplate(String templateName, String templateSource) throws Exception {
        Request scriptRequest = new Request("POST", "_scripts/"+templateName);
        scriptRequest.setJsonEntity(templateSource);
        Response scriptResponse = request(scriptRequest);
        return scriptResponse;
    }

    @Override
    public List<T> scroll(QueryBuilder queryBuilder, Class<T> clazz, Long time, String... indexs) throws Exception {
        if (queryBuilder == null) {
            queryBuilder = new MatchAllQueryBuilder();
        }
        List<T> list = new ArrayList<>();
        ScrollResponse<T> scrollResponse = createScroll(queryBuilder, clazz, time, 50);
        scrollResponse.getList().forEach(s -> list.add(s));
        String scrollId = scrollResponse.getScrollId();
        while (true){
            scrollResponse = queryScroll(clazz, time, scrollId);
            if(scrollResponse.getList() != null && scrollResponse.getList().size() != 0){
                scrollResponse.getList().forEach(s -> list.add(s));
                scrollId = scrollResponse.getScrollId();
            }else{
                break;
            }
        }
        return list;
    }

    @Override
    public ScrollResponse<T> createScroll(QueryBuilder queryBuilder, Class<T> clazz, Long time, Integer size) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String indexname = metaData.getIndexname();
        return createScroll(queryBuilder,clazz,time,size,indexname);
    }

    @Override
    public ScrollResponse<T> createScroll(QueryBuilder queryBuilder, Class<T> clazz, Long time, Integer size, String... indexs) throws Exception {
        if (queryBuilder == null) {
            queryBuilder = new MatchAllQueryBuilder();
        }
        String[] indexname = indexs;
        List<T> list = new ArrayList<>();
        Scroll scroll = new Scroll(TimeValue.timeValueHours(time));
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if(size == null || size == 0){
            searchSourceBuilder.size(Constant.DEFAULT_SCROLL_PERPAGE);
        }else{
            searchSourceBuilder.size(size);
        }
        searchSourceBuilder.query(queryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        //第一把查询出的结果
        for (SearchHit hit : searchHits) {
            T t = JsonUtils.string2Obj(hit.getSourceAsString(), clazz);
            //将_id字段重新赋值给@ESID注解的字段
            correctID(clazz, t, (M)hit.getId());
            list.add(t);
        }
        ScrollResponse<T> scrollResponse = new ScrollResponse(list,scrollId);
        return scrollResponse;
    }

    @Override
    public ScrollResponse<T> queryScroll(Class<T> clazz, Long time , String scrollId) throws Exception {
        SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
        Scroll scroll = new Scroll(TimeValue.timeValueHours(time));
        scrollRequest.scroll(scroll);
        SearchResponse searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
        scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        List<T> list = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            T t = JsonUtils.string2Obj(hit.getSourceAsString(), clazz);
            //将_id字段重新赋值给@ESID注解的字段
            correctID(clazz, t, (M)hit.getId());
            list.add(t);
        }
        ScrollResponse<T> scrollResponse = new ScrollResponse(list,scrollId);
        return scrollResponse;
    }


    @Override
    public List<T> scroll(QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return scroll(queryBuilder, clazz, Constant.DEFAULT_SCROLL_TIME, indexname);
    }

    @Override
    public List<String> completionSuggest(String fieldName, String fieldValue, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return completionSuggest(fieldName, fieldValue, clazz, indexname);
    }

    @Override
    public List<String> completionSuggest(String fieldName, String fieldValue, Class<T> clazz, String... indexs) throws Exception {
        String[] indexname = indexs;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        CompletionSuggestionBuilder completionSuggestionBuilder = new
                CompletionSuggestionBuilder(fieldName + ".suggest");
        completionSuggestionBuilder.text(fieldValue);
        completionSuggestionBuilder.skipDuplicates(true);
        completionSuggestionBuilder.size(Constant.COMPLETION_SUGGESTION_SIZE);
        suggestBuilder.addSuggestion("suggest_" + fieldName, completionSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Suggest suggest = searchResponse.getSuggest();
        if (suggest == null) {
            return null;
        }
        CompletionSuggestion completionSuggestion = suggest.getSuggestion("suggest_" + fieldName);
        List<String> list = new ArrayList<>();
        for (CompletionSuggestion.Entry entry : completionSuggestion.getEntries()) {
            for (CompletionSuggestion.Entry.Option option : entry) {
                String suggestText = option.getText().string();
                list.add(suggestText);
            }
        }
        return list;
    }

    @Override
    public List<String> phraseSuggest(String fieldName, String fieldValue, ElasticsearchTemplateImpl.PhraseSuggestParam param, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return phraseSuggest(fieldName, fieldValue, param, clazz, indexname);
    }



    @Override
    public List<String> phraseSuggest(String fieldName, String fieldValue, ElasticsearchTemplateImpl.PhraseSuggestParam param, Class<T> clazz, String... indexs) throws Exception {
        if(param == null){
            //没指定参数，传入默认参数
            param = new PhraseSuggestParam(5,0,null,"always");
        }
        String[] indexname = indexs;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        PhraseSuggestionBuilder phraseSuggestionBuilder = new PhraseSuggestionBuilder(fieldName);
        phraseSuggestionBuilder
                .text(fieldValue)
                .confidence(param.getConfidence())
                .size(Constant.COMPLETION_SUGGESTION_SIZE)
                .maxErrors(param.getMaxErrors())
                .addCandidateGenerator(new DirectCandidateGeneratorBuilder(fieldName).suggestMode(param.getSuggestMode()));
        if(param.getAnalyzer() != null) {
            phraseSuggestionBuilder.analyzer(param.getAnalyzer());
        }
        suggestBuilder.addSuggestion("suggest_" + fieldName, phraseSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);
        SearchRequest searchRequest = new SearchRequest(indexname);
        searchRequest.source(searchSourceBuilder);
        logger.info(searchSourceBuilder.toString());
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Suggest suggest = searchResponse.getSuggest();
        if (suggest == null) {
            return null;
        }
        PhraseSuggestion phraseSuggestion = suggest.getSuggestion("suggest_" + fieldName);
        List<String> list = new ArrayList<>();
        for (PhraseSuggestion.Entry entry : phraseSuggestion.getEntries()) {
            for (PhraseSuggestion.Entry.Option option : entry) {
                String suggestText = option.getText().string();
                list.add(suggestText);
            }
        }
        return list;
    }

    private int getTotalPages(long totalHits, int pageSize) {
        return pageSize == 0 ? 1 : (int) Math.ceil((double) totalHits / (double) pageSize);
    }

    public static class PhraseSuggestParam{
        private int maxErrors;
        private float confidence;
        private String analyzer;
        private String suggestMode;

        public PhraseSuggestParam(int maxErrors, float confidence, String analyzer, String suggestMode) {
            this.maxErrors = maxErrors;
            this.confidence = confidence;
            this.analyzer = analyzer;
            this.suggestMode = suggestMode;
        }

        public int getMaxErrors() {
            return maxErrors;
        }

        public void setMaxErrors(int maxErrors) {
            this.maxErrors = maxErrors;
        }

        public float getConfidence() {
            return confidence;
        }

        public void setConfidence(float confidence) {
            this.confidence = confidence;
        }

        public String getAnalyzer() {
            return analyzer;
        }

        public void setAnalyzer(String analyzer) {
            this.analyzer = analyzer;
        }

        public String getSuggestMode() {
            return suggestMode;
        }

        public void setSuggestMode(String suggestMode) {
            this.suggestMode = suggestMode;
        }
    }
}
