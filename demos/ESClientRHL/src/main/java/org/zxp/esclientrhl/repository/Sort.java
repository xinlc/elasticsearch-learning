package org.zxp.esclientrhl.repository;


import org.elasticsearch.search.sort.SortOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * program: esdemo
 * description: 排序对象封装
 * author: X-Pacific zhang
 * create: 2019-01-21 17:16
 **/
public class Sort {
    private List<Order> orders = null;


    public List<Order> listOrders(){
        return orders;
    }


    public Sort(Sort.Order... ods) {
        orders = new ArrayList<>();
        for (Order od : ods) {
            orders.add(od);
        }
    }

    public Sort and(Sort sort) {
        if(orders == null){
            orders = new ArrayList<>();
        }
        sort.orders.forEach(order -> orders.add(order));
        return this;
    }

    public static class Order implements Serializable {
        private final SortOrder direction;
        private final String property;

        public Order(SortOrder direction,String property){
            this.direction = direction;
            this.property = property;
        }

        public SortOrder getDirection() {
            return direction;
        }

        public String getProperty() {
            return property;
        }
    }
}
