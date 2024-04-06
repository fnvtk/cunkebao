<template>
  <div class="view-item">
    <a
      href="javascript:;"
      @click="handleClick()"
    >{{ data.lWechatAmt }}</a>
    <el-dialog
      :title="title"
      width="900px"
      top="200px"
      append-to-body
      :visible.sync="visible"
    >
      <list-table
        v-if="visible"
        ref="listtable"
        :tabid="null"
        :listid="1543"
        :opera="opera"
        :objectconfig="objectconfig"
        listtype="list"
        sobjectname="Business/AccountWechat"
        :defaultsearchjson="defaultsearchjson"
      />
    </el-dialog>
  </div>
</template>
<script>
import qs from 'qs'
import { configobj } from '@/api/commonobject'
export default {
  name: 'WechatAmtLink',
  components: {
    ListTable: () => import('@/components/ListTable')
  },

  props: ['data'],
  data() {
    return {
      visible: false,
      opera: null,
      objectconfig: null,
      sobjectname: 'Business/AccountWechat',
      defaultsearchjson: {
        AccountId: 0
      },
      title: ''
    }
  },
  mounted() {
    const data = {
      sobjectname: 'Business/AccountWechat'
    }
    configobj(qs.stringify(data), this.sobjectname.toLowerCase()).then((res) => {
      this.objectconfig = res.data.sysobject
      this.opera = res.data.opera
      this.opera.push('refresh')
    })
  },
  methods: {
    // 参照的链接跳转
    handleClick() {
      this.visible = true
      this.title = '已绑定的微信账号-' + this.data.sName
      this.defaultsearchjson.AccountId = { 'field': 'AccountId', 'value': '1', 'extraValue': this.data.ID }
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
