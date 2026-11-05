import { useEffect, useState } from 'react'
import { api } from '../api'

const vazio = { automovelId: '', dataInicio: '', dataFim: '', valorDiaria: '', bancoId: '', valorCredito: '', taxaJurosMensal: '' }

export default function Pedidos({ usuario }) {
  const agente = usuario.papel !== 'CLIENTE'
  const [pedidos, setPedidos] = useState([])
  const [automoveis, setAutomoveis] = useState([])
  const [bancos, setBancos] = useState([])
  const [form, setForm] = useState(vazio)
  const [editando, setEditando] = useState(null)
  const [erro, setErro] = useState('')

  const carregar = () => api.pedidos().then(setPedidos).catch((e) => setErro(e.message))

  useEffect(() => {
    carregar()
    api.automoveis().then(setAutomoveis)
    api.bancos().then(setBancos)
  }, [])

  const set = (c) => (e) => setForm({ ...form, [c]: e.target.value })

  function paraRequest() {
    const f = { ...form, automovelId: Number(form.automovelId), valorDiaria: Number(form.valorDiaria) }
    if (f.bancoId) {
      f.bancoId = Number(f.bancoId); f.valorCredito = Number(f.valorCredito); f.taxaJurosMensal = Number(f.taxaJurosMensal)
    } else {
      f.bancoId = null; f.valorCredito = null; f.taxaJurosMensal = null
    }
    return f
  }

  async function agir(fn) {
    setErro('')
    try { await fn(); await carregar() } catch (e) { setErro(e.message) }
  }

  async function salvar(e) {
    e.preventDefault()
    await agir(async () => {
      if (editando) await api.modificarPedido(editando, paraRequest())
      else await api.criarPedido(paraRequest())
      setForm(vazio); setEditando(null)
    })
  }

  function editar(p) {
    setEditando(p.id)
    setForm({
      automovelId: p.automovel.id, dataInicio: p.dataInicio, dataFim: p.dataFim, valorDiaria: p.valorDiaria,
      bancoId: p.contratoCredito?.banco.id ?? '', valorCredito: p.contratoCredito?.valor ?? '',
      taxaJurosMensal: p.contratoCredito?.taxaJurosMensal ?? '',
    })
    window.scrollTo(0, 0)
  }

  async function avaliar(id, aprovado) {
    const parecer = prompt('Parecer financeiro (opcional):') ?? ''
    await agir(() => api.avaliarPedido(id, { aprovado, parecer }))
  }

  const encerrado = (p) => p.status === 'CANCELADO' || p.status === 'CONTRATADO'

  return (
    <>
      {(!agente || editando) && (
        <form className="card" onSubmit={salvar}>
          <h2>{editando ? `Modificar pedido #${editando}` : 'Novo pedido de aluguel'}</h2>
          <label>Automóvel</label>
          <select value={form.automovelId} onChange={set('automovelId')} required>
            <option value="">Selecione…</option>
            {automoveis.map((a) => <option key={a.id} value={a.id}>{a.marca} {a.modelo} {a.ano} — {a.placa}</option>)}
          </select>
          <div className="grid">
            <div><label>Início</label><input type="date" value={form.dataInicio} onChange={set('dataInicio')} required /></div>
            <div><label>Fim</label><input type="date" value={form.dataFim} onChange={set('dataFim')} required /></div>
          </div>
          <label>Valor da diária (R$)</label>
          <input type="number" step="0.01" min="0.01" value={form.valorDiaria} onChange={set('valorDiaria')} required />

          <label>Contrato de crédito (opcional)</label>
          <select value={form.bancoId} onChange={set('bancoId')}>
            <option value="">Sem crédito</option>
            {bancos.map((b) => <option key={b.id} value={b.id}>{b.nome}</option>)}
          </select>
          {form.bancoId && (
            <div className="grid">
              <div><label>Valor do crédito (R$)</label><input type="number" step="0.01" min="0.01" value={form.valorCredito} onChange={set('valorCredito')} required /></div>
              <div><label>Juros mensais (%)</label><input type="number" step="0.01" min="0" value={form.taxaJurosMensal} onChange={set('taxaJurosMensal')} required /></div>
            </div>
          )}
          <div className="acoes">
            <button className="pri" type="submit">{editando ? 'Salvar alterações' : 'Enviar pedido'}</button>
            {editando && <button type="button" onClick={() => { setEditando(null); setForm(vazio) }}>Cancelar edição</button>}
          </div>
        </form>
      )}

      {erro && <p className="erro">{erro}</p>}
      <h2>{agente ? 'Pedidos para análise' : 'Meus pedidos'}</h2>
      {pedidos.length === 0 && <p className="mut">Nenhum pedido.</p>}
      {pedidos.map((p) => (
        <div className="card" key={p.id}>
          <div className="linha">
            <strong>#{p.id} — {p.automovel.marca} {p.automovel.modelo} ({p.automovel.placa})</strong>
            <span className={`badge ${p.status}`}>{p.status.replace('_', ' ')}</span>
          </div>
          <p className="mut">
            {agente && <>Cliente: {p.cliente.nome} (CPF {p.cliente.cpf}) · </>}
            {p.dataInicio} a {p.dataFim} · R$ {p.valorDiaria}/dia
          </p>
          {agente && <p className="mut">Rendimentos: {p.cliente.rendimentos.map((r) => `${r.entidadeEmpregadora} R$ ${r.valor}`).join(' | ') || '—'}</p>}
          {p.contratoCredito && (
            <p className="mut">Crédito: {p.contratoCredito.banco.nome} · R$ {p.contratoCredito.valor} · {p.contratoCredito.taxaJurosMensal}% a.m.</p>
          )}
          {p.parecer && <p className="mut">Parecer: {p.parecer}</p>}
          <div className="acoes">
            {!encerrado(p) && <button onClick={() => editar(p)}>Modificar</button>}
            {!agente && !encerrado(p) && <button className="bad" onClick={() => agir(() => api.cancelarPedido(p.id))}>Cancelar</button>}
            {agente && p.status === 'EM_ANALISE' && <>
              <button className="pri" onClick={() => avaliar(p.id, true)}>Aprovar</button>
              <button className="bad" onClick={() => avaliar(p.id, false)}>Reprovar</button>
            </>}
            {agente && p.status === 'APROVADO' && <button className="pri" onClick={() => agir(() => api.contratarPedido(p.id))}>Executar contrato</button>}
          </div>
        </div>
      ))}
    </>
  )
}
