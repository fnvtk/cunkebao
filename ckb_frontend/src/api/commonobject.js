import request from '@/utils/request'

export function tablist(data, objname) {
  return request({
    url: '/v1/backend/' + objname + '/tab/list',
    method: 'post',
    data
  })
}

/**
 * 获取参照视图的列表
 * @param {*} data
 * @param {*} objname
 * @returns
 */
export function referlist(data, objname) {
  return request({
    url: '/v1/backend/' + objname + '/refer/list',
    method: 'post',
    data
  })
}

// 视图配置信息
export function configlist(data, objname) {
  return request({
    url: '/v1/backend/' + objname + '/config/list',
    method: 'post',
    data
  })
}

// 界面配置信息
export function uiConfig(data, objname) {
  return request({
    url: '/v1/backend/' + objname + '/config/ui',
    method: 'post',
    data
  })
}

// 视图列表
export function list(data, objname) {
  return request({
    url: '/v1/backend/' + objname + '/list',
    method: 'post',
    data
  })
}

// 视图中删除数据
export function listdel(data, objname) {
  return request({
    url: '/v1/backend/' + objname + '/del',
    method: 'post',
    data
  })
}

// 视图中导出数据
export function listexport(data, objname) {
  return request({
    url: '/v1/backend/' + objname + '/export',
    method: 'post',
    data
  })
}

// 获取对象配置
export function configobj(data, objname) {
  return request({
    url: '/v1/backend/' + objname + '/config/object',
    method: 'post',
    data
  })
}

// 获取对象详情数据
export function getViewData(data, objname) {
  return request({
    url: '/v1/backend/' + objname + '/view',
    method: 'post',
    data
  })
}

// 获取对象详情数据
export function getEditData(data, objname) {
  return request({
    url: '/v1/backend/' + objname + '/edit',
    method: 'post',
    data
  })
}
