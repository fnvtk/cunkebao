<template>
  <div>
    <div v-if="field.sFieldAs === sNameFieldAs">
      <el-link
        type="primary"
        :underline="false"
        @click="handleView(field.sObjectName, rowData.ID)"
      >{{
        fieldValue }}</el-link>
    </div>
    <div v-else-if="field.sUIType === 'ListTable'">
      <el-link
        v-if="fieldValue"
        :underline="false"
        type="primary"
        @click="handleView(field.sRefKey, fieldValue.ID)"
      >{{
        fieldValue.sName }}</el-link>
    </div>
    <div v-else-if="field.sUIType == 'Bool'">
      <el-tag
        v-if="fieldValue == 1"
        type="success"
      >是</el-tag>
      <el-tag
        v-else
        type="danger"
      >否</el-tag>
    </div>
    <div v-else-if="field.sUIType == 'Common' || field.sUIType == 'List'">
      <div v-if="fieldValue != null">{{ fieldValue.sName }}</div>
    </div>
    <div v-else-if="field.sUIType === 'MultiList'">
      <span v-for="(opt, index) in fieldValue">
        {{ opt.sName }}
        <span v-if="index < fieldValue.length - 1"> ; </span>
      </span>
    </div>
    <div v-else-if="field.sUIType === 'AttachFile'">
      <div v-for="opt in fieldValue">
        <a
          v-if="opt.sFilePath.indexOf('http://') > -1 || opt.sFilePath.indexOf('https://') > -1"
          :href="opt.sFilePath"
          target="_blank"
        >{{ opt.sName }}</a>
        <a
          v-else
          :href="$store.state.config.uploadUrl + '/' + opt.sFilePath"
          target="_blank"
        >{{ opt.sName
        }}</a>
      </div>
    </div>
    <div v-else-if="field.sUIType === 'AttachImage'">
      <span v-for="opt in fieldValue">
        <a
          v-if="opt.sFilePath.indexOf('http://') > -1 || opt.sFilePath.indexOf('https://') > -1"
          :href="opt.sFilePath"
          target="_blank"
        ><img
          :src="opt.sFilePath"
          width="100"
        ></a>
        <a
          v-else
          :href="$store.state.config.uploadUrl + '/' + opt.sFilePath"
          target="_blank"
        ><img
          :src="$store.state.config.uploadUrl + '/' + opt.sFilePath"
          width="100"
        ></a>
      </span>
    </div>
    <div v-else-if="field.sUIType === 'Image'">
      <img
        :src="fieldValue"
        width="100"
      >
    </div>
    <div v-else-if="field.sDataType === 'Date'">
      {{ new Date(fieldValue).toLocaleDateString() }}
    </div>
    <div v-else>
      <other-item
        :field="field"
        :row-data="rowData"
        :value="fieldValue"
      />
    </div>
  </div>
</template>
<script>
import OtherItem from '@/components/ListTable/otheritem'
export default {
  name: 'FieldItem',
  components: {
    OtherItem
  },
  props: ['field', 'rowData', 'sNameFieldAs', 'fieldValue'],
  data() {
    return {
    }
  },
  methods: {
    handleView(sObjectName, ID) {
      // const routeData = this.$router.resolve({
      //   path: '/' + sObjectName.toLowerCase() + '/view/' + ID
      // })
      // window.open(routeData.href, '_blank')

      this.$router.push('/' + sObjectName.toLowerCase() + '/view/' + ID)
    }
  }
}
</script>
