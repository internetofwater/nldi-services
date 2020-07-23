package gov.usgs.owi.nldi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@DatabaseSetup("classpath:/testData/featureWqp.xml")
public class NetworkControllerPositionIT extends BaseIT {

	@Value("${serverContextPath}")
	private String context;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;
	private static final String RESULT_FOLDER  = "network/feature/wqp/";

	@BeforeEach
	public void setUp() {
		urlRoot = "http://localhost:" + port + context;
	}


	//Latitude/Longitude Testing
	@Test
	public void getCoordinatesTest() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/position?coords=POINT(-89.35 43.0864)",
			HttpStatus.OK.value(),
			null,
			null,
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comidLatLon.json"),
			true,
			true);
	}

	@Test
	public void getCoordinatesTestMalformedNumber() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/position?coords=POINT(-89.35 NotANumber)",
			HttpStatus.BAD_REQUEST.value(),
			null,
			null,
			null,
			null,
			false,
			false);
	}

	@Test
	public void getCoordinatesTestMalformedParam() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/position?coords=POINTBAD(-89.35 43.0864)",
			HttpStatus.BAD_REQUEST.value(),
			null,
			null,
			null,
			null,
			false,
			false);
	}

	@Test
	public void getCoordinatesTestNoCoordinates() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/position",
			HttpStatus.BAD_REQUEST.value(),
			null,
			null,
			null,
			null,
			false,
			false);
	}
}
