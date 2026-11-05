import { useState } from 'react'
import { api, setCredenciais } from '../api'
import FormCliente, { clienteVazio } from '../components/FormCliente.jsx'

export default function Auth({ onLogin }) {
  const [modo, setModo] = useState('login')
  const [login, setLogin] = useState('')
  const [senha, setSenha] = useState('')
  const [papel, setPapel] = useState('CLIENTE')
  const [nome, setNome] = useState('')
  const [cliente, setCliente] = useState(clienteVazio)
  const [erro, setErro] = useState('')

  async function entrar(l = login, s = senha) {
    setCredenciais(l, s)
    try {
      onLogin(await api.login())
    } catch (e) {
      setCredenciais(null)
      throw e
    }
  }

  async function submit(e) {
    e.preventDefault()
    setErro('')
    try {
      if (modo === 'registro') {
        await api.registrar({
          login, senha, papel,
          nome: papel === 'CLIENTE' ? cliente.nome : nome,
          cliente: papel === 'CLIENTE' ? cliente : null,
        })
      }
      await entrar()
    } catch (err) {
      setErro(err.message)
    }
  }

  return (
    <div className="auth"><div className="auth-box">
      <div className="marca">
        <div className="logo">🚗</div>
        <h1>Aluguel de Carros</h1>
        <p>Peça, acompanhe e contrate seu aluguel online</p>
      </div>
      <form className="card" onSubmit={submit}>
        <h2>{modo === 'login' ? 'Entrar' : 'Cadastro'}</h2>
        <div className="grid">
          <div>
            <label>Usuário</label>
            <input value={login} onChange={(e) => setLogin(e.target.value)} required minLength={3} />
          </div>
          <div>
            <label>Senha</label>
            <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required minLength={modo === 'registro' ? 6 : 1} />
          </div>
        </div>

        {modo === 'registro' && (
          <>
            <label>Tipo de conta</label>
            <select value={papel} onChange={(e) => setPapel(e.target.value)}>
              <option value="CLIENTE">Cliente</option>
              <option value="AGENTE_EMPRESA">Agente - Empresa</option>
              <option value="AGENTE_BANCO">Agente - Banco</option>
            </select>
            {papel === 'CLIENTE'
              ? <FormCliente valor={cliente} onChange={setCliente} />
              : <><label>Nome da instituição</label><input value={nome} onChange={(e) => setNome(e.target.value)} required /></>}
          </>
        )}

        {erro && <p className="erro">{erro}</p>}
        <div className="acoes">
          <button className="pri" type="submit">{modo === 'login' ? 'Entrar' : 'Cadastrar'}</button>
          <button type="button" onClick={() => { setModo(modo === 'login' ? 'registro' : 'login'); setErro('') }}>
            {modo === 'login' ? 'Criar conta' : 'Já tenho conta'}
          </button>
        </div>
      </form>
    </div></div>
  )
}
