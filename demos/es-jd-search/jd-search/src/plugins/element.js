import Vue from 'vue'
import { Message, Backtop } from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import infiniteScroll from 'vue-infinite-scroll'

// 挂载滚动加载组件
Vue.use(infiniteScroll)
// 挂载回到顶部组件
Vue.use(Backtop)
// 消息组件挂载
Vue.prototype.$message = Message
