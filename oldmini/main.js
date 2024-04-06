import App from './App'
import share from 'share.js'
Vue.mixin(share)
// #ifndef VUE3
import Vue from 'vue'
Vue.config.productionTip = false
Vue.prototype.$wxAppId = 'wx789850448e26c91d';
Vue.prototype.$wxApi = 'https://api.quwanzhi.com';
Vue.prototype.$settingData = [];
Vue.prototype.$userInfo = {},
App.mpType = 'app'
const app = new Vue({
    ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
export function createApp() {
  const app = createSSRApp(App)
  return {
    app
  }
}
// #endif