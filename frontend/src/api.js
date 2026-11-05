const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

let credenciais = null // Basic Auth em memoria + sessionStorage

export function setCredenciais(login, senha) {
  credenciais = login ? btoa(`${login}:${senha}`) : null
  try {
    if (credenciais) sessionStorage.setItem('cred', credenciais)
    else sessionStorage.removeItem('cred')
  } catch { /* ignora */ }
}

export function restaurarCredenciais() {
  try { credenciais = sessionStorage.getItem('cred') } catch { credenciais = null }
  return !!credenciais
}

async function req(metodo, caminho, corpo) {
  const headers = { 'Content-Type': 'application/json' }
  if (credenciais) headers.Authorization = `Basic ${credenciais}`
  const res = await fetch(BASE + caminho, {
    method: metodo,
    headers,
    body: corpo ? JSON.stringify(corpo) : undefined,
  })
  if (res.status === 204) return null
  const dados = await res.json().catch(() => ({}))
  if (!res.ok) {
    throw new Error(res.status === 401 ? 'Usuário ou senha inválidos' : dados.erro || dados.message || `Erro ${res.status}`)
  }
  return dados
}

export const api = {
  registrar: (b) => req('POST', '/auth/register', b),
  login: () => req('POST', '/auth/login'),
  bancos: () => req('GET', '/bancos'),
  automoveis: () => req('GET', '/automoveis'),
  criarAutomovel: (b) => req('POST', '/automoveis', b),
  atualizarAutomovel: (id, b) => req('PUT', `/automoveis/${id}`, b),
  removerAutomovel: (id) => req('DELETE', `/automoveis/${id}`),
  clientes: () => req('GET', '/clientes'),
  cliente: (id) => req('GET', `/clientes/${id}`),
  atualizarCliente: (id, b) => req('PUT', `/clientes/${id}`, b),
  removerCliente: (id) => req('DELETE', `/clientes/${id}`),
  pedidos: () => req('GET', '/pedidos'),
  criarPedido: (b) => req('POST', '/pedidos', b),
  modificarPedido: (id, b) => req('PUT', `/pedidos/${id}`, b),
  cancelarPedido: (id) => req('POST', `/pedidos/${id}/cancelar`),
  avaliarPedido: (id, b) => req('POST', `/pedidos/${id}/avaliar`, b),
  contratarPedido: (id) => req('POST', `/pedidos/${id}/contratar`),
}
