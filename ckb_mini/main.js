// #ifndef VUE3
import Vue from 'vue'
import App from './App'
import config from './config.js'


import authMixin from './mixins/auth-mixin';
Vue.mixin(authMixin);

Vue.config.productionTip = false
Vue.prototype.$config = config

//兼容旧版
Vue.prototype.$wxAppId = 'wx789850448e26c91d';
Vue.prototype.$wxApi = 'https://api.quwanzhi.com';
Vue.prototype.$settingData = [];
Vue.prototype.$userInfo = {};


import uView from "uview-ui";
Vue.use(uView);

App.mpType = 'app'

const app = new Vue({
	...App
})
app.$mount()
// #endif

// #ifdef VUE3
import {
	createSSRApp
} from 'vue'
import App from './App.vue'
export function createApp() {
	const app = createSSRApp(App)
	return {
		app
	}
}
// #endif