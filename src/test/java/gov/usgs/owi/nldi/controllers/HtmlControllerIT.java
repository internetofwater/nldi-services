package gov.usgs.owi.nldi.controllers;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

		checkRedirectHtml(actualbody,"/linked-data/nwissite");
	}

	@Test
	public void getLinkedDataWithQueryStringTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
			"/linked-data/comid/position?coords=POINT(-89.35 48.064)&f=html",
			HttpStatus.OK.value(),
			null,
			null,
			null,
			null,
			false,
			false);
		checkRedirectHtml(actualbody,"/linked-data/comid/position?f=json&coords=POINT(-89.35%2048.064)");
	}

	@Test
	public void getLinkedDataWithAlternateQueryStringTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
			"/linked-data/comid/position?f=html&coords=POINT(-89.35 48.064)",
			HttpStatus.OK.value(),
			null,
			null,
			null,
			null,
			false,
			false);
		checkRedirectHtml(actualbody, "/linked-data/comid/position?f=json&coords=POINT(-89.35%2048.064)");

	}

	@Test
	public void getLinkedDataDataSourcesTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
			"/linked-data?f=html",
			HttpStatus.OK.value(),
			null,
			null,
			null,
			null,
			false,
			false);
	    checkRedirectHtml(actualbody, "/linked-data?f=json");
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpUMTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
			"/linked-data/v2/wqp/USGS-05427880/navigate/UM/flowlines?f=html",
			HttpStatus.OK.value(),
			null,
			null,
			null,
			null,
			true,
			false);
		checkRedirectHtml(actualbody, "/linked-data/v2/wqp/USGS-05427880/navigate/UM/flowlines?f=json");
	}

	@Test
	public void getComidUmDistanceTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
			"/linked-data/v2/comid/13297246/navigate/UM/flowlines?f=html&distance=10",
			HttpStatus.OK.value(),
			null,
			null,
			null,
			null,
			false,
			false);
		checkRedirectHtml(actualbody, "/linked-data/v2/comid/13297246/navigate/UM/flowlines?f=json&distance=10");
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getNavigationOptionsTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
			"/linked-data/v2/wqp/USGS-05427880/navigate/UT?f=html",
			HttpStatus.OK.value(),
			null,
			null,
			null,
			null,
			false,
			false);
		checkRedirectHtml(actualbody, "/linked-data/v2/wqp/USGS-05427880/navigate/UT?f=json");
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

		checkRedirectHtml(actualbody, "/linked-data/comid/13302592/tot");
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
		checkRedirectHtml(actualbody, "/lookups/x?f=json");
	}


	private void checkRedirectHtml(String actualbody, String link) {
		assertTrue(actualbody.contains(link));
		assertFalse(actualbody.contains("f=html"));
		assertTrue(actualbody.trim().startsWith("<html>"));
		assertTrue(actualbody.trim().endsWith("</html>"));
		assertTrue(actualbody.contains("<a "));
		assertTrue(actualbody.contains("href="));
		assertTrue(actualbody.contains("a>"));
	}

}
