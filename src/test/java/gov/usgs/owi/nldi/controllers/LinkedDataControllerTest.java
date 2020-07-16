package gov.usgs.owi.nldi.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Navigation;
import gov.usgs.owi.nldi.services.Parameters;
import gov.usgs.owi.nldi.services.TestConfigurationService;

public class LinkedDataControllerTest {
	@Mock
	private StreamingDao streamingDao;
	@Mock
	private LookupDao lookupDao;
	@Mock
	private Navigation navigation;
	@Mock
	private Parameters parameters;
	@Mock
	private LogService logService;

	private TestConfigurationService configurationService;
	private LinkedDataController controller;
	private MockHttpServletResponse response;
	private MockHttpServletRequest request;

	@BeforeEach
	@SuppressWarnings("unchecked")
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		//Need to mock this for only a few tests
		doCallRealMethod().when(streamingDao).stream(anyString(), anyMap(), any());

		configurationService = new TestConfigurationService();
		controller = new LinkedDataController(lookupDao, streamingDao, navigation, parameters, configurationService, logService);
		response = new MockHttpServletResponse();
		request = new MockHttpServletRequest();

		when(logService.logRequest(any(HttpServletRequest.class))).thenReturn(BigInteger.ONE);
		when(lookupDao.getList(any(String.class), anyMap())).thenReturn(new ArrayList<Map<String, Object>>(), null, getTestList());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getComidTest() {
		when(lookupDao.getComid(anyString(), anyMap())).thenReturn(goodFeature(), null, missingFeature());

		assertEquals("12345", controller.getComid("abc", "def"));

		assertNull(controller.getComid("abc", "def"));

		assertNull(controller.getComid("abc", "def"));
	}

	@Test
	public void getComidWithNullFeatureSourceTest() {
		assertThrows(IllegalArgumentException.class, () -> {
			controller.getComid(null, "def");
		});

	}

