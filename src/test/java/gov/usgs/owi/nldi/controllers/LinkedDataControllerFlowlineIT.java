package gov.usgs.owi.nldi.controllers;

import org.junit.Before;
import org.junit.Test;
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
import gov.usgs.owi.nldi.transform.FlowLineTransformer;


@EnableWebMvc
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@DatabaseSetup("classpath:/testData/crawlerSource.xml")
public class LinkedDataControllerFlowlineIT extends BaseIT {

	@Value("${serverContextPath}")
	private String context;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private static final String RESULT_FOLDER_WQP  = "feature/flowline/wqp/";
	private static final String RESULT_FOLDER_HUC  = "feature/flowline/huc12pp/";

	@Before
	public void setup() {
		urlRoot = "http://localhost:" + port + context;
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpUMTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880/navigate/UM",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"10",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER_WQP, "wqp_USGS-05427880_UM.json"),
				true,
				false);
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureHuc12pp.xml")
	public void getHuc12ppDM10Test() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/huc12pp/070900020601/navigate/DM?distance=10",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"6",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER_HUC, "huc12pp_070900020601_DM_distance_10.json"),
				true,
				false);
	}


	@Test
	@DatabaseSetup("classpath:/testData/featureHuc12pp.xml")
	public void getHuc12ppDM10000TestDistanceAboveMax() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/huc12pp/070900020601/navigate/DM?distance=10000",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFlowlines.distance: distance must be between 1 and 9999 kilometers",
				false,
				false);
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureHuc12pp.xml")
	public void getHuc12ppDM0TestDistanceBelowMin() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/huc12pp/070900020601/navigate/DM?distance=-1",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFlowlines.distance: distance must be between 1 and 9999 kilometers",
				false,
				false);
	}


	@Test
	@DatabaseSetup("classpath:/testData/featureHuc12pp.xml")
	public void getHuc12ppDMTestEmptyDistance() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/huc12pp/070900020601/navigate/DM?distance=",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"51",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER_HUC, "huc12pp_070900020601_DM_distance_empty.json"),
				true,
				false);


	}

	@Test
	public void badInputTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqx/USGS-05427880/navigate/DM",
				HttpStatus.NOT_FOUND.value(),
				null,
				null,
				null,
				null,
				true,
				false);
	}

	//Parameter Error Testing
	@Test
	public void badNavigationModeTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880/navigate/XX",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFlowlines.navigationMode: must match \"DD|DM|PP|UT|UM\"",
				false,
				false);
	}

}
