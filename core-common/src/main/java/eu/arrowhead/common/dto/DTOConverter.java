package eu.arrowhead.common.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;

public class DTOConverter {

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static SystemResponseDTO convertSystemToSystemResponseDTO(final System system) {
		Assert.notNull(system, "System is null");
		
		return new SystemResponseDTO(system.getId(),
				system.getSystemName(),
				system.getAddress(),
				system.getPort(),
				system.getAuthenticationInfo(),
				String.valueOf(system.getCreatedAt()),
				String.valueOf(system.getUpdatedAt()));		
	}
	
	//-------------------------------------------------------------------------------------------------
	public static SystemListResponseDTO convertSystemEntryListToSystemListResponseDTO(final Page<System> systemEntryList) {
		Assert.notNull(systemEntryList, "systemEntryList is null");
		
		final long count = systemEntryList.getTotalElements();		
		
		final SystemListResponseDTO systemListResponseDTO = new SystemListResponseDTO();
		
		systemListResponseDTO.setCount(count);
		systemListResponseDTO.setData(sytemEntryListToSystemResponeDTOList(systemEntryList.getContent()));
		
		return systemListResponseDTO;
		
	}
	
	//-------------------------------------------------------------------------------------------------
	public static System convertSystemRequestDTOToSystem(final SystemRequestDTO systemRequestDTO) {
		Assert.notNull(systemRequestDTO, "System is null");
		Assert.notNull(systemRequestDTO.getAddress(), "SystemAddress is null");
		Assert.notNull(systemRequestDTO.getPort(), "SystemPort is null");
		Assert.notNull(systemRequestDTO.getSystemName(), "SystemName is null");
		
		final String validatedAuthenticationInfo = systemRequestDTO.getAuthenticationInfo();
		if ( validatedAuthenticationInfo != null && !"".equalsIgnoreCase(validatedAuthenticationInfo)) {
			
			return new System(systemRequestDTO.getSystemName(), systemRequestDTO.getAddress(), systemRequestDTO.getPort(), validatedAuthenticationInfo);
		}
		else {
			final System system = new System();
			system.setAddress(systemRequestDTO.getAddress());
			system.setPort(systemRequestDTO.getPort());
			system.setSystemName(systemRequestDTO.getSystemName());
			
			return system;
		}
						
	}
		
	
	public static ServiceDefinitionResponseDTO convertServiceDefinitionToServiceDefinitionResponseDTO (final ServiceDefinition serviceDefinition) {
		Assert.notNull(serviceDefinition, "ServiceDefinition is null");
		Assert.notNull(serviceDefinition.getServiceDefinition(), "ServiceDefinition is null");
		
		return new ServiceDefinitionResponseDTO(serviceDefinition.getId(), serviceDefinition.getServiceDefinition(), String.valueOf(serviceDefinition.getCreatedAt()), String.valueOf(serviceDefinition.getUpdatedAt()));
	}
	
	public static ServiceDefinition convertServiceDefinitionRequestDTOToServiceDefinition(final ServiceDefinitionRequestDTO serviceDefinitionRequestDTO) {
		Assert.notNull(serviceDefinitionRequestDTO, "ServiceDefinitionRequestDTO is null");
		Assert.notNull(serviceDefinitionRequestDTO.getServiceDefinition(), "ServiceDefinition is null");
		
		return new ServiceDefinition(serviceDefinitionRequestDTO.getServiceDefinition());
	}
	
	public static ServiceDefinitionsListResponseDTO convertServiceDefinitionsListToServiceDefinitionListResponseDTO(final List<ServiceDefinition> serviceDefinitions) {
		Assert.notNull(serviceDefinitions, "List of ServiceDefinition is null");
		
		final List<ServiceDefinitionResponseDTO> serviceDefinitionDTOs = new ArrayList<>();
		for (final ServiceDefinition definition : serviceDefinitions) {
			serviceDefinitionDTOs.add(convertServiceDefinitionToServiceDefinitionResponseDTO(definition));
		}		
		return new ServiceDefinitionsListResponseDTO( serviceDefinitionDTOs, serviceDefinitionDTOs.size());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	
	private DTOConverter() {
		throw new UnsupportedOperationException();
	}
	
	//-------------------------------------------------------------------------------------------------
	
	private static List<SystemResponseDTO> sytemEntryListToSystemResponeDTOList(final List<System> systemList) {
		
		final List<SystemResponseDTO> systemResponseDTOs = new ArrayList<>();
		
		for (final System system : systemList) {
			systemResponseDTOs.add(convertSystemToSystemResponseDTO(system));
		}
		
		return systemResponseDTOs;
		
	}
	
}
