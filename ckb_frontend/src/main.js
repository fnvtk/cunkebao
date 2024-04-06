import Vue from 'vue'

import 'normalize.css/normalize.css' // A modern alternative to CSS resets

import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import locale from 'element-ui/lib/locale/lang/en' // lang i18n

import '@/styles/index.scss' // global css
import '@/assets/icon/iconfont.css'

import App from './App'
import store from './store'
import router from './router'

import '@/icons' // icon
import '@/permission' // permission control

import request from '@/utils/request'
import axios from 'axios'
import directives from './directives'

import dataV from '@jiaminghi/data-view'
Vue.use(dataV)

import VueElementUISkeleton from 'vue-elementui-skeleton'
Vue.use(VueElementUISkeleton, {
  directiveName: 'skeleton',
  rows: 10,
  radius: 3
})

/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online ! ! !
 */
if (process.env.NODE_ENV === 'production') {
  const { mockXHR } = require('../mock')
  mockXHR()
}

// set ElementUI lang to EN
// Vue.use(ElementUI, { locale });
// 如果想要中文版 element-ui，按如下方式声明
Vue.use(ElementUI)

Vue.use(directives)

Vue.config.productionTip = false

import * as echarts from 'echarts'
Vue.prototype.$echarts = echarts

// 在这里进行二次开发，支持外部config文件的引入
// Mars，2021年7月21日08:53:26
var appVue
axios.get('/config.json').then(res => {
  appVue = new Vue({
    el: '#app',
    router,
    store,
    render: h => h(App)
  })

  appVue.$store.dispatch('config/setConfig', res.data)

  // set page title
  document.title = res.data.systemTitle

  // 设置request的前缀URL
  // request是单例，所以允许这样赋值
  request.defaults.baseURL = res.data.baseUrl
})
