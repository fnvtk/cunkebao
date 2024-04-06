import request from '@/utils/request'

export function loadConfig() {
  return request({
    url: '/config.json',
    method: 'get'
  })
}
