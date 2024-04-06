<template>
  <span>
    <el-button type="primary" size="mini" :icon="btn.icon" @click="handleClick()">{{ btn.sName }}</el-button>
    <new-user ref="newaccountuser" :accountid="objectid" @reloadData="reloadData" />
    <wechat-append ref="wechatappend" />
    <friend-req-import ref="friendreqimport" :objectid="objectid" :listtable="listTable" />
    <jd-social-media-import ref="jdsocialmediaimport" :listtable="listTable" />
    <alloc ref="alloc" :s-object-name="sObjectName" :listtable="listTable" />
  </span>
</template>
<script>
import qs from 'qs'
import Alloc from '@/components/ListTable/alloc'
import NewUser from '@/views/account/newuser'
import WechatAppend from '@/views/business/WechatAppend'
import FriendReqImport from '@/views/business/friendrequesttask/import'
import JdSocialMediaImport from '@/views/business/jdsocialmedia/import'
import request from '@/utils/request'
export default {
  name: 'ListButton',
  components: {
    NewUser, WechatAppend, FriendReqImport, Alloc, JdSocialMediaImport
  },
  props: ['btn', 'sObjectName', 'sfromobjectname', 'objectid'],
  data() {
    return {
      listTable: null,
      newAccountUserVisible: false
    }
  },
  mounted() {
    this.listTable = this.$parent
    while (this.listTable) {
      if (this.listTable.$options.name == 'ListTable') {
        break
      }
      this.listTable = this.listTable.$parent
    }
  },
  methods: {
    handleNewAccountUser() {
      this.$refs.newaccountuser.show()
    },
    handleClick() {
      this[this.btn.handler]()
    },
    handleNew() {
      this.$router.push('/' + this.sObjectName.toLowerCase() + '/new')
    },
    handleDel() {
      this.listTable.handleDel()
    },
    handleExport() {
      this.listTable.handleExport()
    },
    handleRefresh() {
      this.listTable.handleRefresh()
    },
    handleBatchSelect() {
      this.listTable.handleBatchSelect()
    },
    reloadData(data) {
      this.$emit('reloadData', {})
    },
    handleNewMaterial() {
      this.$router.push('/' + this.sObjectName.toLowerCase() + '/new/' + this.$route.path.split('/')[4])
    },
    handleOpera() {
      if (this.listTable.multipleSelection.length === 0) {
        this.$notify.error({
          title: '警告',
          message: '请至少选择一条数据',
          type: 'warning'
        })
        return
      } else if (this.listTable.multipleSelection.length > 1) {
        this.$notify.error({
          title: '警告',
          message: '只能选一条数据',
          type: 'warning'
        })
        return
      }

      this.$router.push('/' + this.sObjectName + '/opera/objectid/' + this.listTable.multipleSelection[0].data.ID)
    },
    handleGenerateAiContent() {
      if (this.listTable.multipleSelection.length === 0) {
        this.$notify.error({
          title: '警告',
          message: '请至少选择一条数据',
          type: 'warning'
        })
        return
      }

      const param = this.listTable.formatParam()
      if (!this.listTable.selectall) {
        for (const index in this.listTable.multipleSelection) {
          param.selectedids[param.selectedids.length] = this.listTable.multipleSelection[index].data.ID
        }
        param.selectedids = JSON.stringify(param.selectedids)
      } else {
        param.selectedids = 'all'
      }

      const that = this
      this.loading = true

      request({
        url: '/v1/backend/business/material/regenerateai',
        method: 'post',
        data: qs.stringify(param)
      }).then((res) => {
        this.listTable.selectall = false
        this.listTable.handleSelectall()

        this.listTable.loadData()
      }).catch(function (error) {
        that.listTable.loadData()
      })
    },
    handleErrorTask(sAction) {
      if (this.listTable.multipleSelection.length === 0) {
        this.$notify.error({
          title: '警告',
          message: '请至少选择一条数据',
          type: 'warning'
        })
        return
      }

      const param = this.listTable.formatParam()
      if (!this.listTable.selectall) {
        for (const index in this.listTable.multipleSelection) {
          param.selectedids[param.selectedids.length] = this.listTable.multipleSelection[index].data.ID
        }
        param.selectedids = JSON.stringify(param.selectedids)
      } else {
        param.selectedids = 'all'
      }

      const that = this
      this.loading = true

      request({
        url: '/v1/backend/business/errortask/' + sAction,
        method: 'post',
        data: qs.stringify(param)
      }).then((res) => {
        this.listTable.selectall = false
        this.listTable.handleSelectall()

        this.listTable.loadData()
      }).catch(function (error) {
        that.listTable.loadData()
      })
    },
    handleErrorTaskResponse() {
      this.handleErrorTask('response')
    },

    handleErrorTaskDone() {
      this.handleErrorTask('done')
    },

    handleSyncWechat() {
      request({
        url: '/v1/backend/business/wechat/sync',
        method: 'post'
      }).then((res) => {
        this.$notify({
          title: '成功',
          message: res.message,
          type: 'success'
        })
      }).catch(function (error) {

      })
    },
    handleSyncWechatFriend() {
      request({
        url: '/v1/backend/business/wechatfriend/sync',
        method: 'post'
      }).then((res) => {
        this.$notify({
          title: '成功',
          message: res.message,
          type: 'success'
        })
      }).catch(function (error) {

      })
    },
    handleSyncWechatroom() {
      request({
        url: '/v1/backend/business/wechatroom/sync',
        method: 'post'
      }).then((res) => {
        this.$notify({
          title: '成功',
          message: res.message,
          type: 'success'
        })
      }).catch(function (error) {

      })
    },
    handleWechatAppend() {
      this.$refs.wechatappend.show()
    },
    handleBatchBindWechat() {
      if (this.listTable.multipleSelection.length === 0) {
        this.$notify.error({
          title: '警告',
          message: '请至少选择一条数据',
          type: 'warning'
        })
        return
      }

      const param = this.listTable.formatParam()
      if (!this.listTable.selectall) {
        for (const index in this.listTable.multipleSelection) {
          param.selectedids[param.selectedids.length] = this.listTable.multipleSelection[index].data.ID
        }
        param.selectedids = JSON.stringify(param.selectedids)
      } else {
        this.$notify.error({
          title: '警告',
          message: '请不要跨页批量选择',
          type: 'warning'
        })
        return
      }

      param.objectid = this.$route.path.split('/')[4]

      const that = this
      this.loading = true

      request({
        url: '/v1/backend/business/accountwechat/bind',
        method: 'post',
        data: qs.stringify(param)
      }).then((res) => {
        location.reload()
      })
    },
    handleFriendReqImport() {
      this.$refs.friendreqimport.show()
    },
    handleJdSocialMediaImport() {
      this.$refs.jdsocialmediaimport.show()
    },
    handleAlloc() {
      if (this.listTable.multipleSelection.length === 0) {
        this.$notify.error({
          title: '警告',
          message: '请至少选择一条数据',
          type: 'warning'
        })
        return
      }

      this.$refs.alloc.show()
    }

  }
}
</script>
