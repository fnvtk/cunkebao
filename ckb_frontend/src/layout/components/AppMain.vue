<template>
  <section class="app-main">
    <router-view :key="key" />
  </section>
</template>

<script>
export default {
  name: 'AppMain',
  data: function() {
    return {
      app: null
    }
  },
  computed: {
    // 需要缓存的页面 固钉
    cachedViews() {
      return this.$store.state.tagsView.cachedViews
    },
    key() {
      this.app = this.$parent
      while (this.app) {
        if (this.app.$options.name === 'App') {
          break
        }
        this.app = this.app.$parent
      }
      this.app.setNoScroll(false)

      return this.$route.path
    }
  }
}
</script>

<style scoped>
.app-main {
  /*50 = navbar  */
  min-height: calc(100vh - 84px);
  /* width: 100%;
  position: relative;
  overflow: hidden; */
}
.fixed-header + .app-main {
  padding-top: 84px;
}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 15px;
  }
}
</style>
