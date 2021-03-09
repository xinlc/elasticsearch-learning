package org.zxp.esclientrhl.repository;

import java.util.List;

/**
 * program: esdemo
 * description: 分页对象封装
 * author: X-Pacific zhang
 * create: 2019-01-21 17:06
 **/
public class PageList<T> {
    List<T> list;

    private int totalPages = 0;

    private long totalElements = 0;

    private Object[] sortValues;

    private int currentPage;

    private int pageSize;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public Object[] getSortValues() {
        return sortValues;
    }

    public void setSortValues(Object[] sortValues) {
        this.sortValues = sortValues;
    }
}
