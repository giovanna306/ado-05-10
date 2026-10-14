package br.senac.sp.tads.dsw.exemplo5.controller;

import br.senac.sp.tads.dsw.exemplo5.model.Departamento;
import br.senac.sp.tads.dsw.exemplo5.model.Funcionario;
import br.senac.sp.tads.dsw.exemplo5.repository.DepartamentoRepository;
import br.senac.sp.tads.dsw.exemplo5.repository.FuncionarioRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FuncionarioControllerTest {
   @Autowired
   private MockMvc mockMvc;
   @Autowired
   private FuncionarioRepository funcionarioRepository;
   @Autowired
   private DepartamentoRepository departamentoRepository;
   @Autowired
   private ObjectMapper objectMapper;
   private Departamento departamentoPadrao;

   public FuncionarioControllerTest() {
   }

   @BeforeEach
   void setUp() {
      Departamento departamento = new Departamento();
      departamento.setNome("Desenvolvimento");
      departamento.setOrcamento((double)200000.0F);
      this.departamentoPadrao = (Departamento)this.departamentoRepository.save(departamento);
   }

   private Funcionario salvarFuncionario(String nome) {
      Funcionario funcionario = new Funcionario();
      funcionario.setNome(nome);
      funcionario.setDataContratacao(LocalDate.of(2024, 3, 15));
      funcionario.setTrabalhoRemoto(true);
      funcionario.setDepartamento(this.departamentoPadrao);
      return (Funcionario)this.funcionarioRepository.save(funcionario);
   }

   @Test
   void deveCriarFuncionarioComSucesso() throws Exception {
      Funcionario funcionario = new Funcionario();
      funcionario.setNome("Maria Souza");
      funcionario.setDataContratacao(LocalDate.of(2024, 3, 15));
      funcionario.setTrabalhoRemoto(true);
      funcionario.setDepartamento(this.departamentoPadrao);
      String json = this.objectMapper.writeValueAsString(funcionario);
      this.mockMvc.perform(((MockHttpServletRequestBuilder)MockMvcRequestBuilders.post("/api/funcionarios", new Object[0]).contentType(MediaType.APPLICATION_JSON)).content(json)).andExpect(MockMvcResultMatchers.status().isCreated()).andExpect(MockMvcResultMatchers.jsonPath("$.id", new Object[0]).exists()).andExpect(MockMvcResultMatchers.jsonPath("$.nome", new Object[0]).value("Maria Souza")).andExpect(MockMvcResultMatchers.jsonPath("$.departamento.nome", new Object[0]).value("Desenvolvimento"));
   }

   @Test
   void deveRetornarErro400AoCriarFuncionarioComDataFutura() throws Exception {
      Funcionario funcionario = new Funcionario();
      funcionario.setNome("João Futuro");
      funcionario.setDataContratacao(LocalDate.now().plusDays(1L));
      funcionario.setTrabalhoRemoto(false);
      funcionario.setDepartamento(this.departamentoPadrao);
      String json = this.objectMapper.writeValueAsString(funcionario);
      this.mockMvc.perform(((MockHttpServletRequestBuilder)MockMvcRequestBuilders.post("/api/funcionarios", new Object[0]).contentType(MediaType.APPLICATION_JSON)).content(json)).andExpect(MockMvcResultMatchers.status().isBadRequest());
   }

   @Test
   void deveListarTodosOsFuncionarios() throws Exception {
      this.salvarFuncionario("Ana Lima");
      this.mockMvc.perform(MockMvcRequestBuilders.get("/api/funcionarios", new Object[0])).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].nome", new Object[0]).value("Ana Lima"));
   }

   @Test
   void deveBuscarFuncionarioPorId() throws Exception {
      Funcionario salvo = this.salvarFuncionario("Carlos Dias");
      this.mockMvc.perform(MockMvcRequestBuilders.get("/api/funcionarios/" + String.valueOf(salvo.getId()), new Object[0])).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.nome", new Object[0]).value("Carlos Dias")).andExpect(MockMvcResultMatchers.jsonPath("$.departamento.id", new Object[0]).value(this.departamentoPadrao.getId()));
   }

   @Test
   void deveAtualizarFuncionario() throws Exception {
      Funcionario salvo = this.salvarFuncionario("Paula Reis");
      salvo.setTrabalhoRemoto(false);
      String json = this.objectMapper.writeValueAsString(salvo);
      this.mockMvc.perform(((MockHttpServletRequestBuilder)MockMvcRequestBuilders.put("/api/funcionarios/" + String.valueOf(salvo.getId()), new Object[0]).contentType(MediaType.APPLICATION_JSON)).content(json)).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.trabalhoRemoto", new Object[0]).value(false));
   }

   @Test
   void deveApagarFuncionario() throws Exception {
      Funcionario salvo = this.salvarFuncionario("Rafael Costa");
      this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/funcionarios/" + String.valueOf(salvo.getId()), new Object[0])).andExpect(MockMvcResultMatchers.status().isNoContent());
      this.mockMvc.perform(MockMvcRequestBuilders.get("/api/funcionarios/" + String.valueOf(salvo.getId()), new Object[0])).andExpect(MockMvcResultMatchers.status().isNotFound());
   }
}
