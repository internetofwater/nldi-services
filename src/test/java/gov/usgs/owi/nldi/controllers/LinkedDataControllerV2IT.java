package gov.usgs.owi.nldi.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONArrayAs;

import gov.usgs.owi.nldi.transform.FlowLineTransformer;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.usgs.owi.nldi.BaseIT;

@EnableWebMvc
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@DatabaseSetup("classpath:/testData/crawlerSource.xml")
public class LinkedDataControllerV2IT extends BaseIT {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private static final String RESULT_FOLDER  = "feature/flowline/";
	private static final String RESULT_FOLDER_WQP  = "feature/flowline/wqp/";

	@BeforeEach
	public void setUp() {
		urlRoot = "http://localhost:" + port + context;
	}


	//Navigation Types Testing
	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getNavigationOptionsTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
			"/linked-data/v2/wqp/USGS-05427880/navigate/UT?f=json",
			HttpStatus.OK.value(),
			null,
			null,
			MediaType.APPLICATION_JSON_VALUE,
			null,
			true,
			false);
		assertThat(new JSONArray(actualbody),
			sameJSONArrayAs(new JSONArray(getCompareFile(RESULT_FOLDER, "navigate_V2.json"))).allowingAnyArrayOrdering());

	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getNavigationOptionsTestBadRequest() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/v2/wqp/USGS-05427880/navigate/XX",
			HttpStatus.BAD_REQUEST.value(),
			null,
			null,
			null,
			null,
			false,
			false);

	}


	//Flowlines

	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpUMTest() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/v2/wqp/USGS-05427880/navigate/UM/flowlines?f=json",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"10",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER_WQP, "wqp_USGS-05427880_UM.json"),
			true,
			false);
	}
}
