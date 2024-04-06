<template>
  <span>
    <el-dialog :title="title"
               width="1100px"
               top="50px"
               append-to-body
               :visible.sync="visible">
      <list-table v-if="visible"
                  ref="listtable"
                  :tabid="null"
                  :listid="1544"
                  :opera="opera"
                  :objectconfig="objectconfig"
                  listtype="list"
                  :sobjectname="sobjectname"
                  :defaultsearchjson="defaultsearchjson" />
    </el-dialog>
  </span>
</template>
<script>
import qs from 'qs'
import { tablist, configobj } from '@/api/commonobject'
export default {
  name: 'WechatAppend',
  components: {
    ListTable: () => import('@/components/ListTable')
  },
  props: ['data'],
  data() {
    return {
      visible: false,
      opera: null,
      objectconfig: null,
      sobjectname: "Business/WeChat",
      defaultsearchjson: {
      },
      title: "微信列表"
    }
  },
  mounted() {
    const data = {
      sobjectname: this.sobjectname
    }
    configobj(qs.stringify(data), this.sobjectname.toLowerCase()).then((res) => {
      this.objectconfig = res.data.sysobject
      this.opera = res.data.opera
      this.opera.push('refresh')
    })
  },
  methods: {
    show() {
      this.visible = true
    },
    hide() {
      this.visible = true
    }
  }
}
</script>
<style lang="scss" scoped>
a {
  color: #337ab7;
}

a:hover {
  text-decoration: underline;
}
</style>