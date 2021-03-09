package com.jd.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.jd.entity.GoodsDetail;
import com.jd.service.ISearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @projectName: jd-search-api
 * @className: com.jd.service.impl.SearchServiceImpl
 * @description: 搜索实现逻辑层
 * @author: tong.li
 * @createTime: 2020/12/8 1:942
 * @version: v1.0
 * @copyright: 版权所有 © 李彤
 */
@Service
public class SearchServiceImpl implements ISearchService  {

    /**
     * 京东商品数据索引名称
     */
    private static final String JD_GOODS_INDEX_NAME = "jd_goods";

    /**
     * title字段名
     */
    private static final String FILED_NAME_TITLE = "title";

    /**
     * Jackson序列化
     */
    @Autowired
    private ObjectMapper objectMapper;

    private static Map<Integer, String> sortFiledMap;

    static {
        sortFiledMap = new HashMap<Integer, String>() {
            {
                // 人气按评价数排序
                put(1,"evaluationCount");
                // 按价格排序
                put(2,"price");
            }
        };
    }

    /**
     * 官方建议使用ElasticSearch高级的Rest客户端
     */
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public PageInfo search(String keywords, Integer pageNo, Integer pageSize,Integer sortNumber, Boolean isDesc) throws Exception {
        // 存放搜索后的数据
        List<GoodsDetail> data = new ArrayList<>();
        // 构建搜索请求
        SearchRequest searchRequest = new SearchRequest(JD_GOODS_INDEX_NAME);
        // 构建SearchSourceBuilder方便查询
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 分页设置，from是从哪个索引查，size是大小
        sourceBuilder.from((pageNo-1) * pageSize);
        sourceBuilder.size(pageSize);
        // 设置排序，若sortNumber小于等于0，则是默认score排序，若大于1，则按指定字段排序
        if (sortNumber > 0 && sortNumber <= 2) {
            // 这里需要注意，字段进行聚合和排序操作时，如果字段类型是Text类型的,会报错。
            // 由于ES默认情况下会禁用Text字段优化，因此无法进行聚合或排序。若想启用,请将字段的fielddata设置为true，通过取消反转索引来加载字段数据
            // 这种处理方法会占用大量内存,ES官方不建议这样做，建议更改要聚合或排序字段的字段类型
            // ES默认score排序,若使用其他字段排序,score是获取不到的
            sourceBuilder.sort(sortFiledMap.get(sortNumber) ,isDesc ? SortOrder.DESC : SortOrder.ASC);
        }

        // 查询keywords
        if (!ObjectUtils.isEmpty(keywords)) {
            MatchQueryBuilder titleQueryBuilder = QueryBuilders.matchQuery(FILED_NAME_TITLE, keywords);
            sourceBuilder.query(titleQueryBuilder);
        }
        // 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title") // 设置高亮字段
                .preTags("<span style='color:red'>")      // 设置前置标签以及样式
                .postTags("</span>")    // 设置闭合标签以及样式
                .highlighterType("unified") // 设置高亮类型
                .requireFieldMatch(false); // 关闭多个字段高亮
        sourceBuilder.highlighter(highlightBuilder);
        // 设置超时时间为60秒
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // 执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 获取结果
        SearchHits hits = searchResponse.getHits();
        // 创建分页对象
        PageInfo<GoodsDetail> pageInfo = new PageInfo<>(data);
        // 设置分页数据
        pageInfo.setPageNum(pageNo);
        pageInfo.setPageSize(pageSize);
        if (hits == null || hits.getTotalHits().value == 0) {
            return pageInfo;
        }
        // 解析结果
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            GoodsDetail goodsDetail = objectMapper.readValue(sourceAsString, GoodsDetail.class);
            // 获取高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = null;
            if (!CollectionUtils.isEmpty(highlightFields)) {
                title = highlightFields.get(FILED_NAME_TITLE);
            }
            if (title != null) {
                // 如果存在高亮字段，解析高亮字段并将原来的字段值覆盖掉
                Text[] fragments = title.getFragments();
                if (fragments != null && fragments.length > 0) {
                    goodsDetail.setTitle(fragments[0].string());
                }
            }
            // 设置排名
            goodsDetail.setScore(Float.isNaN(hit.getScore()) ? 0.0f : hit.getScore());
            data.add(goodsDetail);

        }
        // 获取总数
        long total = hits.getTotalHits().value;
        pageInfo.setTotal(total);
        pageInfo.setList(data);
        // 当前页的数量
        pageInfo.setSize(data.size());
        // 总共多少页
        pageInfo.setPages(total== 0 ? 0: (int) (total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1));
        // 是否有下一页
        pageInfo.setHasNextPage(pageInfo.getPageNum() < pageInfo.getPages());
        // 是否有上一页
        pageInfo.setHasPreviousPage(pageInfo.getPageNum() > 1 &&  pageInfo.getPageNum() <= pageInfo.getPages() );
        // 是否为第一页
        pageInfo.setIsFirstPage(pageInfo.getPageNum() == 1);
        // 是否为最后一页
        pageInfo.setIsLastPage(pageInfo.getPageNum() == pageInfo.getPages());
        return pageInfo;
    }
}
