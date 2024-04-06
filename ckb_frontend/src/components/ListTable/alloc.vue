<template>
  <span>
    <el-dialog title="批量分配数据" width="480px" top="100px" append-to-body :visible.sync="visible">
      <el-form label-width="120px" label-suffix=":">
        <el-form-item label="客户">
          <el-select v-model="accountid" placeholder="请选择" filterable clearable>
            <el-option v-for="item in accounts" :key="item.lId" :label="item.sName" :value="item.lId">
              {{ item.sName }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="分配用户" required="">
          <el-select v-model="userid" placeholder="请选择" filterable clearable>
            <el-option v-for="item in users" :key="item.lID" :label="item.sName" :value="item.lID">
              {{ item.sName }}
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="visible = false">关闭</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </span>
    </el-dialog>
  </span>
</template>
<script>
import request from '@/utils/request'
import qs from 'qs'
export default {
  name: 'Alloc',
  props: ['sObjectName'],
  data() {
    return {
      visible: false,
      accounts: [],
      users: [],
      userid: null,
      accountid: null
    }
  },
  watch: {
    accountid() {
      this.loadUser()
    }
  },
  methods: {
    loadAcc() {
      const data = {}
      request({
        url: '/v1/backend/business/account/getall',
        method: 'post',
        data: qs.stringify(data)
      }).then((res) => {
        this.accounts = res.data
      })
    },
    loadUser() {
      const data = {
        accountid: this.accountid
      }
      request({
        url: '/v1/backend/business/account/getuserbyacc',
        method: 'post',
        data: qs.stringify(data)
      }).then((res) => {
        this.users = res.data
      })
    },
    show() {
      this.visible = true
      this.loadAcc()
      this.loadUser()
    },
    submit() {
      if (!this.userid) {
        this.$notify.error({
          title: '警告',
          message: '请选择分配的用户',
          type: 'warning',
          offset: 0
        })
        return
      }

      const listTable = this.$parent.listTable

      const param = listTable.formatParam()
      if (!listTable.selectall) {
        for (const index in listTable.multipleSelection) {
          param.selectedids[param.selectedids.length] = listTable.multipleSelection[index].data.ID
        }
        param.selectedids = JSON.stringify(param.selectedids)
      } else {
        param.selectedids = 'all'
      }

      param.keyword = this.userid

      request({
        url: '/v1/backend/' + this.sObjectName + '/alloc',
        method: 'post',
        data: qs.stringify(param)
      }).then((res) => {
        listTable.loadData()
        this.visible = false
      })
    }
  }
}
</script>
