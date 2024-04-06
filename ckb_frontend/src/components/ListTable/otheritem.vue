<template>
  <div>
    <div v-if="field.sUIType === 'WeChat' || field.sUIType === 'WeChatInfo'">
      <img :src="value.sAvatar" style="vertical-align: middle;width: 48px;height: 48px;margin: 2px;">
      <div style="display: inline-block;width: calc(100% - 55px);vertical-align: middle;line-height: 16px;">
        {{ value.sName }}<br>
        {{ value.sWechatId }}<br>
        {{ value.sAlias }}
      </div>
    </div>
    <div v-else-if="field.sUIType === 'CollectObject'">
      <img :src="value.sAvatar" style="vertical-align: middle;width: 48px;height: 48px;margin: 2px;">
      <div style="display: inline-block;width: calc(100% - 55px);vertical-align: middle;line-height: 16px;">
        {{ value.sName }}<br>
        {{ value.sWechatId }}<br>
        {{ value.sAlias }}
      </div>
    </div>
    <div v-else-if="field.sUIType === 'CollectObjects'">
      <img v-for="object in value" :key="object" :src="object.sAvatar"
        style="vertical-align: middle;width: 38px;margin: 2px;">
    </div>
    <div v-else-if="field.sUIType === 'MomentStatus'">
      <el-button type="primary" size="mini" @click="getMomentStatus">{{ value.sName }}</el-button>
      <el-dialog title="发朋友圈详情" width="900px" height="70%" top="50px" append-to-body :visible.sync="visibleMomentStatus">

        <el-table :data="tableMomentStatus" border>
          <el-table-column label="微信号" width="250px">
            <template slot-scope="scope">
              <img :src="scope.row.avatar" style="vertical-align: middle;width: 48px;height: 48px;margin: 2px;">
              <div style="display: inline-block;width: calc(100% - 55px);vertical-align: middle;line-height: 16px;">
                {{ scope.row.nickname }}<br>
                {{ scope.row.wechatId }}<br>
                {{ scope.row.alias }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态">
            <template slot-scope="scope">
              <div v-if="scope.row.status == 1">执行成功</div>
              <div v-else-if="scope.row.status == 10">待执行</div>
              <div v-if="scope.row.isAlive" style="color:green">微信在线</div>
              <div v-else style="color:grey">微信离线</div>
            </template>
          </el-table-column>
          <el-table-column label="扩展">
            <template slot-scope="scope">
              {{ scope.row.extra }}
            </template>
          </el-table-column>
          <el-table-column label="执行时间">
            <template slot-scope="scope">
              {{ scope.row.executeTime }}
            </template>
          </el-table-column>
          <el-table-column label="结束时间">
            <template slot-scope="scope">{{ scope.row.finishedTime }}
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </div>
    <div v-else-if="field.sUIType === 'FriendRequestTask/WechatList'">
      <el-link type="primary" :underline="false" @click="bindWechat">
        <span v-if="value == ''">0</span>
        <span v-else>
          {{ value.length }}
        </span>
      </el-link>
      <bind-wechat :id="rowData.ID" ref="bindwechat" />
    </div>
    <div v-else-if="field.sUIType === 'FriendRequestTask/opera'">
      <el-button-group>
        <el-button size="mini" @click="viewReqTaskStats()">统计</el-button>
        <!--<el-button @click="kefuClick(scope.row)" size="mini" style="">微信池</el-button>-->
        <el-button size="mini" @click="viewReqTaskDetail()">客户池</el-button>
        <el-button size="mini" @click="editReqTask(rowData.ID)">编辑</el-button>
      </el-button-group>
      <friend-request-task-detail :id="rowData.ID" ref="reqtaskdetail" :field="field" />
      <friend-req-stats :id="rowData.ID" ref="reqtaskstats" />
    </div>
    <div v-else-if="field.sUIType === 'FriendRequestTask/amt'">
      <el-link ref="" type="primary" :underline="false" @click="viewReqTaskDetail">
        {{ value }}
      </el-link>
      <friend-request-task-detail :id="rowData.ID" ref="reqtaskdetail" :field="field" />
    </div>
    <div v-else-if="field.sUIType === 'FriendRequestTask/passpercent'">
      <span v-if="rowData.lSuccessAmt > 0">
        {{ Math.round(rowData.lPassAmt / rowData.lSuccessAmt * 1000) / 10 }}%
      </span>
      <span v-else>
        0%
      </span>
    </div>
    <div v-else-if="field.sUIType === 'material/ai'">
      <el-button v-if="rowData.bAIGenerated == 1" size="mini" type="primary"
        @click="viewAiContent(rowData.ID)">查看</el-button>
      <ai-content :id="rowData.ID" ref="aicontent" />
    </div>
    <div v-else>{{ value }}</div>
  </div>
</template>

<script>
import WechatAmtLink from '@/views/business/WechatAmtLink'
import BindWechat from '@/views/business/friendrequesttask/bindwechat'
import FriendRequestTaskDetail from '@/views/business/friendrequesttask/detail'
import FriendReqStats from '@/views/business/friendrequesttask/stats'
import AiContent from '@/views/business/material/aicontent'
import qs from 'qs'
import request from '@/utils/request'
import Aicontent from '@/views/business/material/aicontent.vue'
export default {
  name: 'ListTableOtherItem',
  components: {
    WechatAmtLink, BindWechat, FriendRequestTaskDetail, FriendReqStats, AiContent
  },
  props: ['field', 'value', 'rowData'],
  data() {
    return {
      visibleMomentStatus: false,
      tableMomentStatus: []
    }
  },
  methods: {
    bindWechat() {
      this.$refs.bindwechat.show()
    },
    getMomentStatus() {
      request({
        url: '/v1/backend/business/materialpushlog/getmomentstatus',
        method: 'post',
        data: qs.stringify({ id: this.rowData.ID })
      }).then((res) => {
        this.visibleMomentStatus = true
        this.tableMomentStatus = res.data.result

        for (let i = 0; i < this.tableMomentStatus.length; i++) {
          let date = new Date(this.tableMomentStatus[i].executeTime)
          this.tableMomentStatus[i].executeTime = this.getDateTimeString(date)

          date = new Date(this.tableMomentStatus[i].finishedTime)
          this.tableMomentStatus[i].finishedTime = this.getDateTimeString(date)
        }
      })
    },
    getDateTimeString(dateObj) {
      const year = dateObj.getFullYear()

      if (year === 1) {
        return '无'
      }

      const month = String(dateObj.getMonth() + 1).padStart(2, '0')
      const day = String(dateObj.getDate()).padStart(2, '0')
      const hours = String(dateObj.getHours()).padStart(2, '0')
      const minutes = String(dateObj.getMinutes()).padStart(2, '0')
      const seconds = String(dateObj.getSeconds()).padStart(2, '0')

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },
    editReqTask(id) {
      this.$router.push('/business/friendrequesttask/edit/' + id)
    },
    viewReqTaskDetail() {
      this.$refs.reqtaskdetail.show()
    },
    viewReqTaskStats() {
      this.$refs.reqtaskstats.show()
    },
    viewAiContent() {
      this.$refs.aicontent.show()
    }
  }
}
</script>
