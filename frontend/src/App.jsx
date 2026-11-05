import { useEffect, useState } from 'react'
import { api, setCredenciais, restaurarCredenciais } from './api'
import Auth from './pages/Auth.jsx'
import Pedidos from './pages/Pedidos.jsx'
import Perfil from './pages/Perfil.jsx'
import Agente from './pages/Agente.jsx'

export default function App() {
  const [usuario, setUsuario] = useState(null)
  const [aba, setAba] = useState('pedidos')
  const [carregando, setCarregando] = useState(true)

  useEffect(() => {
    if (!restaurarCredenciais()) return setCarregando(false)
    api.login().then(setUsuario).catch(() => setCredenciais(null)).finally(() => setCarregando(false))
  }, [])

  function sair() {
    setCredenciais(null)
    setUsuario(null)
  }

  if (carregando) return null
  if (!usuario) return <Auth onLogin={setUsuario} />

  const agente = usuario.papel !== 'CLIENTE'
  return (
    <>
      <header>
        <strong>🚗 Aluguel de Carros</strong>
        <nav>
          <button className={aba === 'pedidos' ? 'on' : ''} onClick={() => setAba('pedidos')}>Pedidos</button>
          {agente
            ? <button className={aba === 'agente' ? 'on' : ''} onClick={() => setAba('agente')}>Clientes e automóveis</button>
            : <button className={aba === 'perfil' ? 'on' : ''} onClick={() => setAba('perfil')}>Meu cadastro</button>}
          <button onClick={sair}>Sair ({usuario.login})</button>
        </nav>
      </header>
      <main>
        {aba === 'pedidos' && <Pedidos usuario={usuario} />}
        {aba === 'perfil' && !agente && <Perfil usuario={usuario} onExcluido={sair} />}
        {aba === 'agente' && agente && <Agente />}
      </main>
    </>
  )
}
