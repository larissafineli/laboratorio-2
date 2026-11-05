export const clienteVazio = {
  rg: '', cpf: '', nome: '', endereco: '', profissao: '', rendimentos: [],
}

export default function FormCliente({ valor, onChange }) {
  const set = (campo) => (e) => onChange({ ...valor, [campo]: e.target.value })
  const rend = valor.rendimentos

  const setRend = (i, campo, v) =>
    onChange({ ...valor, rendimentos: rend.map((r, j) => (j === i ? { ...r, [campo]: v } : r)) })

  return (
    <>
      <div className="grid">
        <div><label>Nome</label><input value={valor.nome} onChange={set('nome')} required /></div>
        <div><label>CPF (11 dígitos)</label><input value={valor.cpf} onChange={set('cpf')} required pattern="\d{11}" /></div>
        <div><label>RG</label><input value={valor.rg} onChange={set('rg')} required /></div>
        <div><label>Profissão</label><input value={valor.profissao || ''} onChange={set('profissao')} /></div>
      </div>
      <label>Endereço</label>
      <input value={valor.endereco} onChange={set('endereco')} required />

      <label>Rendimentos (máximo 3)</label>
      {rend.map((r, i) => (
        <div className="grid" key={i}>
          <input placeholder="Entidade empregadora" value={r.entidadeEmpregadora} required
                 onChange={(e) => setRend(i, 'entidadeEmpregadora', e.target.value)} />
          <div className="linha">
            <input type="number" step="0.01" min="0" placeholder="Valor" value={r.valor} required
                   onChange={(e) => setRend(i, 'valor', e.target.value)} />
            <button type="button" className="bad" onClick={() => onChange({ ...valor, rendimentos: rend.filter((_, j) => j !== i) })}>×</button>
          </div>
        </div>
      ))}
      {rend.length < 3 && (
        <div className="acoes">
          <button type="button" onClick={() => onChange({ ...valor, rendimentos: [...rend, { entidadeEmpregadora: '', valor: '' }] })}>
            + Rendimento
          </button>
        </div>
      )}
    </>
  )
}
