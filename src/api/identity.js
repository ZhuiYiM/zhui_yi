import request from './request'

/**
 * 获取用户身份信息
 * @param {number} userId - 用户 ID
 * @returns {Promise}
 */
export function getUserIdentity(userId) {
  return request({
    url: `/api/user/${userId}/identity`,
    method: 'get'
  })
}

/**
 * 获取用户公开信息（包含身份信息）
 * @param {number} userId - 用户 ID
 * @returns {Promise}
 */
export function getUserPublicInfo(userId) {
  return request({
    url: `/api/user/${userId}/public-info`,
    method: 'get'
  })
}
