package gov.usgs.owi.nldi.controllers;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import gov.usgs.owi.nldi.BaseIT;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONArrayAs;

@EnableWebMvc
@SpringBootTest(webEnvironment=WebEnvironment.DEFINED_PORT,
	properties={
		"nldi.displayHost=localhost:8081",
		"nldi.displayProtocol=http",
		"nldi.displayPath=/nldi"})
@TestPropertySource(properties = "server.port=8081")
@DatabaseSetup("classpath:/testData/crawlerSource.xml")
public class LookupControllerIT extends BaseIT {

	@Value("${serverContextPath}")
	private String context;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private static final String RESULT_FOLDER  = "lookup/";

	@BeforeEach
	public void setUp() {
		urlRoot = "http://localhost:" + port + context;
	}

	@Test
	public void getCharacteristicsTest() throws Exception {
		assertEntity(restTemplate,
				"/lookups/tot/characteristics",
				HttpStatus.OK.value(),
				null,
				null,
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "meta/tot.json"),
				true,
				false);
	}

	@Test
	public void getLookupsTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
			"/lookups?f=json",
			HttpStatus.OK.value(),
			null,
			null,
			null,
			null,
			true,
			false);
		assertThat(new JSONArray(actualbody),
			sameJSONArrayAs(new JSONArray(getCompareFile(RESULT_FOLDER, "toc.json"))).allowingAnyArrayOrdering());

	}


	@Test
	public void getLookupsRedirectTest() throws Exception {
		assertEntity(restTemplate,
			"/lookups/tot?f=json",
			HttpStatus.OK.value(),
			null,
			null,
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "meta/tot.json"),
			true,
			false);

	}
}
