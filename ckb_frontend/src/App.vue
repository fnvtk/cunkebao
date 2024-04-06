<template>
  <div>
    <div id="app" :class="{ noscroll: isNoScroll }" v-if="this.$route.path.indexOf('dataview') == -1">
      <router-view />
    </div>
    <div id="dataview" v-else>
      <datav />
    </div>
  </div>
</template>

<script>
import datav from './views/datav/index.vue'
export default {
  name: 'App',
  components: {
    datav
  },
  data: function () {
    return {
      isNoScroll: false
    }
  },
  methods: {
    setNoScroll(isNoScroll) {
      this.isNoScroll = isNoScroll
    }
  }
}
</script>
<style lang="scss" scoped>
.noscroll {
  overflow: hidden;
}

#dataview {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  width: 100%;
  height: 100%;
}
</style>
