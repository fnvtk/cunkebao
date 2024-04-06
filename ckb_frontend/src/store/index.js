import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'
import app from './modules/app'
import settings from './modules/settings'
import user from './modules/user'
import tagsView from './modules/tagsView'
import config from './modules/config'
Vue.use(Vuex)

const store = new Vuex.Store({
  // state:{
  //     // 存储token
  //   Authorization: localStorage.getItem('Authorization') ? localStorage.getItem('Authorization') : ''

  // },
  // mutations: {
  //   // 修改token，并将token存入localStorage
  //   changeLogin (state, user) {  //这里的state对应上面状态state
  //     state.Authorization = user.Authorization;
  //     localStorage.setItem('Authorization', user.Authorization);
  //   }
  // },
  modules: {
    app,
    settings,
    tagsView,
    user,
    config
  },
  getters
})

export default store
