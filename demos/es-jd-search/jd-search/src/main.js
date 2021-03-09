import Vue from 'vue'
import App from './App.vue'
import router from './router'
// 导入CSS样式
import './assets/css/style.css'
// 导入axios
import axios from 'axios'
// 导入qs用于url序列化
import qs from 'qs'
// 导入Element-Ui组件
import './plugins/element.js'

// 配置请求根目录
axios.defaults.baseURL = 'http://localhost:9000/jd'
// 给Vue挂载axios
Vue.prototype.$http = axios
Vue.prototype.$qs = qs

Vue.config.productionTip = false

new Vue({
  // 通过mounted生命周期钩子初始化数据
  mounted: async function () {
    // 初始化品牌数据
    const initBrandList = [
      {
        id: 1,
        name: '佳能'
      }, {
        id: 2,
        name: '尼康'
      },
      {
        id: 3,
        name: '索尼'
      },
      {
        id: 4,
        name: '哈苏'
      },
      {
        id: 5,
        name: '富士'
      },
      {
        id: 6,
        name: '莱卡'
      },
      {
        id: 7,
        name: '松下'
      },
      {
        id: 8,
        name: '大疆'
      },
      {
        id: 9,
        name: '适马'
      },
      {
        id: 10,
        name: '松典'
      }
    ]
    // 初始化排序数据
    const sortData = [
      {
        id: 0,
        name: '综合',
        defaultDesc: 'true'
      },
      {
        id: 1,
        name: '人气',
        defaultDesc: 'true'
      },
      {
        id: 2,
        name: '价格',
        defaultDesc: 'false'
      }
    ]
    const { data: rs } = await this.$http.get('/search?' + this.$qs.stringify(this.params))
    if (rs.status !== 200) {
      // 如果请求失败，进行弹框
      return this.$message(rs.message)
    }
    const initGoodsData = rs.data
    // 注释掉假数据
    // 初始化商品数据
    // const initGoodsData = {
    //   // 页码
    //   pageNum: 1,
    //   // 页大小
    //   pageSize: 30,
    //   // 当前页查询的列表个数
    //   size: 3,
    //   // 查询总数
    //   total: 3,
    //   // 总页数
    //   pages: 1,
    //   // 数据
    //   list: [
    //     {
    //       id: 1,
    //       sku: '71806504602',
    //       title: '佳能（Canon）EOS R5 全画幅专微旗舰 vlog微单相机 8K视频拍摄 微5 EOS R5',
    //       imgUrl: 'https://img12.360buyimg.com/n7/jfs/t1/147160/31/16416/248743/5fc60696E78885288/b3a05bc106140bfa.jpg',
    //       price: 26499.00,
    //       shopName: '彤哥哥相机铺',
    //       evaluationCount: 345,
    //       transactionsCount: 578,
    //       detailUrl: 'https://item.jd.com/71806504602.html'
    //     },
    //     {
    //       id: 2,
    //       sku: '57690437524',
    //       title: '索尼（SONY）a7r4/7RIV/ILCE-7RM4全画幅专业微单相机 单机身（不含镜头） ',
    //       imgUrl: 'https://img14.360buyimg.com/n7/jfs/t1/146148/22/17013/313056/5fc9c999Ee3f824b0/5dfa0f48b6389d03.jpg',
    //       price: 19185.00,
    //       shopName: '彤哥哥相机铺',
    //       evaluationCount: 781,
    //       transactionsCount: 233,
    //       detailUrl: 'https://item.jd.com/57690437524.html'
    //     },
    //     {
    //       id: null,
    //       sku: '10024727923051',
    //       title: '拍拍华为HUAWEIP40Pro+5G手机华为二手手机大陆国行陶瓷白8G+256G',
    //       imgUrl: 'https://img11.360buyimg.com/n7/jfs/t1/155325/20/8861/135429/5fced890Eccce267c/dc50f6ee27eb47ff.jpg',
    //       price: '5599.00',
    //       shopName: '拍拍二手官方旗舰店',
    //       evaluationCount: 16224,
    //       transactionsCount: 677,
    //       detailUrl: 'https://item.jd.com/10024727923051.html'
    //     }
    //   ]
    // }
    localStorage.setItem('initGoodsData', JSON.stringify(initGoodsData))
    localStorage.setItem('initBrandList', JSON.stringify(initBrandList))
    localStorage.setItem('sortData', JSON.stringify(sortData))
  },
  // 通过mounted生命周期钩子清除初始化数据
  destroyed: function () {
    localStorage.removeItem('initGoodsData')
    localStorage.removeItem('brandList')
    localStorage.removeItem('sortData')
  },
  router,
  render: h => h(App)
}).$mount('#app')
