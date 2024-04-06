<template>
  <div class="listtable-container">
    <div v-if="listtype !== 'info' && advancedsearchvisible" class="advancedsearch-container">
      <advancedsearch :fields="listConfig.arrAdvancedSearchId" @updateSearchValue="updateAdvSearchValue" />
    </div>
    <div class="list-data">
      <div v-if="opera.length > 0 && arrListBtn.length > 0" class="button">
        <el-button-group>
          <span v-for="(btn, index) in arrListBtn" :key="index">
            <list-button
              v-if="opera.includes(btn.ID)"
              :btn="btn"
              :s-object-name="sobjectname"
              :sfromobjectname="sfromobjectname"
              :objectid="objectid"
              @reloadData="loadData"
            />
          </span>
        </el-button-group>
      </div>
      <div v-if="listConfig.arrFastSearchId && listConfig.arrFastSearchId.length > 0 || listConfig.bCanBat" class="tool">
        <div v-if="listConfig.bCanBat" class="selectall">
          <el-checkbox v-model="selectall" @change="handleSelectall">跨页选择所有数据</el-checkbox>

        </div>

        <div v-if="listConfig.arrFastSearchId && listConfig.arrFastSearchId.length > 0" class="fastsearch">
          <el-row>
            <el-col :span="8">
              <el-select v-model="listTableParam.fastsearchfield" size="mini" filterable placeholder="请选择">
                <el-option
                  v-for="item in listConfig.arrFastSearchId"
                  :key="item.sFieldAs"
                  :label="item.sName"
                  :value="item.sFieldAs"
                />
              </el-select>
            </el-col>
            <el-col :span="12">
              <el-input v-model="listTableParam.fastsearchkeyword" size="mini" placeholder="请输入关键字查询" />
            </el-col>
            <el-col :span="4">
              <el-button size="mini" icon="el-icon-search" @click="getConfigList()">查询</el-button>
            </el-col>
          </el-row>
        </div>
      </div>
      <div v-if="listConfig.lID" class="datatable">
        <el-table
          ref="datatable"
          v-loading="loading"
          :data="listData"
          :border="true"
          style="width: 100%"
          :height="maxheight"
          fit
          stripe
          size="small"
          :show-summary="isShowSummary"
          :summary-method="getSummaries"
          @sort-change="handleSortChange"
          @selection-change="handleSelectionChange"
        >
          <el-table-column v-if="listConfig.bCanBat" type="selection" width="40" />
          <el-table-column v-if="listConfig.bShowOpera" label="操作" fixed :width="listConfig.lOperaColumnWidth">
            <template slot-scope="scope">
              <div v-if="listtype == 'list' || listtype == 'info'">
                <span v-for="btn in scope.row.btns" :key="btn.ID" style="margin-right:5px">
                  <data-button
                    v-if="opera.includes(btn.ID)"
                    :btn="btn"
                    :s-object-name="sobjectname"
                    :data="scope.row.data"
                  />
                </span>
              </div>
              <div v-else-if="listtype == 'refer'" style="text-align:center">
                <el-button
                  type="text"
                  size="small"
                  @click="handleReferSelect(scope.row.data.ID, scope.row.data.sName)"
                >选中</el-button>
              </div>
            </template>
          </el-table-column>
          <!-- <el-table-column type="index" label="序号" :index="indexMethod" width="50" /> -->
          <el-table-column
            v-for="item in listConfig.arrListSelectId"
            :key="item.lID"
            :prop="item.sFieldAs"
            :type="item.sDataType"
            :width="item.lWidth"
            :align="item.sAlign"
            :label="item.sName"
            :sortable="(item.sDataType == 'Text' || item.sDataType == 'Int' || item.sDataType == 'Float' || item.sDataType == 'Bool' || item.sDataType == 'Date' || item.sDataType == 'DateTime') ? 'custom' : false"
          >
            <template slot-scope="scope">
              <!-- <div v-if="scope.row.data[scope.column.property] !== '' && scope.row.data[scope.column.property] !== null"> -->
              <field-item
                v-if="item.referenceField == null"
                :field="item"
                :field-value="scope.row.data[scope.column.property]"
                :row-data="scope.row.data"
                :s-name-field-as="objectconfig.sNameFieldAs"
              />
              <field-item
                v-else
                :field="item.referenceField"
                :row-data="scope.row.data"
                :field-value="scope.row.data[scope.column.property]"
                :s-name-field-as="objectconfig.sNameFieldAs"
              />
              <!-- </div> -->
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination">
        <el-pagination
          :current-page.sync="page"
          :page-sizes="pagination.sizes"
          :page-size="listTableParam.pagelimit"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>
<script>
import qs from 'qs'
import { configlist, list, listdel, listexport } from '@/api/commonobject'
import Advancedsearch from '@/components/ListTable/advancedsearch'
import DataButton from '@/components/ListTable/databutton'
import FieldItem from '@/components/ListTable/fielditem'
import ListButton from '@/components/ListTable/listbutton'

