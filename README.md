# 🚗 Sistema de Aluguel de Carros

Sistema web para efetuar, modificar, consultar e cancelar pedidos de aluguel de automóveis, com análise financeira por agentes (empresas e bancos). Laboratório 02 — DIAW, Engenharia de Software, PUC Minas.

![Java](https://img.shields.io/badge/Java-25-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.x-brightgreen) ![React](https://img.shields.io/badge/React-Vite-blue) ![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-3ecf8e)

## 📑 Sumário
- [Sobre o projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Instalação e execução](#-instalação-e-execução)
- [Estrutura de pastas](#-estrutura-de-pastas)
- [Demonstração](#-demonstração)
- [Testes](#-testes)
- [Documentação](#-documentação)
- [Autores](#-autores)

## 📖 Sobre o projeto
O sistema só pode ser usado após cadastro prévio e login. Clientes introduzem, modificam, consultam e cancelam pedidos; agentes (empresas e bancos) modificam e avaliam pedidos. Pedidos aprovados na análise financeira podem ter o contrato executado. Um aluguel pode estar associado a um contrato de crédito concedido por um banco agente.

## ✅ Funcionalidades
- Cadastro e login (clientes e agentes); acesso restrito a usuários autenticados.
- **CRUD de cliente**: RG, CPF, nome, endereço, profissão e até 3 rendimentos (entidade empregadora + valor).
- **Pedidos**: introduzir, consultar status, modificar, cancelar; crédito opcional vinculado a um banco.
- **Agentes**: listar/modificar pedidos, aprovar/reprovar com parecer, executar contrato, gerir automóveis (CRUD).
- **Automóveis**: matrícula, ano, marca, modelo, placa e propriedade (cliente, empresa ou banco).

## 🛠 Tecnologias
| Camada | Tecnologia |
|---|---|
| Back-end | Java 25, Spring Boot 4.0.x (MVC), Spring Security, Spring Data JPA |
| Build | Apache Maven |
| Front-end | React + Vite |
| Banco de dados | Supabase (PostgreSQL) |
| Autenticação | Usuário e senha (BCrypt + HTTP Basic stateless) |
| Testes | JUnit 5, H2 em memória |

## 🏗 Arquitetura
MVC no back-end (`controller` → `service` → `repository` → `model`), com `dto`, `config` (segurança/CORS) e `exception`. O front-end React consome a API REST (JSON) e monta as páginas dinamicamente. Dois subsistemas: gestão de pedidos e contratos (back-end) e construção dinâmica das páginas (front-end).

Diagramas em [docs/modelagem.md](docs/modelagem.md) (Mermaid) e [docs/uml/](docs/uml) (PlantUML, versões v1 e v2).

## ▶ Instalação e execução

**Pré-requisitos:** JDK 25, Maven 3.9+, Node.js LTS, projeto no [Supabase](https://supabase.com).

### Variáveis de ambiente (back-end)
Veja [.env.example](.env.example). Copie a connection string JDBC em *Project Settings → Database*.

```bash
export SUPABASE_DB_URL="jdbc:postgresql://<host>:5432/postgres?sslmode=require"
export SUPABASE_DB_USER="postgres.<project-ref>"
export SUPABASE_DB_PASSWORD="<senha>"
export APP_SEED=true   # opcional: cria dados de demonstração
```
No PowerShell use `$env:NOME = "valor"`. As tabelas são criadas pelo Hibernate (`ddl-auto=update`).

### Back-end
```bash
cd backend
mvn spring-boot:run      # http://localhost:8080/api
```

#### Sem Supabase (perfil `local`, banco H2 em memória + dados de demonstração)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
Os dados são perdidos ao reiniciar. Para a entrega final, use o Supabase.

### Front-end
```bash
cd frontend
cp .env.example .env     # VITE_API_URL=http://localhost:8080/api
npm install
npm run dev              # http://localhost:5173
```

### Dados de demonstração (`APP_SEED=true`)
| Usuário | Senha | Papel |
|---|---|---|
| `cliente` | `123456` | Cliente |
| `empresa` | `123456` | Agente - Empresa |
| `banco` | `123456` | Agente - Banco |

### Build de produção
```bash
cd frontend && npm run build        # gera frontend/dist
cd backend && mvn clean package     # gera target/*.jar
```

## 📁 Estrutura de pastas
```
.
├── backend/            # Spring Boot (controller, service, repository, model, dto, config, exception)
├── frontend/           # React + Vite (pages, components, api.js)
├── docs/
│   ├── modelagem.md    # histórias, casos de uso, classes, pacotes (Mermaid)
│   ├── uml/v1, v2/     # PlantUML por versão
│   └── apresentacao.md # roteiro: modelo inicial x final
└── README.md
```

## 🎬 Demonstração
1. Entre como `cliente` e crie um pedido (com ou sem crédito) → **EM ANÁLISE**.
2. Entre como `empresa` ou `banco`, aprove com parecer e execute o contrato.
3. O cliente vê o pedido **CONTRATADO**. Cancelamento e modificação seguem as regras do ciclo de vida.

### API
| Método | Rota | Quem |
|---|---|---|
| POST | `/api/auth/register` | público |
| POST | `/api/auth/login`, GET `/api/auth/me` | autenticado |
| GET/PUT/DELETE | `/api/clientes/{id}` | próprio cliente ou agente |
| GET | `/api/clientes` | agente |
| GET | `/api/automoveis` | autenticado |
| POST/PUT/DELETE | `/api/automoveis[/{id}]` | agente |
| GET/POST | `/api/pedidos` | autenticado / cliente |
| PUT | `/api/pedidos/{id}` | dono do pedido ou agente |
| POST | `/api/pedidos/{id}/cancelar` | cliente |
| POST | `/api/pedidos/{id}/avaliar`, `/contratar` | agente |

## 🧪 Testes
```bash
cd backend && mvn test
```
16 testes: regras de negócio (ciclo de vida do pedido, permissões, limite de 3 rendimentos, login duplicado) e API HTTP de ponta a ponta (401 sem login, 403 por papel, fluxo cadastro → pedido → aprovação → contrato).

> Se o VS Code recompilar o projeto com outro JDK e aparecer `UnsupportedClassVersionError`, rode o Maven com `clean`.

## 📚 Documentação
- [Modelagem](docs/modelagem.md) · [UML PlantUML](docs/uml) · [Roteiro da apresentação](docs/apresentacao.md)

## 👥 Autores
- Christiano Gonara — PUC Minas, Engenharia de Software
