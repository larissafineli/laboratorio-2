import { useEffect, useState } from 'react'
import { api } from '../api'
import FormCliente from '../components/FormCliente.jsx'

export default function Perfil({ usuario, onExcluido }) {
  const [cliente, setCliente] = useState(null)
  const [msg, setMsg] = useState('')
  const [erro, setErro] = useState('')

  useEffect(() => {
    api.cliente(usuario.clienteId).then(setCliente).catch((e) => setErro(e.message))
  }, [usuario.clienteId])

  async function salvar(e) {
    e.preventDefault()
    setErro(''); setMsg('')
    try {
      setCliente(await api.atualizarCliente(usuario.clienteId, cliente))
      setMsg('Cadastro atualizado')
    } catch (err) { setErro(err.message) }
  }

  async function excluir() {
    if (!confirm('Excluir sua conta definitivamente?')) return
    try {
      await api.removerCliente(usuario.clienteId)
      onExcluido()
    } catch (err) { setErro(err.message) }
  }

  if (!cliente) return <p className="erro">{erro}</p>
  return (
    <form className="card" onSubmit={salvar}>
      <h2>Meu cadastro</h2>
      <FormCliente valor={cliente} onChange={setCliente} />
      {erro && <p className="erro">{erro}</p>}
      {msg && <p className="ok">{msg}</p>}
      <div className="acoes">
        <button className="pri" type="submit">Salvar</button>
        <button type="button" className="bad" onClick={excluir}>Excluir conta</button>
      </div>
    </form>
  )
}
