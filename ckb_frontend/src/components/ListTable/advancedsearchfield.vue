<template>
  <div v-if="field.sDataType == 'Date' || field.sDataType == 'DateTime' || field.sDataType == 'Bool'">
    <el-form-item :label="field.sName">
      <ul>
        <li
          v-for="opt in opts"
          :class="currSelectValue === opt ? 'curr' : ''"
          @click="select(opt)"
        >{{ opt }}</li>
        <li v-if="currSelectValue === '自定义'">
          <el-date-picker
            v-model="extraValue"
            class="datapicker"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            size="mini"
            @change="datePick"
          />
        </li>
      </ul>
    </el-form-item>
  </div>
  <div v-else-if="field.sDataType == 'List' || field.sDataType == 'MultiList'">
    <el-form-item :label="field.sName">
      <ul>
        <li
          :class="currSelectValue === '全部' ? 'curr' : ''"
          @click="select('全部')"
        >全部</li>
        <li
          v-for="opt in field.arrEnumOption"
          :class="extraValue === opt.ID ? 'curr' : ''"
          @click="select(opt.sName, opt.ID)"
        >{{ opt.sName }}</li>
      </ul>
    </el-form-item>
  </div>
  <div v-else-if="field.sDataType == 'ListTable'">
    <span>
      <el-form-item :label="field.sName">
        <el-select
          v-model="extraValue"
          v-loadmore="loadMore"
          filterable
          clearable
          :loading="loading"
          :remote-method="remoteMethod"
          remote
          placeholder="请选择"
          @change="handleChange"
          @visible-change="visibleChange"
        >
          <el-option
            v-for="data in listData"
            :key="data.data.ID"
            :label="data.data.sName"
            :value="data.data.ID"
          />
        </el-select>
      </el-form-item>
    </span>
  </div>
  <div v-else-if="field.sDataType == 'Text' || field.sDataType == 'TextArea' || field.sDataType == 'Int' || field.sDataType == 'Float'">
    <span>
      <el-form-item :label="field.sName">
        <el-input
          v-model="currSelectValue"
          placeholder="请输入内容"
          @input="handleChange"
        />
      </el-form-item>
    </span>
  </div>
</template>
<script>
import qs from 'qs'
import { tablist, list } from '@/api/commonobject'
export default {
  name: 'Advancedsearchfield',
  props: ['field', 'defaultvalue'],
  data() {
    var opts = []
    if (this.field.sDataType === 'Date' || this.field.sDataType === 'DateTime') {
      opts = ['全部', '今天', '昨天', '本周', '本月', '上月', '自定义']
    } else if (this.field.sDataType === 'Bool') {
      opts = ['全部', '是', '否']
    }

    var currSelectValue = null
    if (!this.defaultvalue) {
      if (this.field.sDataType === 'Text' || this.field.sDataType === 'TextArea' || this.field.sDataType === 'Int' || this.field.sDataType === 'Float') {

      } else {
        currSelectValue = '全部'
      }
    } else {
      currSelectValue = this.defaultvalue
    }

    return {
      loading: false,
      opts: opts,
      listData: [],
      currSelectValue: currSelectValue,
      extraValue: null,
      ismore: true,
      listTableParam: {
        sobjectname: this.field.sRefKey,
        page: 1,
        pagelimit: 10,
        listid: this.listid,
        tabid: this.tabid,
        fastsearchfield: null,
        fastsearchkeyword: null,
        keyword: null,
        selectedids: [],
        advsearchjson: [],
        orderby: null
      }
    }
  },
  mounted() {
    this.triggerParentUpdate()
  },
  methods: {
    handleChange(v) {
      console.log(v)

      if (!v && this.field.sDataType === 'ListTable') {
        this.currSelectValue = '全部'
      }

      this.triggerParentUpdate(true)
    },
    triggerParentUpdate(reload) {
      if (this.field.sDataType === 'ListTable') {
        for (const index in this.listData) {
          if (this.extraValue === this.listData[index].data.ID) {
            this.currSelectValue = this.listData[index].data.sName
          }
        }
      }

      this.$emit('updateSearchValue', { sFieldAs: this.field.sFieldAs, value: this.currSelectValue, extraValue: this.extraValue, reload: reload })
    },
    select(value, extraValue) {
      this.currSelectValue = value
      this.extraValue = extraValue
      this.listTableParam.page = 1

      this.triggerParentUpdate(true)
    },
    datePick() {
      this.triggerParentUpdate(true)
    },
    loadMore() {
      this.listTableParam.page++
      this.loadListData()
    },
    visibleChange(visible) {
      this.listData = []
      this.listTableParam.page = 1
      this.ismore = true
      if (visible) {
        this.loadListData()
      } else {
        this.listTableParam.keyword = null
      }
    },
    loadListData() {
      if (!this.ismore) {
        return
      }

      this.loading = true

      const that = this
      tablist(qs.stringify({
        sobjectname: this.listTableParam.sobjectname
      }), this.listTableParam.sobjectname.toLowerCase()).then((res) => {
        if (res.code === 10000) {
          that.listTableParam.listid = res.data[0].ListId
          list(qs.stringify(that.listTableParam), that.listTableParam.sobjectname.toLowerCase()).then((res) => {
            if (res.code === 10000) {
              that.listData = that.listData.concat(res.data.arrData)
              that.ismore = res.data.ismore
              that.loading = false
            }
          })
        }
      })
    },
    remoteMethod(query) {
      this.listTableParam.keyword = query
      this.listData = []
      this.listTableParam.page = 1
      this.ismore = true
      this.loadListData()
    }
  }
}
</script>
<style lang="scss" scoped>
.el-form-item {
  margin-bottom: 10px;
}

.el-form-item ul {
  display: flex;
  flex-direction: row;
  margin: 0;
  padding: 0;
  margin-top: 5px;
}

.el-form-item li {
  padding: 0 12px;
  margin-right: 0;
  cursor: pointer;
  height: 30px;
  line-height: 30px;
  list-style: none;
}

ul li.curr {
  color: #459df5;
  background: #deefff;
}
</style>
