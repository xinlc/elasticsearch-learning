<template>
  <div class="page">
    <div id="mallPage" class=" mallist tmall- page-not-market ">
        <!-- 头部搜索 -->
        <div id="header" class=" header-list-app">
            <div class="headerLayout">
                <div class="headerCon ">
                    <!-- Logo-->
                    <h1 id="mallLogo">
                        <img th:src="../static/images/jdlogo.png" alt="">
                    </h1>

                    <div class="header-extra">

                        <!--搜索-->
                        <div id="mallSearch" class="mall-search">
                            <form name="searchTop" class="mallSearch-form clearfix">
                                <fieldset>
                                    <legend>天猫搜索</legend>
                                    <div class="mallSearch-input clearfix">
                                        <div class="s-combobox" id="s-combobox-685">
                                            <div class="s-combobox-input-wrap">
                                                <input type="text" autocomplete="off" v-model="params.keywords" id="mq"
                                                       class="s-combobox-input" aria-haspopup="true" placeholder="请输入关键字">
                                            </div>
                                        </div>
                                        <button type="submit" id="searchbtn"  @click.prevent="doSearch(params.keywords)">搜索</button>
                                    </div>
                                </fieldset>
                            </form>
                            <ul class="relKeyTop">
                                <li><a>彤哥相机专场</a></li>
                                <li><a>彤哥聊Java</a></li>
                                <li><a>彤哥摄影大讲堂</a></li>
                                <li><a>彤哥电脑修理铺</a></li>
                                <li><a>彤哥图书</a></li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 商品详情页面 -->
        <div id="content">
            <div class="main">
                <!-- 品牌分类 -->
                <form class="navAttrsForm">
                    <div class="attrs j_NavAttrs" style="display:block">
                        <div class="brandAttr j_nav_brand">
                            <div class="j_Brand attr">
                                <div class="attrKey">
                                    你可能要搜
                                </div>
                                <div class="attrValues">
                                    <ul class="av-collapse row-2">
                                        <li v-for="brand in brandList" :key="brand.id" @click.prevent="doSearch(brand.name)">
                                            <a href="#">{{brand.name}}</a>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </form>

                <!-- 排序规则 -->
                <div class="filter clearfix">
                    <span v-for="sortItem in sortData" :key="sortItem.id" @click="doSortSearch(sortItem.id,sortItem.defaultDesc)">
                        <a class="fSort" :class="{'fSort-cur': sortItem.id == defaultSortNumber}">
                        {{sortItem.name}}
                        <span v-if="sortItem.id > 1">
                            <!-- 排序上标志，阻止向父级冒泡 -->
                            <i class="f-ico-triangle-mt" :style="sortViewFlag==1 ? {'border-bottom': '4px solid red'} : {}" @click.stop="doSortSearch(sortItem.id,false)" ></i>
                            <!-- 排序下标志，阻止向父级冒泡 -->
                            <i class="f-ico-triangle-mb" :style="sortViewFlag==2 ? {'border-top': '4px solid red'} : {}"  @click.stop="doSortSearch(sortItem.id,true)"></i>
                        </span>
                        <span v-else>
                           <i class="f-ico-arrow-d"></i>
                        </span>
                    </a>
                    </span>
                </div>
                <!-- 商品详情，滚动加载 -->
                <div class="view grid-nosku" infinite-scroll-disabled="disabled" v-infinite-scroll="loadMore" v-if="jdGoodsList !== null && 'list' in jdGoodsList && jdGoodsList !== null">
                    <div class="product"  v-for="goods in jdGoodsList.list" :key="goods.id">
                        <a :href="goods.detailUrl">
                            <div class="product-iWrap">
                                <!--商品封面-->
                                <div class="productImg-wrap">
                                    <a class="productImg">
                                        <img :src="goods.imgUrl">
                                    </a>
                                </div>
                                <!--价格-->
                                <p class="productPrice">
                                    <em><b>¥</b>{{goods.price}}</em>
                                </p>
                                <!--标题-->
                                <p class="productTitle">
                                    <!-- 使用v-html原因是高亮渲染 -->
                                    <a  v-html="goods.title"></a>
                                </p>
                                <!-- 店铺名 -->
                                <div class="productShop">
                                    <span>店铺：{{goods.shopName}} </span>
                                </div>
                                <!-- 成交信息 -->
                                <p class="productStatus">
                                    <span>月成交<em>{{goods.transactionsCount}}笔</em></span>
                                    <span>评价 <a>{{goods.evaluationCount}}</a></span>
                                </p>
                            </div>
                         </a>
                    </div>
                </div>
                <div>
                    <p v-if="this.loading">加载中...</p>
                    <div><p v-if="this.noMore">没有更多了</p></div>
                </div>
            </div>
        </div>
    </div>
    <!-- 回到顶部 -->
    <el-backtop target="#mallPage" :bottom="100">
        <div class="back-top" >UP</div>
    </el-backtop>
