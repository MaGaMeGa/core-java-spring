package eu.arrowhead.core.qos.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.icmp4j.IcmpPingResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurementLog;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurementLogDetails;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementPingRepository;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogDetailsRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;

@RunWith(SpringRunner.class)
public class QoSDBServiceTest {

	//=================================================================================================
	// members
	@InjectMocks
	private QoSDBService qoSDBService;

	@Mock
	private QoSIntraMeasurementRepository qoSIntraMeasurementRepository;

	@Mock
	private QoSIntraMeasurementPingRepository qoSIntraMeasurementPingRepository;

	@Mock
	private QoSIntraPingMeasurementLogRepository qoSIntraPingMeasurementLogRepository;

	@Mock
	private QoSIntraPingMeasurementLogDetailsRepository qoSIntraPingMeasurementLogDetailsRepository;

	@Mock
	private SystemRepository systemRepository;

	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE = " sortable field  is not available.";
	private static final String NOT_IN_DB_ERROR_MESSAGE = " is not available in database";
	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String NULL_ERROR_MESSAGE = " is null";

	//=================================================================================================
	// methods

	//=================================================================================================
	// Tests of updateCountStartedAt

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testUpdateCountStartedAt() {

		final ZonedDateTime testStartedAt = ZonedDateTime.now().minusMinutes(1);
		final List<QoSIntraPingMeasurement> measurementList = getQosIntraPingMeasurementListForTest();
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);

		when(qoSIntraMeasurementPingRepository.findAll()).thenReturn(measurementList);
		when(qoSIntraMeasurementPingRepository.saveAll(valueCapture.capture())).thenReturn(List.of());
		doNothing().when(qoSIntraMeasurementPingRepository).flush();

		qoSDBService.updateCountStartedAt();

		verify(qoSIntraMeasurementPingRepository, times(1)).findAll();
		verify(qoSIntraMeasurementPingRepository, times(1)).saveAll(any());
		verify(qoSIntraMeasurementPingRepository, times(1)).flush();

		final List<QoSIntraPingMeasurement> captured = valueCapture.getValue();
		assertEquals(measurementList.size(), captured.size());
		for (final QoSIntraPingMeasurement qoSIntraPingMeasurement : captured) {

			assertEquals(0, qoSIntraPingMeasurement.getSent());
			assertEquals(0, qoSIntraPingMeasurement.getReceived());
			assertTrue(qoSIntraPingMeasurement.getCountStartedAt().isAfter(testStartedAt));
		}
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void testUpdateCountStartedAtFlushThrowDatabaseException() {

		final ZonedDateTime testStartedAt = ZonedDateTime.now().minusMinutes(1);;
		final List<QoSIntraPingMeasurement> measurementList = getQosIntraPingMeasurementListForTest();
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);

		when(qoSIntraMeasurementPingRepository.findAll()).thenReturn(measurementList);
		when(qoSIntraMeasurementPingRepository.saveAll(valueCapture.capture())).thenReturn(List.of());
		doThrow(HibernateException.class).when(qoSIntraMeasurementPingRepository).flush();

		try {

			qoSDBService.updateCountStartedAt();

		} catch (final Exception ex) {

			assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());

			verify(qoSIntraMeasurementPingRepository, times(1)).findAll();
			verify(qoSIntraMeasurementPingRepository, times(1)).saveAll(any());
			verify(qoSIntraMeasurementPingRepository, times(1)).flush();

			final List<QoSIntraPingMeasurement> captured = valueCapture.getValue();
			assertEquals(measurementList.size(), captured.size());

			for (final QoSIntraPingMeasurement qoSIntraPingMeasurement : captured) {

				assertEquals(0, qoSIntraPingMeasurement.getSent());
				assertEquals(0, qoSIntraPingMeasurement.getReceived());
				assertTrue(qoSIntraPingMeasurement.getCountStartedAt().isAfter(testStartedAt));
			}

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void testUpdateCountStartedAtSaveAllThrowDatabaseException() {

		final ZonedDateTime testStartedAt = ZonedDateTime.now().minusMinutes(1);
		final List<QoSIntraPingMeasurement> measurementList = getQosIntraPingMeasurementListForTest();
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);

