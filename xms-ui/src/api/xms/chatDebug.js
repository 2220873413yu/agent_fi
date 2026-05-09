import axios from 'axios'

export function askChat(baseUrl, token, data, hutool) {
  return axios({
    url: normalizeBaseUrl(baseUrl) + (hutool ? '/api/chat/hutool/ask' : '/api/chat/ask'),
    method: 'post',
    timeout: 60000,
    headers: buildHeaders(token),
    data
  }).then(res => res.data)
}

export function listChatSessions(baseUrl, token, params) {
  return axios({
    url: normalizeBaseUrl(baseUrl) + '/api/chat/sessionList',
    method: 'get',
    timeout: 10000,
    headers: buildHeaders(token),
    params
  }).then(res => res.data)
}

export function listChatMessages(baseUrl, token, params) {
  return axios({
    url: normalizeBaseUrl(baseUrl) + '/api/chat/messageList',
    method: 'get',
    timeout: 10000,
    headers: buildHeaders(token),
    params
  }).then(res => res.data)
}

function buildHeaders(token) {
  const headers = {
    'Content-Type': 'application/json;charset=utf-8'
  }
  if (token) {
    headers.Authorization = token
  }
  return headers
}

function normalizeBaseUrl(baseUrl) {
  return (baseUrl || '').replace(/\/+$/, '')
}