</div>
</template>

<script>
export default {
  name: 'Search',
  data () {
    return {
      sortData: JSON.parse(localStorage.getItem('sortData')),
      brandList: JSON.parse(localStorage.getItem('initBrandList')),
      params: {
        keywords: '',
        pageNo: 1,
        pageSize: 30,
        sortNumber: 0,
        isDesc: true
      },
      jdGoodsList: JSON.parse(localStorage.getItem('initGoodsData')),
      defaultSortNumber: 0,
      sortViewFlag: 0,
      loading: false
    }
  },
  props: {
  },
  methods: {
    async doSearch (keywords, sc) {
      console.log(typeof (sc) === 'undefined')
      if (typeof (sc) === 'undefined') {
        this.params.pageNo = 1
      }
      this.params.keywords = keywords
      // 请求后端接口进行搜索操作
      const { data: rs } = await this.$http.get('/search?' + this.$qs.stringify(this.params))
      if (rs.status !== 200) {
        // 如果请求失败，进行弹框
        return this.$message.error(rs.message)
      }
      if (sc === true) {
        this.jdGoodsList.list = this.jdGoodsList.list.concat(rs.data.list)
        return
      }
      this.jdGoodsList = rs.data
    },
    async doSortSearch (id, defaultDesc) {
      // 排序字段置为选中
      if (id === 2) {
        // 排序箭头置为选中
        this.sortViewFlag = !defaultDesc ? 1 : 2
        defaultDesc = (this.sortViewFlag === 2)
      }
      this.defaultSortNumber = id
      // 重新设置排序号和排序规则
      this.params.sortNumber = id
      this.params.isDesc = defaultDesc
      // 执行搜索
      this.doSearch(this.params.keywords)
    },
    /**
     * 滚动加载
     */
    loadMore () {
      this.loading = true
      setTimeout(() => {
        // 页码+1
        this.params.pageNo += 1
        this.loading = false
        if (this.params.pageNo > this.jdGoodsList.pages) {
          // 加载完成所有数据后，将页码置为初始值1
          this.params.pageNo = 1
          return
        }
        // 执行搜索
        this.doSearch(this.params.keywords, true)
      }, 2000)
    }
  },
  watch: {
    'params.keywords': async function (newVal) {
      // 若不搜索，则默认显示LocalStorage存储的商品信息
      if (newVal === '') {
        // 执行搜索
        this.doSearch(newVal)
      }
    }
  },
  computed: {
    noMore () {
      return this.params.pageNo === this.jdGoodsList.pages
    },
    disabled () {
      return this.loading || this.noMore
    }
  }
}
</script>
<style lang="scss" scope>
#mallPage {
    height: 100vh;
    overflow-x: hidden;
}
.back-top {
    height: 100%;
    width: 100%;
    background-color: #f2f5f6;
    box-shadow: 0 0 6px rgba(0,0,0, .12);
    text-align: center;
    line-height: 40px;
    color: #1989fa;
}
</style>
