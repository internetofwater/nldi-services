package gov.usgs.owi.nldi.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.ResultHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import gov.usgs.owi.nldi.NavigationMode;
import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Navigation;
import gov.usgs.owi.nldi.services.Parameters;
import gov.usgs.owi.nldi.transform.ITransformer;

public class BaseControllerTest {

	@Mock
	private ConfigurationService configurationService;
	@Mock
	private LookupDao lookupDao;
	@Mock
	private StreamingDao streamingDao;
	@Mock
	private Navigation navigation;
	@Mock
	private Parameters parameters;
	@Mock
	private ITransformer transformer;
	@Mock
	private LogService logService;
	private HttpServletResponse response;

	private class TestBaseController extends BaseController {
		public TestBaseController(LookupDao inLookupDao, StreamingDao inStreamingDao, Navigation inNavigation, Parameters inParameters, ConfigurationService inConfigurationService, LogService inLogService) {
			super(inLookupDao, inStreamingDao, inNavigation, inParameters, inConfigurationService, inLogService);
		}
	}

	private TestBaseController controller;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		controller = new TestBaseController(lookupDao, streamingDao, navigation, parameters, configurationService, logService);
		response = new MockHttpServletResponse();
	}

	@Test
	public void addContentHeaderTest() {
		controller.addContentHeader(response);

		assertTrue(response.containsHeader(BaseController.HEADER_CONTENT_TYPE));
		assertEquals(BaseController.MIME_TYPE_GEOJSON, response.getHeader(BaseController.HEADER_CONTENT_TYPE));
	}

	@Test
	public void getSessionIdTest() throws Exception {
		when(navigation.navigate(anyMap())).thenReturn(new HashMap<String, String>());
		when(navigation.interpretResult(anyMap(), any(HttpServletResponse.class))).thenReturn("abc");
		assertEquals("abc", controller.getSessionId(new HashMap<String, Object>(), response));
		verify(navigation).navigate(anyMap());
		verify(navigation).interpretResult(anyMap(), any(HttpServletResponse.class));
	}

	@Test
	public void streamFeaturesTest() throws Exception {
		controller.streamFeatures(response, "123", "navigationMode", "456", "789", "dataSource", false);
		verify(streamingDao).stream(anyString(), anyMap(), any(ResultHandler.class));
		verify(navigation, never()).navigate(anyMap());
		verify(navigation, never()).interpretResult(anyMap(), any(HttpServletResponse.class));
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void streamFeaturesLegacyTest() throws Exception {
		when(navigation.navigate(anyMap())).thenReturn(new HashMap<String, String>());
		when(navigation.interpretResult(anyMap(), any(HttpServletResponse.class))).thenReturn(null, "abc");

		controller.streamFeatures(response, "123", "navigationMode", "456", "789", "dataSource", true);
		verify(streamingDao, never()).stream(anyString(), anyMap(), any(ResultHandler.class));

		controller.streamFeatures(response, "123", "navigationMode", "456", "789", "dataSource", true);
		verify(streamingDao).stream(anyString(), anyMap(), any(ResultHandler.class));
	}

	@Test
	public void streamFlowLinesTest() throws Exception {
		controller.streamFlowLines(response, "123", "navigationMode", "456", "789", false);
		verify(streamingDao).stream(anyString(), anyMap(), any(ResultHandler.class));
		verify(navigation, never()).navigate(anyMap());
		verify(navigation, never()).interpretResult(anyMap(), any(HttpServletResponse.class));
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void streamBasinTest() throws Exception {
		controller.streamBasin(response, "123");
		verify(streamingDao).stream(anyString(), anyMap(), any(ResultHandler.class));
		verify(navigation, never()).navigate(anyMap());
		verify(navigation, never()).interpretResult(anyMap(), any(HttpServletResponse.class));
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void streamFlowLinesLegacyTest() throws Exception {
		when(navigation.navigate(anyMap())).thenReturn(new HashMap<String, String>());
		when(navigation.interpretResult(anyMap(), any(HttpServletResponse.class))).thenReturn(null, "abc");
		controller.streamFlowLines(response, "123", "navigationMode", "456", "789", true);
		verify(streamingDao, never()).stream(anyString(), anyMap(), any(ResultHandler.class));

		controller.streamFlowLines(response, "123", "navigationMode", "456", "789", true);
		verify(streamingDao).stream(anyString(), anyMap(), any(ResultHandler.class));
	}

	@Test
	public void streamResultsTest() {
		controller.streamResults(transformer, "navigationMode", new HashMap<String, Object>());
		verify(streamingDao).stream(anyString(), anyMap(), any(ResultHandler.class));
		verify(transformer).end();
	}

	@Test
	public void isLegacyTest() {
		assertTrue(controller.isLegacy("true", NavigationMode.DM.toString()));
		assertTrue(controller.isLegacy("true", null));
		assertTrue(controller.isLegacy(" true ", null));
		assertTrue(controller.isLegacy("True ", null));

		assertFalse(controller.isLegacy(null, null));
		assertFalse(controller.isLegacy("", null));
		assertFalse(controller.isLegacy("  ", null));
		assertFalse(controller.isLegacy("false", null));
		assertFalse(controller.isLegacy("abc", null));

		assertFalse(controller.isLegacy(null, NavigationMode.DD.toString()));
		assertFalse(controller.isLegacy(null, NavigationMode.DM.toString()));
		assertFalse(controller.isLegacy(null, NavigationMode.UM.toString()));
		assertFalse(controller.isLegacy(null, NavigationMode.UT.toString()));
		assertTrue(controller.isLegacy(null, NavigationMode.PP.toString()));
	}
}
