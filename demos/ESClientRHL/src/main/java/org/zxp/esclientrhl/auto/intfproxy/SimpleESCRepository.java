package org.zxp.esclientrhl.auto.intfproxy;

import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.context.ApplicationContext;
import org.zxp.esclientrhl.enums.AggsType;
import org.zxp.esclientrhl.repository.ElasticsearchTemplate;
import org.zxp.esclientrhl.repository.PageList;
import org.zxp.esclientrhl.repository.PageSortHighLight;

import java.util.List;
import java.util.Map;

/**
 * program: esclientrhl
 * description:
 * author: X-Pacific zhang
 * create: 2019-09-03 13:21
 **/
public class SimpleESCRepository<T,M> implements ESCRepository<T,M> {
    private Class<T> domainClass;
    private Class<M> idClass;

    private ApplicationContext applicationContext;
    private ElasticsearchTemplate elasticsearchTemplate = null;

    public SimpleESCRepository(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
    }

    private ElasticsearchTemplate getElasticsearchTemplate(){
        return applicationContext.getBean(ElasticsearchTemplate.class);
    }

    @Override
    public Response request(Request request) throws Exception {
        return getElasticsearchTemplate().request(request);
    }

    @Override
    public boolean save(T o) throws Exception {
        return getElasticsearchTemplate().save(o);
    }

    @Override
    public BulkResponse save(List<T> list) throws Exception {
        return getElasticsearchTemplate().save(list);
    }

    @Override
    public boolean update(T t) throws Exception {
        return getElasticsearchTemplate().update(t);
    }

    @Override
    public boolean updateCover(T t) throws Exception {
        return getElasticsearchTemplate().updateCover(t);
    }

    @Override
    public boolean delete(T t) throws Exception {
        return getElasticsearchTemplate().delete(t);
    }

    @Override
    public boolean deleteById(M id) throws Exception {
        return getElasticsearchTemplate().deleteById(id, domainClass);
    }

    @Override
    public T getById(M id) throws Exception {
        return (T)getElasticsearchTemplate().getById(id, domainClass);
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest) throws Exception {
        return getElasticsearchTemplate().search(searchRequest);
    }

    @Override
    public List<T> search(QueryBuilder queryBuilder) throws Exception {
        return getElasticsearchTemplate().search(queryBuilder, domainClass);
    }

    @Override
    public long count(QueryBuilder queryBuilder) throws Exception {
        return getElasticsearchTemplate().count(queryBuilder, domainClass);
    }

    @Override
    public PageList<T> search(QueryBuilder queryBuilder, PageSortHighLight pageSortHighLight) throws Exception {
        return getElasticsearchTemplate().search(queryBuilder, pageSortHighLight, domainClass);
    }

    @Override
    public List<T> searchMore(QueryBuilder queryBuilder, int limitSize) throws Exception {
        return getElasticsearchTemplate().searchMore(queryBuilder,limitSize,domainClass);
    }

    @Override
    public List<String> completionSuggest(String fieldName, String fieldValue) throws Exception {
        return getElasticsearchTemplate().completionSuggest(fieldName, fieldValue, domainClass);
    }

    @Override
    public Map aggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, String bucketName) throws Exception {
        return getElasticsearchTemplate().aggs(metricName, aggsType, queryBuilder, domainClass,bucketName);
    }


    public Class<T> getDomainClass() {
        return domainClass;
    }

    public void setDomainClass(Class<T> domainClass) {
        this.domainClass = domainClass;
    }

    public Class<M> getIdClass() {
        return idClass;
    }

    public void setIdClass(Class<M> idClass) {
        this.idClass = idClass;
    }

}
