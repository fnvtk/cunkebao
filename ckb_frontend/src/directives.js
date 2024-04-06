import Vue from 'vue'
const directives = Vue.directive('loadmore', {
  bind(el, binding) {
  // 获取element-ui定义好的scroll盒子
    const SELECTWRAP_DOM = el.querySelector('.el-select-dropdown .el-select-dropdown__wrap')
    SELECTWRAP_DOM.addEventListener('scroll', function() {
      const CONDITION = this.scrollHeight - this.scrollTop - 20 <= this.clientHeight
      if (CONDITION) {
        binding.value()
      }
    })
  }
})

export default directives
