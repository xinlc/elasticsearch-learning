package org.zxp.esclientrhl.auto.intfproxy;

import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.QueryBuilder;
import org.zxp.esclientrhl.enums.AggsType;
import org.zxp.esclientrhl.repository.PageList;
import org.zxp.esclientrhl.repository.PageSortHighLight;

import java.util.List;
import java.util.Map;

/**
 * program: esclientrhl
 * description:
 * author: X-Pacific zhang
 * create: 2019-09-02 17:29
 **/
public interface ESCRepository<T,M> {

    /**
     * 通过Low Level REST Client 查询
     * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/6.6/java-rest-low-usage-requests.html
     * @param request
     * @return
     * @throws Exception
     */
    public Response request(Request request) throws Exception;


    /**
     * 新增索引
     * @param t
     */
    public boolean save(T t) throws Exception;

    /**
     * 新增索引集合
     * @param list
     */
    public BulkResponse save(List<T> list) throws Exception;

    /**
     * 按照有值字段更新索引
     * @param t
     */
    public boolean update(T t) throws Exception;


    /**
     * 覆盖更新索引
     * @param t
     */
    public boolean updateCover(T t) throws Exception;


    /**
     * 删除索引
     * @param t
     */
    public boolean delete(T t) throws Exception;

    /**
     * 删除索引
     * @param id
     */
    public boolean deleteById(M id) throws Exception;

    /**
     * 根据ID查询
     * @param id
     * @return
     * @throws Exception
     */
    public T getById(M id) throws Exception;

    /**
     * 【最原始】查询
     * @param searchRequest
     * @return
     * @throws Exception
     */
    public SearchResponse search(SearchRequest searchRequest) throws Exception;


    /**
     * 非分页查询
     * 目前暂时传入类类型
     * @param queryBuilder
     * @return
     * @throws Exception
     */
    public List<T> search(QueryBuilder queryBuilder) throws Exception;


    /**
     * 查询数量
     * @param queryBuilder
     * @return
     * @throws Exception
     */
    public long count(QueryBuilder queryBuilder) throws Exception;


    /**
     * 支持分页、高亮、排序的查询
     * @param queryBuilder
     * @param pageSortHighLight
     * @return
     * @throws Exception
     */
    public PageList<T> search(QueryBuilder queryBuilder, PageSortHighLight pageSortHighLight) throws Exception;

    /**
     * 非分页查询，指定最大返回条数
     * 目前暂时传入类类型
     * @param queryBuilder
     * @param limitSize 最大返回条数
     * @return
     * @throws Exception
     */
    public List<T> searchMore(QueryBuilder queryBuilder,int limitSize) throws Exception;

    /**
     * 搜索建议
     * @param fieldName
     * @param fieldValue
     * @return
     * @throws Exception
     */
    public List<String> completionSuggest(String fieldName,String fieldValue) throws Exception;


    /**
     * 普通聚合查询
     * 以bucket分组以aggstypes的方式metric度量
     * @param bucketName
     * @param metricName
     * @param aggsType
     * @return
     */
    public Map aggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, String bucketName) throws Exception;

}
