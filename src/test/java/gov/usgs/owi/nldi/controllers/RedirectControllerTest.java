package gov.usgs.owi.nldi.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.TestConfigurationService;
import org.springframework.web.servlet.view.RedirectView;

public class RedirectControllerTest {
	@Mock
	private LogService logService;

	private TestConfigurationService configurationService;
	private RedirectController controller;

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		MockitoAnnotations.initMocks(this);

		configurationService = new TestConfigurationService();
        controller = new RedirectController(configurationService);
	}

	@Test
	public void getSwaggerTest() throws Exception {
		RedirectView redirectView= controller.getSwagger();
		String url = redirectView.getUrl();
		assertEquals("/test-url/v3/api-docs/swagger-config", configurationService.getSwaggerApiDocsUrl());
		assertTrue(url.contains("/test-url/swagger-ui/index.html?configUrl="));
	}

}