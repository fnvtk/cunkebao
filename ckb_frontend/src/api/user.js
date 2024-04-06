import request from '@/utils/request'
import qs from 'qs'

export function login(data) {
  return request({
    url: '/v1/backend/system/sysuser/login',
    method: 'post',
    data: qs.stringify(data)
  })
}

export function getInfo(token) {
  return request({
    url: '/v1/backend/system/sysuser/profile/info',
    method: 'post',
    params: { token }
  })
}

export function logout() {
  return request({
    url: '/v1/backend/system/sysuser/loginout',
    method: 'post'
  })
}