	@Test
	public void getComidWithNullFeatureIdTest() {
		assertThrows(IllegalArgumentException.class, () -> {
			controller.getComid("FakeFeatureSource", null);
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getFlowlinesTest() throws Exception {
		when(lookupDao.getComid(anyString(), anyMap())).thenReturn(null, goodFeature());
		controller.getFlowlines(request, response, "DoesntMatter", "DoesntMatter", null, null, null, null);
		verify(logService).logRequest(any(HttpServletRequest.class));
		verify(logService).logRequestComplete(any(BigInteger.class), any(int.class));
		//Mock lookupDao 1st response of null means the comid is not found, thus a 404
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

		controller.getFlowlines(request, response, null, null, null, null, null, null);
		verify(logService, times(2)).logRequest(any(HttpServletRequest.class));
		verify(logService, times(2)).logRequestComplete(any(BigInteger.class), any(int.class));
		//Mock lookupDao 2nd response doesn't actually exist, thus causes a 500 when we try to get flowlines
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getFeaturesTest() throws Exception {
		when(lookupDao.getComid(anyString(), anyMap())).thenReturn(null, goodFeature());
		controller.getFeatures(request, response, "DoesntMatter", "DoesntMatter", null, null, null, null, null);
		verify(logService).logRequest(any(HttpServletRequest.class));
		verify(logService).logRequestComplete(any(BigInteger.class), any(int.class));
		//Mock lookupDao 1st response of null means the comid is not found, thus a 404
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

		controller.getFeatures(request, response, null, null, null, null, null, null, null);
		verify(logService, times(2)).logRequest(any(HttpServletRequest.class));
		verify(logService, times(2)).logRequestComplete(any(BigInteger.class), any(int.class));
		//Mock lookupDao 2nd response doesn't actually exist, thus causes a 500 when we try to get features
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
	}

	public static Map<String, Object> goodFeature() {
		Map<String, Object> rtn = new LinkedHashMap<>();
		rtn.put(Parameters.COMID, "12345");
		return rtn;
	}

	public static Map<String, Object> missingFeature() {
		Map<String, Object> rtn = new LinkedHashMap<>();
		return rtn;
	}

	@Test
	public void getCharacteristicDataWithNullParamsTest() throws IOException {
		controller.getCharacteristicData(request, response, null, null, null, null);
		verify(logService).logRequest(any(HttpServletRequest.class));
		verify(logService).logRequestComplete(any(BigInteger.class), any(int.class));
		//this is a INTERNAL_SERVER_ERROR because of NPEs that shouldn't happen in real life.
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
	}

	@Test
	public void getCharacteristicDataWithNonexistingComidTest() throws IOException {
		controller.getCharacteristicData(request, response, "NowhereSource", "IDontExist", null, null);
		verify(logService).logRequest(any(HttpServletRequest.class));
		verify(logService).logRequestComplete(any(BigInteger.class), any(int.class));
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
	}

	@Test
	public void getBasinTest() throws Exception {
		when(lookupDao.getComid(anyString(), anyMap())).thenReturn(goodFeature());
		doNothing().when(streamingDao).stream(anyString(), anyMap(), any());

		controller.getBasin(request, response, "DoesntMatter", "DoesntMatter");
		verify(logService).logRequest(any(HttpServletRequest.class));
		verify(logService).logRequestComplete(any(BigInteger.class), any(int.class));
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void getBasinWithNullParamsTest() throws Exception {
		controller.getBasin(request, response, null, null);
		verify(logService).logRequest(any(HttpServletRequest.class));
		verify(logService).logRequestComplete(any(BigInteger.class), any(int.class));
		//this is a INTERNAL_SERVER_ERROR because of NPEs that shouldn't happen in real life.
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
	}

	@Test
	public void getBasinWithNonexistingComidTest() throws Exception {
		controller.getBasin(request, response, "NowhereSource", "IDontExist");
		verify(logService).logRequest(any(HttpServletRequest.class));
		verify(logService).logRequestComplete(any(BigInteger.class), any(int.class));
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
	}

	@Test
	public void getDataSourcesTest() throws UnsupportedEncodingException {
		List<Map<String, Object>> out = controller.getDataSources(request, response);
		verify(logService).logRequest(any(HttpServletRequest.class));
		verify(logService).logRequestComplete(any(BigInteger.class), any(int.class));
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("[{source=comid, sourceName=NHDPlus comid, features=http://owi-test.usgs.gov:8080/test-url/linked-data/comid}]", out.toString());
	}

	@Test
	public void getFeaturestest() {
		try {
			controller.getFeatures(request, response, null);
		} catch (Exception e) {
			assertTrue(e instanceof NullPointerException);
		}
		verify(logService).logRequest(any(HttpServletRequest.class));
		verify(logService).logRequestComplete(any(BigInteger.class), any(int.class));
		//this is a INTERNAL_SERVER_ERROR because of NPEs that shouldn't happen in real life.
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
	}

	@Test
	public void getRegisteredFeatureTest() {
		try {
			controller.getRegisteredFeature(request, response, null, null);
		} catch (Exception e) {
			assertTrue(e instanceof NullPointerException);
		}
		verify(logService).logRequest(any(HttpServletRequest.class));
		verify(logService).logRequestComplete(any(BigInteger.class), any(int.class));
		//this is a INTERNAL_SERVER_ERROR because of NPEs that shouldn't happen in real life.
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
	}

	@Test
	public void getNavigationTypesTest() throws UnsupportedEncodingException {
		controller.getNavigationTypes(request, response, null, null);
		verify(logService).logRequest(any(HttpServletRequest.class));
		verify(logService).logRequestComplete(any(BigInteger.class), any(int.class));
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

		response = new MockHttpServletResponse();
		controller.getNavigationTypes(request, response, null, null);
		verify(logService, times(2)).logRequest(any(HttpServletRequest.class));
		verify(logService, times(2)).logRequestComplete(any(BigInteger.class), any(int.class));
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

		response = new MockHttpServletResponse();
		Map<String, Object> out = controller.getNavigationTypes(request, response, "test", "test123");
		verify(logService, times(3)).logRequest(any(HttpServletRequest.class));
		verify(logService, times(3)).logRequestComplete(any(BigInteger.class), any(int.class));
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("{upstreamMain=http://owi-test.usgs.gov:8080/test-url/linked-data/test/test123/navigate/UM, upstreamTributaries=http://owi-test.usgs.gov:8080/test-url/linked-data/test/test123/navigate/UT, downstreamMain=http://owi-test.usgs.gov:8080/test-url/linked-data/test/test123/navigate/DM, downstreamDiversions=http://owi-test.usgs.gov:8080/test-url/linked-data/test/test123/navigate/DD}",
				out.toString());
	}

	public static List<Map<String, Object>> getTestList() {
		List<Map<String, Object>> rtn = new ArrayList<>();
		Map<String, Object> entry = new HashMap<>();
		entry.put("key", "value");
		rtn.add(entry);
		return rtn;
	}
}