		when(qoSIntraMeasurementPingRepository.findAll()).thenReturn(measurementList);
		when(qoSIntraMeasurementPingRepository.saveAll(valueCapture.capture())).thenThrow(HibernateException.class);
		doNothing().when(qoSIntraMeasurementPingRepository).flush();

		try {

			qoSDBService.updateCountStartedAt();

		} catch (final Exception ex) {

			assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());

			verify(qoSIntraMeasurementPingRepository, times(1)).findAll();
			verify(qoSIntraMeasurementPingRepository, times(1)).saveAll(any());
			verify(qoSIntraMeasurementPingRepository, times(0)).flush();

			final List<QoSIntraPingMeasurement> captured = valueCapture.getValue();
			assertEquals(measurementList.size(), captured.size());

			for (final QoSIntraPingMeasurement qoSIntraPingMeasurement : captured) {

				assertEquals(0, qoSIntraPingMeasurement.getSent());
				assertEquals(0, qoSIntraPingMeasurement.getReceived());
				assertTrue(qoSIntraPingMeasurement.getCountStartedAt().isAfter(testStartedAt));
			}

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void testUpdateCountStartedAtFindAllThrowDatabaseException() {

		final List<QoSIntraPingMeasurement> measurementList = getQosIntraPingMeasurementListForTest();

		when(qoSIntraMeasurementPingRepository.findAll()).thenThrow(HibernateException.class);
		when(qoSIntraMeasurementPingRepository.saveAll(any())).thenReturn(measurementList);
		doNothing().when(qoSIntraMeasurementPingRepository).flush();

		try {

			qoSDBService.updateCountStartedAt();

		} catch (final Exception ex) {

			assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());

			verify(qoSIntraMeasurementPingRepository, times(1)).findAll();
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAll(any());
			verify(qoSIntraMeasurementPingRepository, times(0)).flush();

			throw ex;
		}

	}

	//=================================================================================================
	// Tests of createMeasurement

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateMeasurement() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final ArgumentCaptor<QoSIntraMeasurement> valueCapture = ArgumentCaptor.forClass(QoSIntraMeasurement.class);

		when(qoSIntraMeasurementRepository.saveAndFlush(valueCapture.capture())).thenReturn(measurement);

		qoSDBService.createMeasurement(system, QoSMeasurementType.PING, aroundNow);

		verify(qoSIntraMeasurementRepository, times(1)).saveAndFlush(any());

		final QoSIntraMeasurement captured = valueCapture.getValue();
		assertEquals(system.getId(), captured.getSystem().getId());
		assertEquals(QoSMeasurementType.PING, captured.getMeasurementType());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreateMeasurementSaveAndFlushThrowException() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final System system = getSystemForTest();

		final ArgumentCaptor<QoSIntraMeasurement> valueCapture = ArgumentCaptor.forClass(QoSIntraMeasurement.class);

		when(qoSIntraMeasurementRepository.saveAndFlush(valueCapture.capture())).thenThrow(HibernateException.class);

		qoSDBService.createMeasurement(system, QoSMeasurementType.PING, aroundNow);

		verify(qoSIntraMeasurementRepository, times(1)).saveAndFlush(any());

		final QoSIntraMeasurement captured = valueCapture.getValue();
		assertEquals(system.getId(), captured.getSystem().getId());
		assertEquals(QoSMeasurementType.PING, captured.getMeasurementType());

	}

	//=================================================================================================
	// Tests of getMeasurement

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurement() {

		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		qoSDBService.getMeasurement(systemResponseDTO);

		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
		verify(qoSIntraMeasurementRepository, times(1)).findBySystemAndMeasurementType(any(), any());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetMeasurementRequestedSystemNotInDB() {

		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.ofNullable(null));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getMeasurement(systemResponseDTO);

		} catch (final Exception ex) {

			assertEquals("Requested system" + NOT_IN_DB_ERROR_MESSAGE, ex.getMessage());

			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurementMeasurementNotInDB() {

		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		final System system = getSystemForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.ofNullable(null));

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();
		when(qoSIntraMeasurementRepository.saveAndFlush(any())).thenReturn(measurement);

		qoSDBService.getMeasurement(systemResponseDTO);

		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
		verify(qoSIntraMeasurementRepository, times(1)).findBySystemAndMeasurementType(any(), any());
		verify(qoSIntraMeasurementRepository, times(1)).saveAndFlush(any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMeasurementNullRequestParameter() {

		final System system = getSystemForTest();
		final SystemResponseDTO systemResponseDTO = null;
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getMeasurement(systemResponseDTO);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("SystemRequestDTO" + NULL_ERROR_MESSAGE));

			verify(systemRepository, times(0)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMeasurementSystemNameIsNullSystemResponseDTOParameter() {

		final System system = getSystemForTest();
		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		systemResponseDTO.setSystemName(null);
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getMeasurement(systemResponseDTO);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("System name" + EMPTY_OR_NULL_ERROR_MESSAGE));

			verify(systemRepository, times(0)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMeasurementSystemNameIsEmptySystemResponseDTOParameter() {

		final System system = getSystemForTest();
		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		systemResponseDTO.setSystemName("   ");
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getMeasurement(systemResponseDTO);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("System name" + EMPTY_OR_NULL_ERROR_MESSAGE));

			verify(systemRepository, times(0)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMeasurementSystemAddressIsNullSystemResponseDTOParameter() {

		final System system = getSystemForTest();
		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		systemResponseDTO.setAddress(null);
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getMeasurement(systemResponseDTO);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("System address" + EMPTY_OR_NULL_ERROR_MESSAGE));

			verify(systemRepository, times(0)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMeasurementSystemAddressIsEmptySystemResponseDTOParameter() {

		final System system = getSystemForTest();
		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		systemResponseDTO.setAddress("   ");
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getMeasurement(systemResponseDTO);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("System address" + EMPTY_OR_NULL_ERROR_MESSAGE));

			verify(systemRepository, times(0)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//=================================================================================================
	// Tests of createMeasurement

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreatePingMeasurement() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final ZonedDateTime aroundNow = ZonedDateTime.now();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		qoSDBService.createPingMeasurement(measurementParam, calculations, aroundNow);

		verify(qoSIntraMeasurementPingRepository, times(1)).saveAndFlush(any());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreatePingMeasurementWithNullMeasurementParameter() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = null;
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final ZonedDateTime aroundNow = ZonedDateTime.now();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		try {

			qoSDBService.createPingMeasurement(measurementParam, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("QoSIntraMeasurement" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAndFlush(any());
			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreatePingMeasurementWithNullCalculationsParameter() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = null;//getCalculationsForTest();
		final ZonedDateTime aroundNow = ZonedDateTime.now();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		try {

			qoSDBService.createPingMeasurement(measurementParam, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("PingMeasurementCalculationsDTO" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAndFlush(any());
			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreatePingMeasurementWithNullAroundNowParameter() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final ZonedDateTime aroundNow = null;//ZonedDateTime.now();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		try {

			qoSDBService.createPingMeasurement(measurementParam, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("ZonedDateTime" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAndFlush(any());
			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreatePingMeasurementSaveAndFlushThrowsDatabseException() {

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final ZonedDateTime aroundNow = ZonedDateTime.now();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenThrow(HibernateException.class);

		try {

			qoSDBService.createPingMeasurement(measurementParam, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraMeasurementPingRepository, times(1)).saveAndFlush(any());
			throw ex;
		}

	}

	//=================================================================================================
	// Tests of getPingMeasurementByMeasurement

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPingMeasurementByMeasurement() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenReturn(Optional.of(pingMeasurement));

		qoSDBService.getPingMeasurementByMeasurement(measurementParam);

		verify(qoSIntraMeasurementPingRepository, times(1)).findByMeasurement(any());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetPingMeasurementByMeasurementWithNullMeasurementParameter() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = null;//getQoSIntraMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenReturn(Optional.of(pingMeasurement));

		try {

			qoSDBService.getPingMeasurementByMeasurement(measurementParam);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("QoSIntraMeasurement" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).findByMeasurement(any());
			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPingMeasurementByMeasurementFindByMeasurementThrowsException() {

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenThrow(HibernateException.class);

		try {

			qoSDBService.getPingMeasurementByMeasurement(measurementParam);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraMeasurementPingRepository, times(1)).findByMeasurement(any());
			throw ex;
		}

	}

	//=================================================================================================
	// Tests of logMeasurementToDB

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testLogMeasurementToDB() {

		final String address = "address";
		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenReturn(measurementLog);

		qoSDBService.logMeasurementToDB(address, calculations, aroundNow);

		verify(qoSIntraPingMeasurementLogRepository, times(1)).saveAndFlush(any());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementToDBWithNullAddressParameter() {

		final String address = null;//"address";
		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenReturn(measurementLog);

		try {

			qoSDBService.logMeasurementToDB(address, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("Address" + EMPTY_OR_NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogRepository, times(0)).saveAndFlush(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementToDBWithEmptyAddressParameter() {

		final String address = "   ";//"address";
		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenReturn(measurementLog);

		try {

			qoSDBService.logMeasurementToDB(address, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("Address" + EMPTY_OR_NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogRepository, times(0)).saveAndFlush(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementToDBWithNullAroundNowParameter() {

		final String address = "address";
		final ZonedDateTime aroundNow = null;//ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenReturn(measurementLog);

		try {

			qoSDBService.logMeasurementToDB(address, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("ZonedDateTime" + NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogRepository, times(0)).saveAndFlush(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementToDBWithNullCalculationsParameter() {

		final String address = "address";
		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = null;//getCalculationsForTest();

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenReturn(measurementLog);

		try {

			qoSDBService.logMeasurementToDB(address, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("PingMeasurementCalculationsDTO" + NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogRepository, times(0)).saveAndFlush(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testLogMeasurementToDBSaveAndFlushThrowException() {

		final String address = "address";
		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenThrow(HibernateException.class);

		try {

			qoSDBService.logMeasurementToDB(address, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraPingMeasurementLogRepository, times(1)).saveAndFlush(any());

			throw ex;
		}

	}

	//=================================================================================================
	// Tests of logMeasurementDetailsToDB

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testLogMeasurementDetailsToDB() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);
		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(valueCapture.capture())).thenReturn(measurementLogDetailsList);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		qoSDBService.logMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		verify(qoSIntraPingMeasurementLogDetailsRepository, times(1)).saveAll(any());
		verify(qoSIntraPingMeasurementLogDetailsRepository, times(1)).flush();

		final List<QoSIntraPingMeasurementLogDetails> captured = valueCapture.getValue();
		assertEquals(responseList.size(), captured.size());
		for (final QoSIntraPingMeasurementLogDetails qoSIntraPingMeasurementLogDetails : captured) {

			assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasurementLog());
			assertNotNull(qoSIntraPingMeasurementLogDetails.isSuccessFlag());
			assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasuredAt());
			
		}

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementDetailsToDBWithNullResponseListParameter() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = null;//getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(any())).thenReturn(measurementLogDetailsList);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("List<IcmpPingResponse>" + EMPTY_OR_NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).flush();

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementDetailsToDBWithEmptyResponseListParameter() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = List.of();//getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(any())).thenReturn(measurementLogDetailsList);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("List<IcmpPingResponse>" + EMPTY_OR_NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).flush();

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementDetailsToDBWithNullMeasurementLogSavedParameter() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = null;//new QoSIntraPingMeasurementLog();

		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(any())).thenReturn(measurementLogDetailsList);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("QoSIntraPingMeasurementLog" + NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).flush();

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementDetailsToDBWithNullAroundNowParameter() {

		final ZonedDateTime aroundNow = null;//ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(any())).thenReturn(measurementLogDetailsList);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("ZonedDateTime" + NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).flush();

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void testLogMeasurementDetailsToDBSaveAllThrowsException() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(valueCapture.capture())).thenThrow(HibernateException.class);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(1)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).flush();

			final List<QoSIntraPingMeasurementLogDetails> captured = valueCapture.getValue();
			assertEquals(responseList.size(), captured.size());
			for (final QoSIntraPingMeasurementLogDetails qoSIntraPingMeasurementLogDetails : captured) {

				assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasurementLog());
				assertNotNull(qoSIntraPingMeasurementLogDetails.isSuccessFlag());
				assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasuredAt());
				
			}

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void testLogMeasurementDetailsToDBFlushThrowsException() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);
		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(valueCapture.capture())).thenReturn(measurementLogDetailsList);
		doThrow(HibernateException.class).when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(1)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(1)).flush();

			final List<QoSIntraPingMeasurementLogDetails> captured = valueCapture.getValue();
			assertEquals(responseList.size(), captured.size());
			for (final QoSIntraPingMeasurementLogDetails qoSIntraPingMeasurementLogDetails : captured) {

				assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasurementLog());
				assertNotNull(qoSIntraPingMeasurementLogDetails.isSuccessFlag());
				assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasuredAt());
				
			}

			throw ex;
		}

	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private List<QoSIntraPingMeasurement> getQosIntraPingMeasurementListForTest() {

		final int sizeOfMeasurementList = 3;
		final List<QoSIntraPingMeasurement> qoSIntraPingMeasurementList = new ArrayList<>(sizeOfMeasurementList);

		for (int i = 0; i < sizeOfMeasurementList; i++) {

			qoSIntraPingMeasurementList.add(getQosIntraPingMeasurementForTest());
		}

		return qoSIntraPingMeasurementList;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurement getQosIntraPingMeasurementForTest() {

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurement pingMeasurement = new QoSIntraPingMeasurement();

		pingMeasurement.setMeasurement(measurement);
		pingMeasurement.setAvailable(true);
		pingMeasurement.setMaxResponseTime(1);
		pingMeasurement.setMinResponseTime(1);
		pingMeasurement.setMeanResponseTimeWithoutTimeout(1);
		pingMeasurement.setMeanResponseTimeWithTimeout(1);
		pingMeasurement.setJitterWithoutTimeout(1);
		pingMeasurement.setJitterWithTimeout(1);
		pingMeasurement.setLostPerMeasurementPercent(0);
		pingMeasurement.setCountStartedAt(ZonedDateTime.now());
		pingMeasurement.setLastAccessAt(ZonedDateTime.now());
		pingMeasurement.setSent(35);
		pingMeasurement.setSentAll(35);
		pingMeasurement.setReceived(35);
		pingMeasurement.setReceivedAll(35);

		return pingMeasurement;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraMeasurement getQoSIntraMeasurementForTest() {

		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = new QoSIntraMeasurement(
				system, 
				QoSMeasurementType.PING, 
				ZonedDateTime.now());

		return measurement;
	}

	//-------------------------------------------------------------------------------------------------
	private System getSystemForTest() {

		final System system = new System(
				"testSystem",
				"address",
				12345,
				"authenticationInfo");

		return system;
	}

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemResponseDTOForTest() {

		return DTOConverter.convertSystemToSystemResponseDTO(getSystemForTest());
	}

	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> getResponseListForTest() {

		final int sizeOfList = 3;
		final List<IcmpPingResponse> responseList = new ArrayList<>(3);

		for (int i = 0; i < sizeOfList; i++) {
			responseList.add(getIcmpResponseForTest());
		}

		return responseList;
	}

	//-------------------------------------------------------------------------------------------------
	private IcmpPingResponse getIcmpResponseForTest() {

		final IcmpPingResponse response = new IcmpPingResponse();
		response.setDuration(1);
		response.setRtt(1);
		response.setSuccessFlag(true);
		response.setTimeoutFlag(false);
		response.setSize(32);
		response.setTtl(64);

		return response;
	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementCalculationsDTO getCalculationsForTest() {

		final PingMeasurementCalculationsDTO calculatiions = new PingMeasurementCalculationsDTO(
				true,//available,
				1,//maxResponseTime,
				1,//minResponseTime,
				1,//meanResponseTimeWithTimeout,
				1,//meanResponseTimeWithoutTimeout,
				1,//jitterWithTimeout,
				1,//jitterWithoutTimeout,
				35,//sentInThisPing,
				35,//receivedInThisPing,
				0);//lostPerMeasurementPercent);

		return calculatiions;
	}
}