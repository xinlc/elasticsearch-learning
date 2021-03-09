package com.jd.controller;

import com.jd.service.ISearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


















import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @projectName: jd-search-api
 * @className: com.jd.controller.SearchController
 * @description: 搜索相关接口
 * @author: tong.li
 * @createTime: 2020/12/8 19:21
 * @version: v1.0
 * @copyright: 版权所有 © 李彤
 */
@RestController
@RequestMapping("/jd")
@Slf4j
public class SearchController {

    @Autowired
    private ISearchService searchService;

    /**
     * 搜索API GET请求
     * @param keywords   搜索关键字
     * @param pageNo     分页页码,不传默认查第一页
     * @param pageSize   分页页大小,不传默认查一页查30条
     * @param sortNumber 排序号(排序字段) 0-按默认评分排序,1-按评价数排序,2-按价格排序
     * @param isDesc     是否倒叙排序,默认倒序
     */
    @GetMapping("/search")
    public Map<String, Object> searchGoods(@RequestParam(required = false) String keywords,
                                @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                @RequestParam(required = false, defaultValue = "30") Integer pageSize,
                                @RequestParam(required = false, defaultValue = "0") Integer sortNumber,
                                @RequestParam(required = false, defaultValue = "true") Boolean isDesc) {
        log.info("搜索参数:{},{},{},{},{}",keywords, pageNo, pageSize, sortNumber, isDesc);
        // 为了程序严谨性,处理一下页码和页大小
        pageNo = pageNo <= 0 ? 1 : pageNo;
        pageSize = pageSize <= 0 ? 30 : pageSize;
        // 进行条件搜索
        Map<String, Object> rs = new LinkedHashMap<>();
        rs.put("timestamp", LocalDateTime.now());
        try {
            // 这里为了方便模拟真实项目开发，使用Map组装返回给前端，实际开发中是封装的泛型响应实体类为主
            rs.put("status", 200);
            rs.put("message", "搜索成功");
            rs.put("data",searchService.search(keywords, pageNo, pageSize,sortNumber,isDesc));
        } catch (Exception e) {
            rs.put("status", 500);
            rs.put("message", "服务器异常,请稍后再试");
            rs.put("data",null);
            log.error("搜索异常", e);
        }
        return rs;
    }
}
