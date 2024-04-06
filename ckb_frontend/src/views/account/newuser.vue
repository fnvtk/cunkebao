<template>
  <span>
    <el-dialog title="创建子账号" :visible.sync="visible" width="600px">

      <el-form ref="createmenuform" :rules="rules" :model="menuFormData" label-width="100px">
        <el-form-item label="姓名" prop="sName" required>
          <el-input v-model="menuFormData.sName" />
        </el-form-item>
        <el-form-item label="手机号" prop="sMobile" required>
          <el-input v-model="menuFormData.sMobile" />
          <slot>登录时使用手机号登录，初始密码为123456</slot>
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="visible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </span>
    </el-dialog>
  </span>
</template>
<script>
import qs from 'qs'
import request from '@/utils/request'
export default {
  name: 'NewUser',
  props: ['accountid'],
  data() {
    return {
      visible: false,
      menuFormData: {
        id: null,
        sName: null,
        sMobile: null
      },
      rules: {
        sName: [
          { required: true, message: '请输入姓名', trigger: 'change' }
        ],
        sMobile: [
          { required: true, message: '请输入手机号', trigger: 'change' }
        ]
      }
    }
  },
  mounted() {

  },
  methods: {
    show() {
      this.visible = true
    },
    save() {
      this.$refs.createmenuform.validate((valid) => {
        if (valid) {
          request({
            url: '/v1/backend/business/account/newuser',
            method: 'post',
            data: qs.stringify({
              AccountId: this.accountid,
              sName: this.menuFormData.sName,
              sMobile: this.menuFormData.sMobile
            })
          }).then((res) => {
            this.$refs.createmenuform.resetFields()
            this.visible = false
            this.$emit('reloadData', {})
          })
        } else {
          return false
        }
      })
    }
  }
}

</script>
