<template>
  <span>
    <el-button
      type="text"
      size="small"
      @click="handleClick()"
    >{{ btn.sName }}</el-button>
    <div v-if="btn.ID=='addToMiniPrograme'">

      <el-dialog
        title="添加至小程序库"
        width="600px"
        top="100px"
        append-to-body
        :visible.sync="visibleAddToMiniPrograme"
      >
        <el-form
          ref="form"
          :model="miniprogrameform"
          label-width="120px"
          label-suffix=":"
        >
          <el-form-item
            label="小程序名称"
            required
          >
            <el-col :span="24">
              <el-input v-model="miniprogrameform.sName" />
            </el-col>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="small"
              @click="submitMPForm()"
            >保存</el-button>
            <el-button
              type="primary"
              size="small"
              plain
              @click="back()"
            >取消</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>

    </div>
  </span>
</template>
<script>
import request from '@/utils/request'
import qs from 'qs'
export default {
  name: 'DataButton',
  props: ['data', 'btn', 'sObjectName'],
  data() {
    return {
      visibleAddToMiniPrograme: false,
      miniprogrameform: {
        lId: '',
        sName: ''
      }
    }
  },
  mounted() {

  },
  methods: {
    handleClick() {
      this[this.btn.handler]()
    },
    handleView() {
      // const routeData = this.$router.resolve({
      //   path: '/' + this.sObjectName.toLowerCase() + '/view/' + encodeURI(this.data.ID)
      // })
      // window.open(routeData.href, '_blank')

      this.$router.push('/' + this.sObjectName.toLowerCase() + '/view/' + encodeURI(this.data.ID))
    },
    handleEdit() {
      this.$router.push('/' + this.sObjectName.toLowerCase() + '/edit/' + this.data.ID)
    },
    handleConfigWorkFlow() {
      this.$router.push('/' + this.sObjectName.toLowerCase() + '/configworkflow/' + this.data.ID)
    },
    handleaddToMiniPrograme() {
      this.visibleAddToMiniPrograme = true

      this.miniprogrameform.lId = this.data.ID

      const data = {
        id: this.data.ID
      }
      request({
        url: '/v1/backend/business/material/getminiprogramename',
        method: 'post',
        data: qs.stringify(data)
      }).then((res) => {
        this.miniprogrameform.sName = res.message
      })
    },
    handleAllocChatroomTask() {

    },
    submitMPForm() {
      if (this.miniprogrameform.sName == '') {
        this.$notify.error({
          title: '警告',
          message: '请输入小程序名称',
          type: 'warning',
          offset: 100
        })
        return
      }

      request({
        url: '/v1/backend/business/miniprogramelib/add',
        method: 'post',
        data: qs.stringify(this.miniprogrameform)
      }).then((res) => {
        this.$notify({
          title: '成功',
          message: res.message,
          type: 'success'
        })
        this.visibleAddToMiniPrograme = false
      })
    }
  }
}
</script>
