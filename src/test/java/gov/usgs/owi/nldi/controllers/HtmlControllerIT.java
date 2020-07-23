package gov.usgs.owi.nldi.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.usgs.owi.nldi.BaseIT;

@EnableWebMvc
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@DatabaseSetup("classpath:/testData/crawlerSource.xml")
public class HtmlControllerIT extends BaseIT {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@BeforeEach
	public void setUp() {
		urlRoot = "http://localhost:" + port + context;
	}


	@Test
	public void getLinkedDataHtmlTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/linked-data/nwissite?f=html",
				HttpStatus.OK.value(),
				null,
				null,
				null,
				null,
				false,
				false);

		assertTrue(actualbody.contains("?f=json"));
		assertTrue(actualbody.contains("/linked-data/nwissite"));
		assertTrue(actualbody.contains("<html>"));
		assertTrue(actualbody.contains("</html"));
		assertTrue(actualbody.contains("<a "));
		assertTrue(actualbody.contains("href="));
		assertTrue(actualbody.contains("a>"));
	}

	@Test
	public void getNetworkHtmlTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/linked-data/comid/13302592/tot?f=html",
				HttpStatus.OK.value(),
				null,
				null,
				null,
				null,
				false,
				false);

		assertTrue(actualbody.contains("?f=json"));
		assertTrue(actualbody.contains("/linked-data/comid/13302592/tot"));
		assertTrue(actualbody.contains("<html>"));
		assertTrue(actualbody.contains("</html"));
		assertTrue(actualbody.contains("<a "));
		assertTrue(actualbody.contains("href="));
		assertTrue(actualbody.contains("a>"));
	}


	@Test
	public void getLookupsHtmlTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/lookups/x?f=html",
				HttpStatus.OK.value(),
				null,
				null,
				null,
				null,
				false,
				false);
		assertTrue(actualbody.contains("/lookups/x?f=json"));
		assertTrue(actualbody.contains("<html>"));
		assertTrue(actualbody.contains("</html"));
		assertTrue(actualbody.contains("<a "));
		assertTrue(actualbody.contains("href="));
		assertTrue(actualbody.contains("a>"));
	}
}
