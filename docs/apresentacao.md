# Roteiro da Apresentação Final

## 1. Modelo inicial (v1) × modelo final (v2)

| Aspecto | v1 (inicial) | v2 (implementado) | Motivo |
|---|---|---|---|
| Usuários | `Cliente` e `Agente` (Empresa/Banco) como hierarquia de `Usuario` | Uma única entidade `Usuario` com `Papel`; `Cliente` é o perfil do papel CLIENTE | Simplifica login/Spring Security; evita herança JPA para apenas diferenças de papel |
| Agentes | Classes `Empresa` e `Banco` | Valores do enum `Papel` | Agentes só têm login e nome; sem atributos próprios |
| Contrato | `Pedido` → `Contrato` → `ContratoCredito` | `Pedido` com `status` (`CONTRATADO`) e `ContratoCredito` opcional | O contrato de aluguel é a execução do pedido aprovado, sem dados adicionais |
| Pedido | só `status` | datas, diária, parecer financeiro, ciclo de vida completo | Necessário para a análise e para o cliente acompanhar |
| Pacotes | controller/model/repository/view | + service, dto, config, exception | Regras de negócio fora do controller |
| Rendimentos | até 3 por cliente | idem, validado no DTO e no serviço | Regra do enunciado |

## 2. Demonstração (≈ 5 min)
1. Subir com `APP_SEED=true` (usuários `empresa`, `banco`, `cliente`, senha `123456`).
2. Tentar acessar sem login → 401.
3. Cliente: criar pedido com crédito do banco → status **EM ANÁLISE**.
4. Cliente: modificar e cancelar um pedido.
5. Agente: aprovar com parecer → executar contrato → cliente vê **CONTRATADO**.
6. CRUD do cliente (Meu cadastro) e limite de 3 rendimentos.
7. Agente: gerir automóveis.

## 3. Alinhamento modelo × código
- Cada classe do diagrama v2 corresponde a uma classe em `backend/.../model`.
- Cada pacote do diagrama corresponde a um pacote Java / pasta do front-end.
- Testes automatizados cobrem o ciclo de vida do pedido e as regras de negócio.
