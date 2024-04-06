<template>
  <el-breadcrumb class="app-breadcrumb">
    <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-if="$route.path !== '/dashboard'"
                        :to="to">{{ objectconfig.sName }}</el-breadcrumb-item>
    <el-breadcrumb-item v-if="$route.path !== '/dashboard'">{{ curr.name }}</el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script>
import qs from 'qs'
import { configobj } from '@/api/commonobject'
export default {
  data() {
    return {
      to: '',
      objectconfig: {},
      sObjectName: '',
      curr: null
    }
  },
  watch: {
    $route() {
      this.getBreadcrumb()
    },
    to() {
      document.title = this.curr.name + '-' + this.objectconfig.sName + '-' + this.$store.state.config.systemTitle
    }
  },
  created() {
    this.getBreadcrumb()
  },
  methods: {
    getBreadcrumb() {
      // only show routes with meta.title
      const matched = this.$route.matched.filter(item => item.name)
      this.curr = matched[0]

      this.sObjectName = this.$route.path.split('/')[1] + '/' + this.$route.path.split('/')[2]

      if (this.$route.path == '/dashboard') {
        return
      }

      // 获取对象配置
      const data = {
        sobjectname: this.sObjectName
      }
      configobj(qs.stringify(data), this.sObjectName).then((res) => {
        this.objectconfig = res.data.sysobject
        this.to = { path: '/' + this.objectconfig.sObjectName.toLowerCase() + '/home' }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;

  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
}
</style>
