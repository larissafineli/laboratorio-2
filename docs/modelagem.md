# Modelagem do Sistema de Aluguel de Carros

> Versão 1.0 — diagramas em Mermaid (renderizados automaticamente pelo GitHub).
> Ao alterar o código, atualize este arquivo e registre a mudança no histórico ao final.

## 1. Histórias do Usuário

| ID | Como… | Quero… | Para… | Critérios de aceite |
|----|-------|--------|-------|---------------------|
| US01 | visitante | me cadastrar como cliente (RG, CPF, nome, endereço, profissão, até 3 rendimentos) | usar o sistema | CPF com 11 dígitos e único; login único; senha ≥ 6 caracteres; máx. 3 rendimentos |
| US02 | agente (empresa/banco) | me cadastrar como agente | avaliar pedidos | login único |
| US03 | usuário cadastrado | fazer login com usuário e senha | acessar o sistema | sem login, nenhuma rota (exceto cadastro) é acessível; erro claro para credenciais inválidas |
| US04 | cliente | consultar e modificar meu cadastro | manter dados atualizados | CPF continua único |
| US05 | cliente | excluir meu cadastro | encerrar minha conta | bloqueado se houver pedidos |
| US06 | cliente | introduzir um pedido de aluguel (automóvel, período, diária, crédito opcional) | alugar um carro | pedido nasce `EM_ANALISE`; data final ≥ inicial |
| US07 | cliente | consultar o status dos meus pedidos | acompanhar a análise | vê apenas os próprios pedidos |
| US08 | cliente | modificar um pedido | corrigir dados | só se não estiver `CONTRATADO`/`CANCELADO`; volta a `EM_ANALISE` |
| US09 | cliente | cancelar um pedido | desistir do aluguel | só se não estiver `CONTRATADO` |
| US10 | agente | listar todos os pedidos com dados e rendimentos do cliente | analisar financeiramente | — |
| US11 | agente | aprovar ou reprovar um pedido com parecer | decidir sobre o aluguel | só pedidos `EM_ANALISE` |
| US12 | agente | executar o contrato de um pedido aprovado | formalizar o aluguel | só pedidos `APROVADO` → `CONTRATADO` |
| US13 | agente | cadastrar automóveis (matrícula, ano, marca, modelo, placa, propriedade) | disponibilizá-los | placa e matrícula únicas |
| US14 | banco | ser associado a pedidos como concedente de crédito | financiar o aluguel | contrato de crédito opcional por pedido |

## 2. Diagrama de Casos de Uso

```mermaid
flowchart LR
    V([Visitante]) --> UC1((Cadastrar-se))
    C([Cliente]) --> UC3((Login))
    A([Agente<br/>Empresa/Banco]) --> UC3
    C --> UC4((Consultar/modificar<br/>cadastro))
    C --> UC5((Excluir cadastro))
    C --> UC6((Introduzir pedido))
    C --> UC7((Consultar pedidos))
    C --> UC8((Modificar pedido))
    C --> UC9((Cancelar pedido))
    A --> UC10((Listar pedidos))
    A --> UC8
    A --> UC11((Avaliar pedido))
    A --> UC12((Executar contrato))
    A --> UC13((Cadastrar automóvel))
    UC6 -. include .-> UC3
    UC11 -. extend .-> UC14((Associar contrato<br/>de crédito))
    UC12 -. include .-> UC11
```

## 3. Diagrama de Classes

```mermaid
classDiagram
    class Usuario {
        +Long id
        +String login
        -String senhaHash
        +String nome
        +Papel papel
    }
    class Cliente {
        +Long id
        +String rg
        +String cpf
        +String nome
        +String endereco
        +String profissao
    }
    class Rendimento {
        +Long id
        +String entidadeEmpregadora
        +BigDecimal valor
    }
    class Automovel {
        +Long id
        +String matricula
        +Integer ano
        +String marca
        +String modelo
        +String placa
        +TipoProprietario proprietario
    }
    class Pedido {
        +Long id
        +LocalDate dataInicio
        +LocalDate dataFim
        +BigDecimal valorDiaria
        +StatusPedido status
        +String parecer
    }
    class ContratoCredito {
        +Long id
        +BigDecimal valor
        +BigDecimal taxaJurosMensal
    }
    class Papel {
        <<enumeration>>
        CLIENTE
        AGENTE_EMPRESA
        AGENTE_BANCO
    }
    class StatusPedido {
        <<enumeration>>
        EM_ANALISE
        APROVADO
        REPROVADO
        CONTRATADO
        CANCELADO
    }
    class TipoProprietario {
        <<enumeration>>
        CLIENTE
        EMPRESA
        BANCO
    }

    Usuario "1" <-- "0..1" Cliente : usuario
    Cliente "1" *-- "0..3" Rendimento
    Cliente "1" <-- "0..*" Pedido
    Automovel "1" <-- "0..*" Pedido
    Pedido "1" *-- "0..1" ContratoCredito
    ContratoCredito "0..*" --> "1" Usuario : banco (AGENTE_BANCO)
    Usuario --> Papel
    Pedido --> StatusPedido
    Automovel --> TipoProprietario
```

### Ciclo de vida do pedido

```mermaid
stateDiagram-v2
    [*] --> EM_ANALISE : cliente introduz
    EM_ANALISE --> APROVADO : agente aprova
    EM_ANALISE --> REPROVADO : agente reprova
    APROVADO --> CONTRATADO : agente executa contrato
    EM_ANALISE --> CANCELADO : cliente cancela
    APROVADO --> CANCELADO : cliente cancela
    REPROVADO --> CANCELADO : cliente cancela
    REPROVADO --> EM_ANALISE : cliente modifica
    APROVADO --> EM_ANALISE : cliente/agente modifica
```

## 4. Diagrama de Pacotes (Visão Lógica)

Arquitetura MVC no back-end; o front-end React atua como camada de visão que consome a API REST.

```mermaid
flowchart TB
    subgraph FE["frontend (React + Vite)"]
        pages[pages] --> components[components]
        pages --> apijs[api.js]
        components
    end
    subgraph BE["backend (Spring Boot - com.aluguel)"]
        controller["controller (C)"] --> service[service]
        controller --> dto[dto]
        service --> repository[repository]
        service --> model["model (M)"]
        repository --> model
        config[config<br/>segurança/CORS] -.-> controller
        exception[exception] -.-> controller
    end
    DB[(Supabase<br/>PostgreSQL)]
    apijs -- "HTTP/JSON + Basic Auth" --> controller
    repository -- JPA/JDBC --> DB
```

Subsistemas citados no enunciado: **gestão de pedidos e contratos** (`controller/service/repository/model` do back-end) e **construção dinâmica das páginas web** (front-end React, que renderiza as telas a partir dos dados da API).

## 5. Histórico de versões

| Versão | Mudança |
|--------|---------|
| 1.0 | Modelos iniciais; código implementado com `Pedido` possuindo `ContratoCredito` opcional e `Usuario` unificando clientes e agentes (login), com `Cliente` como perfil do usuário com papel `CLIENTE`. |
