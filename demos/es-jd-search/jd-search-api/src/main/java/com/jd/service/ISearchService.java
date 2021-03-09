package com.jd.service;

import com.github.pagehelper.PageInfo;


/**
 * @projectName: jd-search-api
 * @className: com.jd.service.impl.ISearchService
 * @description: 搜索API接口抽象层
 * @author: tong.li
 * @createTime: 2020/12/8 19:41
 * @version: v1.0
 * @copyright: 版权所有 © 李彤
 */
public interface ISearchService {

    PageInfo search(String keywords, Integer pageNo, Integer pageSize,Integer sortNumber,  Boolean isDesc) throws Exception;

}
