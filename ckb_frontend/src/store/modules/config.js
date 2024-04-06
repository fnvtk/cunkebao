import { loadConfig } from '@/api/config'
import axios from 'axios'

const state = {
  baseUrl: '',
  uploadUrl: '',
  systemTitle: ''
}

const mutations = {
  SET_STATE: (state, config) => {
    Object.assign(state, config)
  }
}

const actions = {
  loadConfig({ commit }) {
    axios.get('/config.json').then(res => {
      commit('SET_STATE', res.data)
    })
  },
  setConfig({ commit }, config) {
    commit('SET_STATE', config)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
