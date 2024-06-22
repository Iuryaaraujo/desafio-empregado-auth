package com.devsuperior.demo.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.demo.dto.EmployeeDTO;
import com.devsuperior.demo.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EmployeeControllerIT {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private TokenUtil tokenUtil;
	
	private String operatorUsername;
	private String operatorPassword;
	private String adminUsername;
	private String adminPassword;
	
	@BeforeEach
	void setUp() throws Exception {
		
		operatorUsername = "ana@gmail.com";
		operatorPassword = "123456";
		adminUsername = "bob@gmail.com";
		adminPassword = "123456";
	}

	// insert deve retornar 403 Forbidden/Acesso negado quando o operador estiver logado
	@Test
	public void insertShouldReturn403WhenOperatorLogged() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, operatorUsername, operatorPassword);

		EmployeeDTO dto = new EmployeeDTO(null, "Joaquim", "joaquim@gmail.com", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isForbidden());
	}	

	// insert deve retornar 401 quando usuário não tiver logado
	@Test
	public void insertShouldReturn401WhenNoUserLogged() throws Exception {

		EmployeeDTO dto = new EmployeeDTO(null, "Joaquim", "joaquim@gmail.com", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnauthorized());
	}	

	// insert deve inserir recurso quando o administrador estiver logado e corrigir dados
	@Test
	public void insertShouldInsertResourceWhenAdminLoggedAndCorrectData() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

		EmployeeDTO dto = new EmployeeDTO(null, "Joaquim", "joaquim@gmail.com", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").value("Joaquim"));
		result.andExpect(jsonPath("$.email").value("joaquim@gmail.com"));
		result.andExpect(jsonPath("$.departmentId").value(1L));
	}

	// insert deve retornar 422 Unprocessable entity quando o administrador estiver logado e nome em branco
	// teste de validação
	@Test
	public void insertShouldReturn422WhenAdminLoggedAndBlankName() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

		EmployeeDTO dto = new EmployeeDTO(null, "   ", "joaquim@gmail.com", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableEntity());
		result.andExpect(jsonPath("$.errors[0].fieldName").value("name"));
		result.andExpect(jsonPath("$.errors[0].message").value("Campo requerido"));
	}

	// insert deve retornar 422 Unprocessable entity quando o administrador estiver logado e e-mail inválido
	// teste de validação
	@Test
	public void insertShouldReturn422WhenAdminLoggedAndInvalidEmail() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

		EmployeeDTO dto = new EmployeeDTO(null, "Joaquim", "joaquim@", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableEntity());
		result.andExpect(jsonPath("$.errors[0].fieldName").value("email"));
		result.andExpect(jsonPath("$.errors[0].message").value("Email inválido"));
	}

	// insert deve retornar 422 Unprocessable entity quando o administrador estiver logado e departamento nulo
	// teste de validação
	@Test
	public void insertShouldReturn422WhenAdminLoggedAndNullDepartment() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

		EmployeeDTO dto = new EmployeeDTO(null, "Joaquim", "joaquim@gmail.com", null);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableEntity());
		result.andExpect(jsonPath("$.errors[0].fieldName").value("departmentId"));
		result.andExpect(jsonPath("$.errors[0].message").value("Campo requerido"));
	}
}
