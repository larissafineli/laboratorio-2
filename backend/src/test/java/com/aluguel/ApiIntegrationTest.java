package com.aluguel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Testa a API HTTP de ponta a ponta: autenticacao, autorizacao e fluxo do pedido. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {

    @Value("${local.server.port}")
    int porta;

    static final HttpClient http = HttpClient.newHttpClient();
    static String cliente, agente;
    static boolean preparado = false;

    record Resp(int status, String corpo) {}

    Resp chamar(String metodo, String caminho, String usuario, String json) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + "/api" + caminho))
                .header("Content-Type", "application/json")
                .method(metodo, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        if (usuario != null) {
            b.header("Authorization", "Basic " + Base64.getEncoder().encodeToString((usuario + ":123456").getBytes()));
        }
        HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Resp(r.statusCode(), r.body());
    }

    void preparar() throws Exception {
        if (preparado) return;
        String s = UUID.randomUUID().toString().substring(0, 8);
        cliente = "cli" + s;
        agente = "ag" + s;
        String cpf = String.valueOf(Math.abs(s.hashCode()) % 90000000000L + 10000000000L);
        assertEquals(201, chamar("POST", "/auth/register", null, """
                {"login":"%s","senha":"123456","papel":"CLIENTE","nome":"Cli",
                 "cliente":{"rg":"1","cpf":"%s","nome":"Cli","endereco":"Rua","profissao":"Dev",
                 "rendimentos":[{"entidadeEmpregadora":"X","valor":1000}]}}""".formatted(cliente, cpf)).status());
        assertEquals(201, chamar("POST", "/auth/register", null, """
                {"login":"%s","senha":"123456","papel":"AGENTE_EMPRESA","nome":"Emp"}""".formatted(agente)).status());
        preparado = true;
    }

    @Test
    void semLoginRetorna401() throws Exception {
        assertEquals(401, chamar("GET", "/pedidos", null, null).status());
    }

    @Test
    void senhaErradaRetorna401() throws Exception {
        preparar();
        HttpRequest r = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + "/api/auth/login"))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((cliente + ":errada").getBytes()))
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        assertEquals(401, http.send(r, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void loginValidoRetornaPerfil() throws Exception {
        preparar();
        Resp r = chamar("POST", "/auth/login", cliente, null);
        assertEquals(200, r.status());
        assertTrue(r.corpo().contains("\"papel\":\"CLIENTE\""));
        assertFalse(r.corpo().contains("senha"), "nao pode expor a senha");
    }

    @Test
    void clienteNaoListaClientesNemCadastraAutomovel() throws Exception {
        preparar();
        assertEquals(403, chamar("GET", "/clientes", cliente, null).status());
        assertEquals(403, chamar("POST", "/automoveis", cliente, "{}").status());
    }

    @Test
    void fluxoCompletoPeloHttp() throws Exception {
        preparar();
        String s = UUID.randomUUID().toString().substring(0, 6);
        Resp carro = chamar("POST", "/automoveis", agente, """
                {"matricula":"M%s","ano":2024,"marca":"Fiat","modelo":"Argo","placa":"P%s","proprietario":"EMPRESA"}""".formatted(s, s));
        assertEquals(201, carro.status());
        String id = carro.corpo().replaceAll(".*\"id\":(\\d+).*", "$1");

        String hoje = LocalDate.now().toString();
        String corpo = """
                {"automovelId":%s,"dataInicio":"%s","dataFim":"%s","valorDiaria":120}""".formatted(id, hoje, LocalDate.now().plusDays(2));
        Resp pedido = chamar("POST", "/pedidos", cliente, corpo);
        assertEquals(201, pedido.status());
        assertTrue(pedido.corpo().contains("\"status\":\"EM_ANALISE\""));
        String pid = pedido.corpo().replaceAll(".*\"id\":(\\d+),\"parecer.*", "$1");

        assertEquals(403, chamar("POST", "/pedidos/" + pid + "/avaliar", cliente, "{\"aprovado\":true}").status());
        assertTrue(chamar("POST", "/pedidos/" + pid + "/avaliar", agente, "{\"aprovado\":true,\"parecer\":\"ok\"}")
                .corpo().contains("\"status\":\"APROVADO\""));
        assertTrue(chamar("POST", "/pedidos/" + pid + "/contratar", agente, null)
                .corpo().contains("\"status\":\"CONTRATADO\""));
        assertTrue(chamar("GET", "/pedidos", cliente, null).corpo().contains("\"status\":\"CONTRATADO\""));
    }

    @Test
    void dataDeInicioNoPassadoEhRecusada() throws Exception {
        preparar();
        String corpo = """
                {"automovelId":1,"dataInicio":"%s","dataFim":"%s","valorDiaria":120}""".formatted(
                LocalDate.now().minusDays(3), LocalDate.now());
        int st = chamar("POST", "/pedidos", cliente, corpo).status();
        assertTrue(st == 400 || st == 404, "esperado 400 (ou 404 se o carro 1 nao existir), veio " + st);
    }
}