export default {
  name: 'ListTable',
  components: {
    Advancedsearch, DataButton, ListButton, FieldItem
  },
  props: ['tabid', 'listid', 'sobjectname', 'sfromobjectname', 'objectconfig', 'listtype', 'defaultsearchjson', 'relatedid', 'objectid', 'opera', 'loadfromcache'],
  data() {
    return {
      // 提交到后端的视图参数
      listTableParam: {
        sobjectname: this.sobjectname,
        page: 1,
        pagelimit: 10,
        listid: this.listid,
        tabid: this.tabid,
        relatedid: this.relatedid,
        objectid: this.objectid,
        fastsearchfield: null,
        fastsearchkeyword: null,
        selectedids: [],
        advsearchjson: [],
        orderby: {
          field: null,
          order: null
        }
      },

      page: 1,

      // 视图的按钮
      arrListBtn: [],

      // 视图的配置
      listConfig: {},

      // 视图表格的数据
      listData: [],

      // 统计字段
      sumData: {},

      // 数据总数
      pagination: {
        total: 0,
        count: 0,
        sizes: [10, 20, 50, 100]
      },

      // 选中的数据
      multipleSelection: [],

      // 全选
      selectall: false,

      loading: false,

      isShowSummary: false,

      advancedsearchvisible: false,

      maxheight: '300px'
    }
  },
  watch: {
    page: function(newName, oldName) {
      this.listTableParam.page = newName
    }
  },
  mounted() {
    this.getConfigList()

    // 视图的参数从缓存中获取
    if (this.loadfromcache) {
      const cacheparam = localStorage.getItem('/list/' + this.listTableParam.listid)
      if (cacheparam) {
        const param = JSON.parse(cacheparam)

        for (const key in param) {
          if (param.hasOwnProperty(key)) { // 这个检查是为了确保属性是对象自身的，而不是从原型链继承的
            this.listTableParam[key] = param[key]
          }
        }
      }
    }

    this.maxheight = (window.innerHeight - 350) + 'px'

    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 组件销毁时移除监听器，避免内存泄漏
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    handleResize() {
      this.maxheight = (window.innerHeight - 350) + 'px'
    },
    handleReferSelect(ID, sName) {
      this.$emit('handleReferSelect', {
        ID: ID,
        sName: sName
      })
    },
    handleSelectall() {
      this.$refs.datatable.clearSelection()
      if (this.selectall) {
        this.$refs.datatable.toggleAllSelection()
      }
    },
    getConfigList() {
      this.loading = true
      const that = this

      configlist(
        qs.stringify({
          sobjectname: this.sobjectname,
          listid: this.listid,
          relatedid: this.relatedid,
          type: this.listtype,
          objectid: this.objectid,
          sfromobjectname: this.sfromobjectname
        }),
        this.sobjectname.toLowerCase()
      ).then((res) => {
        that.listConfig = res.data.list
        that.arrListBtn = res.data.arrBtn

        if (that.listConfig.arrFastSearchId.length > 0) {
          that.listTableParam.fastsearchfield = that.listConfig.arrFastSearchId[0].sFieldAs
        }
        that.listTableParam.listid = that.listConfig.lID

        if (that.listConfig.arrAdvancedSearchId.length > 0) {
          that.advancedsearchvisible = true
        }

        that.loadData()
      })
    },
    formatParam() {
      const listTableParam = JSON.parse(JSON.stringify(this.listTableParam))

      // 格式化高级搜索项
      var advsearchjson = []
      for (var index in this.listTableParam.advsearchjson) {
        const value = this.listTableParam.advsearchjson[index].value
        const extraValue = this.listTableParam.advsearchjson[index].extraValue
        if (value === '全部' || !value) {
          continue
        }
        advsearchjson[advsearchjson.length] = {
          field: index,
          value: value,
          extraValue: extraValue
        }
      }

      if (this.defaultsearchjson) {
        for (const sFieldName in this.defaultsearchjson) {
          advsearchjson.push(this.defaultsearchjson[sFieldName])
        }
      }

      listTableParam.advsearchjson = JSON.stringify(advsearchjson)
      listTableParam.orderby = JSON.stringify(listTableParam.orderby)

      // 把参数缓存起来
      localStorage.setItem('/list/' + this.listTableParam.listid, JSON.stringify(this.listTableParam))

      return listTableParam
    },
    loadData() {
      this.loading = true
      const that = this
      list(qs.stringify(this.formatParam()), this.listTableParam.sobjectname.toLowerCase()).then((res) => {
        if (res.code === 10000) {
          this.listData = res.data.arrData
          this.pagination.total = res.data.lCount
          that.loading = false
          this.sumData = {}
          for (var i in res.data.arrSumField) {
            var sumField = res.data.arrSumField[i]
            this.sumData[sumField.sFieldAs] = sumField.fValue
          }

          if (res.data.arrSumField.length > 0) {
            this.isShowSummary = true
          } else {
            this.isShowSummary = false
          }

          this.page = this.listTableParam.page
        }
      }).catch(function(error) {
        that.loading = false
      })
    },
    updateAdvSearchValue(data) {
      this.listTableParam.advsearchjson = data.data

      if (data.reload) {
        this.loadData()
      }
    },
    // 表格多选
    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    // 处理新建
    handleCreate() {
      this.$router.push('/' + this.sobjectname + '/new')
    },
    // 处理删除
    handleDel() {
      if (this.multipleSelection.length === 0) {
        this.$notify.error({
          title: '警告',
          message: '请至少选择一条数据',
          type: 'warning'
        })
        return
      }

      const param = this.formatParam()
      if (!this.selectall) {
        for (const index in this.multipleSelection) {
          param.selectedids[param.selectedids.length] = this.multipleSelection[index].data.ID
        }
        param.selectedids = JSON.stringify(param.selectedids)
      } else {
        param.selectedids = 'all'
      }

      const that = this
      this.loading = true
      listdel(qs.stringify(param), this.listTableParam.sobjectname.toLowerCase()).then((res) => {
        this.selectall = false
        this.handleSelectall()

        this.loadData()
      }).catch(function(error) {
        that.loadData()
      })
    },
    handleSizeChange(val) {
      this.listTableParam.pagelimit = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.listTableParam.page = val
      this.loadData()
    },
    // 处理导出
    handleExport() {
      if (this.multipleSelection.length === 0) {
        this.$notify.error({
          title: '警告',
          message: '请至少选择一条数据',
          type: 'warning'
        })
        return
      }

      const param = this.formatParam()
      if (!this.selectall) {
        for (const index in this.multipleSelection) {
          param.selectedids[param.selectedids.length] = this.multipleSelection[index].ID
        }
        param.selectedids = JSON.stringify(param.selectedids)
      } else {
        param.selectedids = 'all'
      }

      this.loading = true
      listexport(qs.stringify(param), this.listTableParam.sobjectname.toLowerCase()).then((res) => {
        this.loading = false
        if (res.code === 10000) {
          const a = document.createElement('a') // 创建a标签
          a.href = this.$store.state.config.uploadUrl + '/temp/' + res.data.filename// 指定下载链接
          a.download = 'test' // 指定下载文件名，如果是跨域了，就会失效
          a.click() // 触发下载
          console.log(a)
          URL.revokeObjectURL(a.href) // 释放URL对象
        }
      })
    },
    // 处理排序事件
    handleSortChange(val) {
      if (val.order) {
        this.listTableParam.orderby.field = val.prop
        this.listTableParam.orderby.order = val.order
      } else {
        this.listTableParam.orderby.field = null
        this.listTableParam.orderby.order = null
      }
      this.listTableParam.page = 1
      this.loadData()
    },
    handleView(sObjectName, ID) {
      // const routeData = this.$router.resolve({
      //   path: '/' + sObjectName.toLowerCase() + '/view/' + ID
      // })
      // window.open(routeData.href, '_blank')

      this.$router.push('/' + sObjectName.toLowerCase() + '/view/' + ID)
    },
    indexMethod(index) {
      return index + (this.listTableParam.page - 1) * this.listTableParam.pagelimit + 1
    },
    handleRefresh() {
      this.selectall = false
      this.handleSelectall()
      this.getConfigList()
    },
    handleBatchSelect() {
      if (this.multipleSelection.length === 0) {
        this.$notify.error({
          title: '警告',
          message: '请至少选择一条数据',
          type: 'warning'
        })
        return
      }

      this.$emit('handleBatchSelect', this.multipleSelection, this.selectall)
    },
    getSummaries(param) {
      const { columns, data } = param
      const sums = []

      columns.forEach((column, index) => {
        if (index === 1) {
          sums[index] = '合计'
          return
        }
        if (this.sumData[column.property]) {
          sums[index] = this.sumData[column.property]
        } else {
          sums[index] = null
        }
      })

      return sums
    }
  }
}
</script>
<style lang="scss" scoped>
.list-data {
  position: relative;
}

.advancedsearch-container {
  background-color: #f5f7fa;
  position: relative;
  margin-bottom: 15px;
}

.advancedsearch-open {
  padding: 10px;
}

.el-form-item {
  margin-bottom: 0px;
}

.list-data .tool {
  height: 35px;
}

.list-data .datatable {}

.list-data .button {
  margin-bottom: 15px;
}

.list-data .tool .fastsearch {
  float: right;
  width: 400px;
  margin-right: 15px;
}

.list-data .tool .selectall {
  padding-top: 5px;
  float: left;
}

.pagination {
  margin: 10px 0;
  text-align: center;
}

a {
  color: #337ab7;
}

a:hover {
  text-decoration: underline;
}
</style>
