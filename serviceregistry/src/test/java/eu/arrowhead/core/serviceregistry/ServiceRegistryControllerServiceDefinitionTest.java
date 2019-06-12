package eu.arrowhead.core.serviceregistry;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.ServiceDefinitionsListResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@RunWith (SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration (classes = { ServiceRegistryDBSerrviceTestContext.class })
public class ServiceRegistryControllerServiceDefinitionTest {
	
	//=================================================================================================
	// members
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockServiceRegistryDBService") 
	private ServiceRegistryDBService serviceRegistryDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of getServiceDefinitions
	
	@Test
	public void getServiceDefinitionsTestWithoutParameter() throws Exception  {
		final int numOfEntries = 5;
		final Page<ServiceDefinition> serviceDefinitionEntries = createServiceDefinitionPageForDBMocking(numOfEntries);
		final ServiceDefinitionsListResponseDTO serviceDefinitionEntriesDTO = DTOConverter.convertServiceDefinitionsListToServiceDefinitionListResponseDTO(serviceDefinitionEntries);
		when(serviceRegistryDBService.getServiceDefinitionEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceDefinitionEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final ServiceDefinitionsListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceDefinitionsListResponseDTO.class);
		assertEquals(numOfEntries, responseBody.getCount());
	}
	
	@Test
	public void getServiceDefinitionsTestWithPageAndSizeParameter() throws Exception {
		final int numOfEntries = 8;
		final Page<ServiceDefinition> serviceDefinitionEntries = createServiceDefinitionPageForDBMocking(numOfEntries);
		final ServiceDefinitionsListResponseDTO serviceDefinitionEntriesDTO = DTOConverter.convertServiceDefinitionsListToServiceDefinitionListResponseDTO(serviceDefinitionEntries);
		when(serviceRegistryDBService.getServiceDefinitionEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceDefinitionEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.param("page", "0")
				.param("item_per_page", String.valueOf(numOfEntries))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final ServiceDefinitionsListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceDefinitionsListResponseDTO.class);
		assertEquals(numOfEntries, responseBody.getCount());
	}
	
	@Test
	public void getServiceDefinitionsTestWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.param("item_per_page", "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test 
	public void getServiceDefinitionsTestWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.param("page", "0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void getServiceDefinitionsTestWithInvalidSortDirectionFlagParametert() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.param("direction", "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of getServiceDefinitionsById
	
	@Test
	public void getServiceDefinitionByIdTestWithExistingId() throws Exception {
		final int requestedId = 1;
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(requestedId, "", "", "");		
		when(serviceRegistryDBService.getServiceDefinitionByIdResponse(anyLong())).thenReturn(serviceDefinitionResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/services/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final ServiceDefinitionResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceDefinitionResponseDTO.class);
		assertEquals(requestedId, responseBody.getId());
	}
	
	@Test
	public void getServiceDefinitionByIdTestWithInvalidId() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/services/-1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of addServiceDefinition
	
	@Test
	public void addServiceDefinitionTestWithValidDefinition() throws Exception {
		final String serviceDefinition = "testDefinition";
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(0, serviceDefinition,"","");
		when(serviceRegistryDBService.createServiceDefinitionResponse(anyString())).thenReturn(serviceDefinitionResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(post("/serviceregistry/mgmt/services")
				.content("{\"serviceDefinition\": \"" + serviceDefinition + "\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn();
		
		final ServiceDefinitionResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceDefinitionResponseDTO.class);
		assertEquals(serviceDefinition, responseBody.getServiceDefinition());
	}
	
	@Test
	public void addServiceDefinitionTestWithNullDefinition() throws Exception {
		this.mockMvc.perform(post("/serviceregistry/mgmt/services")
				.content("{\"serviceDefinition\": \"\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void addServiceDefinitionTestWithBlankDefinition() throws Exception {
		this.mockMvc.perform(post("/serviceregistry/mgmt/services")
				.content("{\"serviceDefinition\": \"      \"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of putUpdateServiceDefinition
	
	@Test
	public void putUpdateServiceDefinitionTestWithValidDefintion() throws Exception {
		final String serviceDefinition = "testDefinition";
		final int id = 5;
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(id, serviceDefinition,"","");
		when(serviceRegistryDBService.updateServiceDefinitionByIdResponse(anyLong(), anyString())).thenReturn(serviceDefinitionResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(put("/serviceregistry/mgmt/services/" + id)
				.content("{\"serviceDefinition\": \"" + serviceDefinition + "\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final ServiceDefinitionResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceDefinitionResponseDTO.class);
		assertEquals(serviceDefinition, responseBody.getServiceDefinition());
	}
	
	@Test
	public void putUpdateServiceDefinitionTestWithNullDefintion() throws Exception {
		final String serviceDefinition = null;
		this.mockMvc.perform(put("/serviceregistry/mgmt/services/5")
				.content("{\"serviceDefinition\": "+ serviceDefinition + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void putUpdateServiceDefinitionTestWithBlankDefintion() throws Exception {
		final String serviceDefinition = "      ";
		this.mockMvc.perform(put("/serviceregistry/mgmt/services/5")
				.content("{\"serviceDefinition\": "+ serviceDefinition + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of patchUpdateServiceDefinition
	
	@Test
	public void patchUpdateServiceDefinitionTestWithValidDefintion() throws Exception {
		final String serviceDefinition = "testDefinition";
		final int id = 5;
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(id, serviceDefinition,"","");
		when(serviceRegistryDBService.updateServiceDefinitionByIdResponse(anyLong(), anyString())).thenReturn(serviceDefinitionResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(patch("/serviceregistry/mgmt/services/" + id)
				.content("{\"serviceDefinition\": \"" + serviceDefinition + "\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final ServiceDefinitionResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceDefinitionResponseDTO.class);
		assertEquals(serviceDefinition, responseBody.getServiceDefinition());
	}
	
	@Test
	public void patchUpdateServiceDefinitionTestWithNullDefintion() throws Exception {
		final String serviceDefinition = null;
		this.mockMvc.perform(patch("/serviceregistry/mgmt/services/5")
				.content("{\"serviceDefinition\": "+ serviceDefinition + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void patchUpdateServiceDefinitionTestWithBlankDefintion() throws Exception {
		final String serviceDefinition = "      ";
		this.mockMvc.perform(patch("/serviceregistry/mgmt/services/5")
				.content("{\"serviceDefinition\": "+ serviceDefinition + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of removeServiceDefinition
	
	@Test
	public void removeServiceDefinitionTestWithValidId( ) throws Exception {
		this.mockMvc.perform(delete("/serviceregistry/mgmt/services/4")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	public void removeServiceDefinitionTestWithInvalidId( ) throws Exception {
		this.mockMvc.perform(delete("/serviceregistry/mgmt/services/0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// assistant methods
	
	private Page<ServiceDefinition> createServiceDefinitionPageForDBMocking(final int amountOfEntry) {
		final List<ServiceDefinition> serviceDefinitionList = new ArrayList<>();
		for (int i = 0; i < amountOfEntry; i++) {
			final ServiceDefinition serviceDefinition = new ServiceDefinition("mockedService" + i);
			serviceDefinition.setId(i);
			final ZonedDateTime timeStamp = ZonedDateTime.now();
			serviceDefinition.setCreatedAt(timeStamp);
			serviceDefinition.setUpdatedAt(timeStamp);
			serviceDefinitionList.add(serviceDefinition);
		}
		final Page<ServiceDefinition> entries = new PageImpl<ServiceDefinition>(serviceDefinitionList);
		return entries;
	}
}
