<template>
  <div>
    <div :class="opened ? 'advancedsearch-open' : 'advancedsearch-open hide'">
      <el-form label-suffix=":" label-width="80px" label-position="left" size="small">
        <div v-for="(field, index) in fields" :key="index">
          <advancedsearchfield
            v-if="field.sDataType == 'Date' || field.sDataType == 'DateTime'"
            ref="field"
            :field="field"
            :defaultvalue="advancedSearchFieldValue[field.sFieldAs]"
            @updateSearchValue="updateSearchValue"
          />
        </div>
        <div v-for="(field, index) in fields" :key="index">
          <advancedsearchfield
            v-if="field.sDataType == 'Bool'"
            ref="field"
            :field="field"
            :defaultvalue="advancedSearchFieldValue[field.sFieldAs]"
            @updateSearchValue="updateSearchValue"
          />
        </div>
        <div v-for="(field, index) in fields" :key="index">
          <advancedsearchfield
            v-if="field.sDataType == 'List' || field.sDataType == 'MultiList'"
            ref="field"
            :field="field"
            :defaultvalue="advancedSearchFieldValue[field.sFieldAs]"
            @updateSearchValue="updateSearchValue"
          />
        </div>
        <div class="other">
          <ul>
            <li
              v-for="(field, index) in fields"
              :key="index"
            >
              <advancedsearchfield
                v-if="field.sDataType !== 'List' && field.sDataType !== 'MultiList' && field.sDataType !== 'Bool' && field.sDataType !== 'Date' && field.sDataType !== 'DateTime'"
                ref="field"
                :field="field"
                :defaultvalue="advancedSearchFieldValue[field.sFieldAs]"
                @updateSearchValue="updateSearchValue"
              />
            </li>
          </ul>
          <div class="clearfloat" />
        </div>
      </el-form>
      <div class="button" @click.prevent="openAdvancedSearch"><i class="el-icon-arrow-up" /></div>
    </div>
    <div :class="opened ? 'advancedsearch-closed hide' : 'advancedsearch-closed'">
      <div class="left">筛选条件：</div>
      <div v-if="displaysummary" class="right">
        <div v-for="(field, index) in fields" :key="index" class="item">{{ field.sName }}: <span
          v-if="advancedSearchFieldValue[field.sFieldAs]"
        >{{ advancedSearchFieldValue[field.sFieldAs].value ?
          advancedSearchFieldValue[field.sFieldAs].value : "全部" }}</span><span v-else>全部</span></li>
        </div>
      </div>
      <div class="clearfloat" />
      <div class="button" @click.prevent="openAdvancedSearch"><i class="el-icon-arrow-down" /></div>
    </div>

  </div>
</template>
<script>
import Advancedsearchfield from '@/components/ListTable/advancedsearchfield'
export default {
  name: 'Advancedsearch',
  components: { Advancedsearchfield },
  props: ['fields'],
  data() {
    return {
      displaysummary: true,
      opened: false,
      advancedSearchFieldValue: {
      }
    }
  },
  updated() {
    this.displaysummary = true
  },
  methods: {
    updateSearchValue(data) {
      this.advancedSearchFieldValue[data.sFieldAs] = {
        value: data.value,
        extraValue: data.extraValue
      }
      this.displaysummary = true
      this.$emit('updateSearchValue', { data: this.advancedSearchFieldValue, reload: data.reload })
    },
    openAdvancedSearch() {
      this.opened = !this.opened
    }
  }
}
</script>
<style lang="scss" scoped>
.hide {
  display: none;
}

.advancedsearch-closed {
  padding: 15px 10px 0px;
  // height: 55px;
}

.advancedsearch-closed .left {
  float: left;
  width: 80px;
  height: 28px;
  color: #666666;
  font-size: 13px;
  line-height: 28px;
}

.advancedsearch-closed .right {
  float: left;
  width: calc(100% - 240px);
}

.advancedsearch-closed .right .item {
  float: left;
  margin-right: 15px;
  margin-bottom: 15px;
  padding: 5px 12px;
  color: #459df5;
  background-color: #deefff;
  font-size: 13px;
}

.advancedsearch-open {
  padding: 10px;
}

ul,
ol {
  list-style: none;
  margin: 0;
  padding: 0;
  height: 40px;
}

li {
  float: left;
  margin-right: 15px;
}

.advancedsearch-open .button,
.advancedsearch-closed .button {
  cursor: pointer;
  width: 40px;
  height: 18px;
  text-align: center;
  background-color: #f5f7fa;
  position: absolute;
  left: 50%;
  bottom: -27px;
  -webkit-transform: translate(-50%, -50%);
  -ms-transform: translate(-50%, -50%);
  transform: translate(-50%, -50%);
}

.advancedsearch-open .button:before,
.advancedsearch-closed .button:before {
  content: "";
  display: block;
  width: 20px;
  height: 18px;
  position: absolute;
  transform: skewX(40deg);
  background-color: #f5f7fa;
  left: -11px;
  top: 0;
}

.advancedsearch-open .button:after,
.advancedsearch-closed .button:after {
  content: "";
  display: block;
  width: 20px;
  height: 18px;
  position: absolute;
  transform: skewX(-40deg);
  background-color: #f5f7fa;
  top: 0;
  right: -11px;
}

.clearfloat {
  clear: both;
  height: 0;
  font-size: 1px;
  line-height: 0px;
}
</style>
