package gov.usgs.owi.nldi.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

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

public class HtmlControllerTest {

	private TestConfigurationService configurationService;
	private HtmlController controller;
	private MockHttpServletResponse response;
	private MockHttpServletRequest request;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		configurationService = new TestConfigurationService();
		controller = new HtmlController();
		response = new MockHttpServletResponse();
		request = new MockHttpServletRequest();
	}

	@Test
	public void ensureIsHttpsTest() throws Exception {
		StringBuffer url = new StringBuffer("http://labs-dev.wma.chs.usgs.gov/api/nldi/linked-data/nwissite?f=json");
		String link = controller.ensureIsHttps(url);
		assertEquals("https://labs-dev.wma.chs.usgs.gov/api/nldi/linked-data/nwissite?f=json", link);

		url = new StringBuffer("https://labs-dev.wma.chs.usgs.gov/api/nldi/linked-data/nwissite?f=json");
		link = controller.ensureIsHttps(url);
		assertEquals("https://labs-dev.wma.chs.usgs.gov/api/nldi/linked-data/nwissite?f=json", link);

		url = new StringBuffer("http://localhost:8080/api/nldi/linked-data/nwissite?f=json");
		link = controller.ensureIsHttps(url);
		assertEquals("http://localhost:8080/api/nldi/linked-data/nwissite?f=json", link);

	}
}
