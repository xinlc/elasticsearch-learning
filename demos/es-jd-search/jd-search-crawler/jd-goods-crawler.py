#!/usr/bin python3.6
# -*- encoding: utf-8 -*-
"""
@File    : __init__.py.py
@Description : 爬取京东商品数据
@Author  : tong.li
@Email   : lt_alex@163.com
@Blog    : https://ltalex.gitee.io
@Time    : 2020/12/6 下午8:31
"""
import requests
from pyquery import PyQuery as pq
import time
from requests.exceptions import RequestException
import re
from elasticsearch import Elasticsearch
from elasticsearch import helpers
import json
import random

# 连接ElasticSearch
es = Elasticsearch(
    ['127.0.0.1'],
    port=9200
)


def request_page(url):
    try:
        # 设置请求头
        headers = {
            'User-Agent' : 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36'
        }
        datas = []
        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            text = response.text
            # 获取京东商品数据
            doc = pq(text)
            lis = doc('div#J_goodsList li[data-sku]').items()
            for index,li in enumerate(lis) : 
                goods =  {
                    # ID=微秒级时间戳+索引
                    'id': int(round(time.time() * 1000000)) + index,
                    #SKU
                    'sku': li.attr('data-sku'),
                    #  名称,正则替换掉不需要的空格
                    'title': re.sub('\\s{2,}|\\n*','',li('.p-name em').text()),
                    #  图片
                    'imgUrl': 'https:' + li('.p-img img').attr('data-lazy-img'),
                    # 价格
                    'price': float(0.0 if li('.p-price i').text() == '免费' else li('.p-price i').text()),
                    # 店铺名称
                    'shopName': li('.p-shop span').text(),
                    # 评价数,随机生成
                    'evaluationCount': random.randint(100,99999),
                     # 成交数,随机生成
                    'transactionsCount': random.randint(100,9999),
                    # 跳转到的商品详情地址
                    'detailUrl': 'https:' +  li('.p-img a').attr('href')
                }
                datas.append(goods)
            return datas
        return None
    except RequestException:
        return None

# 存储到:ElasticSearch
def storeToES(datas):
    actions = []
    for data in datas:
        action = {
            '_index':'jd_goods', #索引名称
            '_source': data
        }
        actions.append(action)
    helpers.bulk(es, actions)

def getData(url):
    datas = request_page(url)
    # 往ES批量插入数据
    if len(datas) != 0 : {
        storeToES(datas)
    }
    print(json.dumps(datas,ensure_ascii=False))
    print('-' * 200)


if __name__ == '__main__':
    # 爬取京东商品数据
    keywords = "手机" # 关键字信息，相当于京东搜索输入框
    url = 'https://search.jd.com/Search?keyword='+keywords+'&enc=utf-8&page='
    for i in range(100):
        getData(url + str(i+1))
        # 京东有反爬虫限制,爬的太多会有IP或验证码限制,等待0.5毫秒再次请求
        time.sleep(0.5)