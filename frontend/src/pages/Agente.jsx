import { useEffect, useState } from 'react'
import { api } from '../api'

const vazio = { matricula: '', ano: '', marca: '', modelo: '', placa: '', proprietario: 'EMPRESA' }

export default function Agente() {
  const [clientes, setClientes] = useState([])
  const [automoveis, setAutomoveis] = useState([])
  const [form, setForm] = useState(vazio)
  const [editando, setEditando] = useState(null)
  const [erro, setErro] = useState('')

  const carregar = () => {
    api.clientes().then(setClientes).catch((e) => setErro(e.message))
    api.automoveis().then(setAutomoveis)
  }
  useEffect(carregar, [])

  const set = (c) => (e) => setForm({ ...form, [c]: e.target.value })

  async function salvar(e) {
    e.preventDefault()
    setErro('')
    try {
      const corpo = { ...form, ano: Number(form.ano) }
      if (editando) await api.atualizarAutomovel(editando, corpo)
      else await api.criarAutomovel(corpo)
      setForm(vazio); setEditando(null); carregar()
    } catch (err) { setErro(err.message) }
  }

  function editar(a) {
    setEditando(a.id)
    setForm({ matricula: a.matricula, ano: a.ano, marca: a.marca, modelo: a.modelo, placa: a.placa, proprietario: a.proprietario })
    window.scrollTo(0, 0)
  }

  async function remover(a) {
    if (!confirm(`Remover ${a.marca} ${a.modelo}?`)) return
    setErro('')
    try { await api.removerAutomovel(a.id); carregar() } catch (err) { setErro(err.message) }
  }

  return (
    <>
      <form className="card" onSubmit={salvar}>
        <h2>{editando ? 'Editar automóvel' : 'Cadastrar automóvel'}</h2>
        <div className="grid">
          <div><label>Matrícula</label><input value={form.matricula} onChange={set('matricula')} required /></div>
          <div><label>Placa</label><input value={form.placa} onChange={set('placa')} required /></div>
          <div><label>Marca</label><input value={form.marca} onChange={set('marca')} required /></div>
          <div><label>Modelo</label><input value={form.modelo} onChange={set('modelo')} required /></div>
          <div><label>Ano</label><input type="number" value={form.ano} onChange={set('ano')} required /></div>
          <div>
            <label>Propriedade</label>
            <select value={form.proprietario} onChange={set('proprietario')}>
              <option value="CLIENTE">Cliente</option><option value="EMPRESA">Empresa</option><option value="BANCO">Banco</option>
            </select>
          </div>
        </div>
        {erro && <p className="erro">{erro}</p>}
        <div className="acoes">
          <button className="pri" type="submit">{editando ? 'Salvar' : 'Cadastrar'}</button>
          {editando && <button type="button" onClick={() => { setEditando(null); setForm(vazio) }}>Cancelar</button>}
        </div>
      </form>

      <h2>Automóveis ({automoveis.length})</h2>
      {automoveis.map((a) => (
        <div className="card linha" key={a.id}>
          <span>{a.marca} {a.modelo} {a.ano} · placa {a.placa} · matrícula {a.matricula} · <span className="badge">{a.proprietario}</span></span>
          <span className="acoes">
            <button onClick={() => editar(a)}>Editar</button>
            <button className="bad" onClick={() => remover(a)}>Remover</button>
          </span>
        </div>
      ))}

      <h2>Clientes ({clientes.length})</h2>
      {clientes.map((c) => (
        <div className="card" key={c.id}>
          <strong>{c.nome}</strong> · CPF {c.cpf} · RG {c.rg}
          <p className="mut">{c.endereco} · {c.profissao || 'profissão não informada'}</p>
        </div>
      ))}
    </>
  )
}
