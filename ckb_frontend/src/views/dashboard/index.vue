<template>
  <div v-loading="loading" class="container">

    <div style="margin-top:15px">
      <el-card shadow="always" class="box-card panel-item-big">
        <div class="little-title">总览</div>
        <div class="red-transfer">
          <div class="item-inner-big">
            <div class="t">子账号数量</div>
            <div class="v">{{ accountamt }}</div>
          </div>
          <div class="item-inner-big">
            <div class="t">微信号数量</div>
            <div class="v">{{ wechatamt }}</div>
          </div>
          <div class="item-inner-big">
            <div class="t">微信号在线数量</div>
            <div class="v">{{ wechatonlineamt }}</div>
          </div>
          <div class="item-inner-big">
            <div class="t">抖音号数量</div>
            <div class="v">{{ douyinamt }}</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="always" class="box-card panel-item-big">
        <div class="little-title" style="background: rgb(77, 203, 115);padding: 5px 11px;">资源总览</div>
        <div class="red-transfer">
          <div class="item-inner-big">
            <div class="t">计划数目</div>
            <div class="v">{{ taskamt }}</div>
          </div>
          <div class="item-inner-big">
            <div class="t">未添加数</div>
            <div class="v">{{ noexecamt }}</div>
          </div>
          <div class="item-inner-big">
            <div class="t">成功数</div>
            <div class="v">{{ successamt }}</div>
          </div>
          <div class="item-inner-big">
            <div class="t">通过数</div>
            <div class="v">{{ passamt }}</div>
          </div>
        </div>

      </el-card>
    </div>
    <el-tabs type="border-card">
      <el-tab-pane label="场景获客">

        <el-row>
          <el-col :span="4" class="col">
            <el-card :body-style="{ padding: '0px' }">
              <el-row>
                <el-col><img
                    src="https://nimg.ws.126.net/?url=http%3A%2F%2Fdingyue.ws.126.net%2F2022%2F0727%2F5c84c207j00rfnrwn000hc000go00gog.jpg&thumbnail=660x2147483647&quality=80&type=jpg"
                    class="image"></el-col>
              </el-row>
              <el-row>
                <el-col :span="20" style="padding: 12px 20px;"><span>抖音</span></el-col><el-col :span="4"><el-button
                    type="text" class="button el-icon-plus" @click="newTask('douyin')" /></el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :span="4" class="col">
            <el-card :body-style="{ padding: '0px' }">
              <el-row>
                <el-col><img src="@/assets/images/mp.webp" class="image"></el-col>
              </el-row>
              <el-row>
                <el-col :span="20" style="padding: 12px 20px;"><span>公众号</span></el-col><el-col :span="4"><el-button
                    type="text" class="button el-icon-plus" @click="newTask('mp')" /></el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :span="4" class="col">
            <el-card :body-style="{ padding: '0px' }">
              <el-row>
                <el-col><img src="https://img95.699pic.com/xsj/0c/gy/3s.jpg%21/fh/300" class="image"></el-col>
              </el-row>
              <el-row>
                <el-col :span="20" style="padding: 12px 20px;"><span>海报</span></el-col><el-col :span="4"><el-button
                    type="text" class="button el-icon-plus" @click="newTask('poster')" /></el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :span="4" class="col">
            <el-card :body-style="{ padding: '0px' }">
              <el-row>
                <el-col><img
                    src="https://img2.baidu.com/it/u=594032224,3621617730&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500"
                    class="image"></el-col>
              </el-row>
              <el-row>
                <el-col :span="20" style="padding: 12px 20px;"><span>支付码</span></el-col><el-col :span="4"><el-button
                    type="text" class="button el-icon-plus" @click="newTask('qcode')" /></el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :span="4" class="col">
            <el-card :body-style="{ padding: '0px' }">
              <el-row>
                <el-col><img src="@/assets/images/chatroom.jpg" class="image"></el-col>
              </el-row>
              <el-row>
                <el-col :span="20" style="padding: 12px 20px;"><span>微信群获客</span></el-col><el-col :span="4"><el-button
                    type="text" class="button el-icon-plus" @click="newTask('chatroom')" /></el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :span="4" class="col">
            <el-card :body-style="{ padding: '0px' }">
              <el-row>
                <el-col><img src="@/assets/images/api.jpg" class="image"></el-col>
              </el-row>
              <el-row>
                <el-col :span="20" style="padding: 12px 20px;"><span>API获客</span></el-col><el-col :span="4"><el-button
                    type="text" class="button el-icon-plus" @click="newTask('api')" /></el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :span="4" class="col">
            <el-card :body-style="{ padding: '0px' }">
              <el-row>
                <el-col><img src="@/assets/images/order.png" class="image"></el-col>
              </el-row>
              <el-row>
                <el-col :span="20" style="padding: 12px 20px;"><span>订单导入</span></el-col><el-col :span="4"><el-button
                    type="text" class="button el-icon-plus" @click="newTask('orderimport')" /></el-col>
              </el-row>
            </el-card>
          </el-col>
        </el-row>

      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script>
