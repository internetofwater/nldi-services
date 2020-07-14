package gov.usgs.owi.nldi.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.TestConfigurationService;
import org.springframework.web.servlet.view.RedirectView;

public class RedirectControllerTest {
	@Mock
	private LogService logService;

	private TestConfigurationService configurationService;
	private RedirectController controller;
	private MockHttpServletResponse response;
	private MockHttpServletRequest request;

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		MockitoAnnotations.initMocks(this);

		//Need to mock this for only a few tests

		configurationService = new TestConfigurationService();
		response = new MockHttpServletResponse();
		request = new MockHttpServletRequest();
        controller = new RedirectController(configurationService);
		when(logService.logRequest(any(HttpServletRequest.class))).thenReturn(BigInteger.ONE);
	}

	@Test
	public void getSwaggerTest() throws Exception {
		RedirectView redirectView= controller.getSwagger();
		String url = redirectView.getUrl();
		assertEquals(configurationService.getSwaggerApiDocsUrl(), "/test-url/v3/api-docs/swagger-config");
		assertTrue(url.contains("/test-url/swagger-ui/index.html?configUrl="));
		assertEquals(configurationService.getSwaggerApiDocsUrl(), "/test-url/v3/api-docs/swagger-config");
	}

}