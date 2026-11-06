import { useEffect, useState } from 'react'

// Converte "dd/mm/aaaa" -> "aaaa-mm-dd" (ou '' se invalida)
export function brParaIso(br) {
  const m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(br)
  if (!m) return ''
  const [, d, mes, a] = m
  const dt = new Date(Number(a), Number(mes) - 1, Number(d))
  const ok = dt.getFullYear() === Number(a) && dt.getMonth() === Number(mes) - 1 && dt.getDate() === Number(d)
  return ok ? `${a}-${mes}-${d}` : ''
}

export function isoParaBr(iso) {
  const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(iso || '')
  return m ? `${m[3]}/${m[2]}/${m[1]}` : ''
}

export function hojeIso() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

function mascara(txt) {
  const n = txt.replace(/\D/g, '').slice(0, 8)
  if (n.length <= 2) return n
  if (n.length <= 4) return `${n.slice(0, 2)}/${n.slice(2)}`
  return `${n.slice(0, 2)}/${n.slice(2, 4)}/${n.slice(4)}`
}

/** Campo de data no formato brasileiro. value/onChange trabalham em ISO (aaaa-mm-dd). */
export default function CampoData({ label, value, onChange, min, avisoMin }) {
  const [texto, setTexto] = useState(isoParaBr(value))

  // Sincroniza quando o valor muda por fora (editar pedido, limpar formulario)
  useEffect(() => {
    if (brParaIso(texto) !== (value || '')) setTexto(isoParaBr(value))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value])

  function alterar(e) {
    const t = mascara(e.target.value)
    setTexto(t)
    onChange(brParaIso(t))
  }

  const completo = texto.length === 10
  let aviso = ''
  if (completo && !value) aviso = 'Data inválida. Use o formato dd/mm/aaaa.'
  else if (value && min && value < min) aviso = avisoMin

  return (
    <div>
      <label>{label}</label>
      <input
        value={texto}
        onChange={alterar}
        placeholder="dd/mm/aaaa"
        inputMode="numeric"
        maxLength={10}
        required
        aria-invalid={!!aviso}
        style={aviso ? { borderColor: 'var(--bad)' } : undefined}
      />
      {aviso && <div className="campo-aviso">{aviso}</div>}
    </div>
  )
}