import request from '@/utils/request'
import qs from 'qs'
export default {
  data() {
    return {
      loading: false,
      accountamt: '...',
      wechatamt: '...',
      wechatonlineamt: '...',
      taskamt: '...',
      noexecamt: '...',
      successamt: '...',
      passamt: '...'
    }
  },
  mounted() {
    this.loading = true
    const param = {
    }
    request({
      url: '/v1/backend/business/dashboard/stats',
      method: 'post',
      data: qs.stringify(param)
    }).then((res) => {
      this.accountamt = res.data.accountamt
      this.wechatamt = res.data.wechatamt
      this.wechatonlineamt = res.data.wechatonlineamt
      this.douyinamt = res.data.douyinamt
      this.taskamt = res.data.taskamt
      this.noexecamt = res.data.noexecamt
      this.successamt = res.data.successamt
      this.passamt = res.data.passamt

      this.loading = false
    })
  },
  methods: {
    newTask(sType) {
      this.$router.push('/business/friendrequesttask/new?type=' + sType)
    }
  }
}
</script>
<style scoped>
.container {
  padding: 10px;
}

.image {
  width: 200px;
  height: 200px;
  display: block;
}

.col {
  padding: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* rgb(64, 158, 255)  #409eff*/
.panel-item-big {
  display: inline-block;
  vertical-align: top;
  width: 818px;
  height: 97px;
  background: white;
  border-radius: 2px;
  -webkit-box-shadow: initial;
  box-shadow: initial;
  margin-bottom: 24px;
  margin-right: 24px;
}

.panel-item-big .little-title {
  display: inline-block;
  vertical-align: top;
  width: 38px;
  height: 97px;
  background: #3aa0ff;
  padding: 28px 11px;
  font-size: 15px;
  font-family: PingFang-SC-Bold;
  font-weight: bold;
  color: white;
  line-height: 21px;
}

.panel-item-big .red-transfer {
  display: inline-block;
}

.panel-item-big .t {
  height: 20px;
  font-size: 13px;
  font-family: PingFangSC-Regular;
  font-weight: 400;
  color: rgba(0, 0, 0, 0.45);
  line-height: 20px;
}

.panel-item-big .v {
  height: 28px;
  font-size: 22px;
  font-family: PingFangSC-Medium;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
  line-height: 29px;
}

.panel-item-big ::v-deep .el-card__body {
  padding: 0px;
  font-size: 0;
}

.info-item {
  display: inline-block;
  vertical-align: top;
  width: 312px;
  height: 188px;
  border-radius: 2px;
  margin-right: 25px;
  font-size: 14px;
  margin-bottom: 24px;
  background: white;
  border: 0;
}

.info-item:last-child {
  margin-right: 0px;
}

.item-top {
  height: 126px;
}

.item-top>div:first-child {
  height: 85px;
}

.red-transfer {
  font-size: 14px;
  height: 97px;
  padding: 23px 0;
}

.el-icon-refresh {
  float: right;
  padding: 3px 0;
  color: #409EFF;
}

.info-item .el-card__header {
  padding: 10px 20px;
  border-bottom: 1px solid #ebeef5 !important;
}

.item-top-title1 {
  height: 22px;
  font-size: 14px;
  font-family: PingFangSC-Regular;
  font-weight: 400;
  color: rgba(0, 0, 0, 0.45);
  line-height: 22px;
}

.item-top-value1 {
  height: 38px;
  font-size: 30px;
  font-family: HelveticaNeue;
  color: rgba(0, 0, 0, 0.85);
  line-height: 38px;
  margin-top: 4px;
}

.item-bottom {
  border-top: 1px solid #e8e8e8;
  line-height: 39px;
}

.item-bottom>div {
  display: inline-block;
  width: 95px;
}

.item-bottom>div:first-child {
  width: 160px;
}

.item-top .item-bottom2 {
  height: 22px;
  line-height: 22px;
}

.item-bottom2>div {
  display: inline-block;
  width: 125px !important;
}

.pillar-container {
  width: 100%;
  height: 8px;
  background: #f0f2f5;
  border-radius: 1px;
}

.pillar-inner {
  height: 8px;
  border-radius: 1px 0px 0px 1px;
  margin-top: 8px;
}

.pillar-inner.account {
  background: #8543e0;
}

.pillar-inner.device {
  background: #13c2c2;
}

.pillar-inner.wechat {
  background: #33abfb;
}

.box-card {
  padding: 0px;
}

.panel-item-big .el-card__body {
  padding: 0px;
  font-size: 0;
}

.info-item .el-card__header {
  padding: 10px 10px;
}

.info-item .el-card__body {
  padding: 20px 25px 0px 25px;
}

.el-table .row-selected {
  background-color: rgba(0, 0, 0, 0.1);
  color: blue;
}

.stat-item .el-card__body {
  padding: 0 !important;
}

.stat-item {
  width: 1080px;
}

.stat-item .stat-title {
  height: 60px;
  padding: 16px 0 14px 40px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.item-inner {
  display: inline-block;
  line-height: 32px;
  width: 100%;
}

.item-inner-big {
  display: inline-block;
  width: 190px;
  line-height: 32px;
  text-align: center;
  border-right: 1px solid rgba(0, 0, 0, 0.1);
}

.red-transfer .item-inner-big:last-child {
  border-right: 0px;
}
</style>
