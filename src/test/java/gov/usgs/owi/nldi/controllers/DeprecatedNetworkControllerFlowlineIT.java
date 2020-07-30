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
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.transform.FlowLineTransformer;



@EnableWebMvc
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@DatabaseSetup("classpath:/testData/crawlerSource.xml")

// This test class contains tests for the deprecated "navigate" endpoints.  Don't add
// new tests here and delete this class when we drop support for those endpoints.
// The new tests that are tied to the new "navigation" endpoints are in
// NetworkControllerFlowlineIT
public class DeprecatedNetworkControllerFlowlineIT extends BaseIT {

	@Value("${serverContextPath}")
	private String context;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private static final String RESULT_FOLDER  = "network/flowline/";

	@BeforeEach
	public void setUp() {
		urlRoot = "http://localhost:" + port + context;
	}

	//UT Testing
	@Test
	public void getComidUtTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13293474/navigate/UT",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"7",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13293474_UT.json"),
				true,
				false);
	}

	@Test
	public void getComidUtDistanceTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigate/UT?distance=10",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"9",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13297246_UT_distance_10.json"),
				true,
				false);
	}


	@Test
	public void getComidUtDistanceTestEmpty() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigate/UT?distance=",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"359",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13297246_UT_distance_empty.json"),
				true,
				false);

	}

	@Test
	public void getComidUtDistanceTestAboveMax() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigate/UT?distance=10000",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFlowlines.distance: distance must be between 1 and 9999 kilometers",
				false,
				false);
	}

	@Test
	public void getComidUtDistanceTestBelowMin() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigate/UT?distance=-1",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFlowlines.distance: distance must be between 1 and 9999 kilometers",
				false,
				false);
	}

	@Test
	public void getComidUtDiversionTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13294158/navigate/UT",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"15",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13294158_UT.json"),
				true,
				false);
	}

	//UM Testing
	@Test
	public void getComidUmTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13293474/navigate/UM",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"4",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13293474_UM.json"),
				true,
				false);
	}

	@Test
	public void getComidUmDistanceTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigate/UM?distance=10",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"6",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13297246_UM_distance_10.json"),
				true,
				false);
	}

	//DM Testing
	@Test
	public void getComidDmTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13296790/navigate/DM",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"5",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13296790_DM.json"),
				true,
				false);
	}

	@Test
	public void getComidDmDiversionsNotIncludedTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13294310/navigate/DM",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"42",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13294310_DM.json"),
				true,
				false);
	}

	@Test
	public void getComidDmDistanceTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13293474/navigate/DM?distance=10",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"8",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13293474_DM_distance_10.json"),
				true,
				false);
	}

	//DD Testing
	@Test
	public void getComidDdTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13294310/navigate/DD",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"49",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13294310_DD.json"),
				true,
				false);
	}

	@Test
	public void getComidDdDistanceTest() throws Exception {
		//We are going to sacrifice a little accuracy for performance, so this does not match the old way...
		assertEntity(restTemplate,
				"/linked-data/comid/13294310/navigate/DD?distance=11",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"13",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13294310_DD_distance_11.json"),
				true,
				false);
	}

	//PP Testing
	@Test
	public void getComidPpStopComidInvalidTest() throws Exception {
		// This deprecated endpoint and test are sharing the result file with
		// the current endpoint and test, so tweak the comparison string accordingly.
		String compareFile = getCompareFile(RESULT_FOLDER, "comid_13297246_PP_stop_13297198.json");
		compareFile = compareFile.replace("/flowlines", "");
		compareFile = compareFile.replace("navigation", "navigate");
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigate/PP?stopComid=13297198",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				MediaType.APPLICATION_JSON_VALUE,
				compareFile,
				true,
				true);
	}

	@Test
	public void getComidPpStopComidTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297198/navigate/PP?stopComid=13297246",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"12",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13297198_PP_stop_13297246.json"),
				true,
				false);
	}

	//Interesting diversion/tributary
	//There is another simple diversion between 13294248 and 13294242
	@Test
	public void interestingTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13293844/navigate/DM?distance=5",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"7",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13293844_DM_distance_5.json"),
				true,
				false);

		assertEntity(restTemplate,
				"/linked-data/comid/13293844/navigate/DD?distance=5",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"14",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13293844_DD_distance_5.json"),
				true,
				false);

		assertEntity(restTemplate,
				"/linked-data/comid/13294328/navigate/DM?distance=5",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"6",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13294328_DM_distance_5.json"),
				true,
				false);

		assertEntity(restTemplate,
				"/linked-data/comid/13294328/navigate/DD?distance=5",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"10",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13294328_DD_distance_5.json"),
				true,
				false);

		assertEntity(restTemplate,
				"/linked-data/comid/13294390/navigate/UM?distance=5",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"6",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13294390_UM_distance_5.json"),
				true,
				false);

		assertEntity(restTemplate,
				"/linked-data/comid/13294390/navigate/UT?distance=5",
				HttpStatus.OK.value(),
				FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
				"22",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13294390_UT_distance_5.json"),
				true,
				false);
	}

	//Parameter Error Testing
	@Test
	public void badNavigationModeTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297198/navigate/XX",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFlowlines.navigationMode: must match \"DD|DM|PP|UT|UM\"",
				false,
				false);
	}



	//UT Testing
	@Test
	public void getComidUtTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13293474/navigate/UT",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"7",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13293474_UT.json"),
			true,
			false);
	}

	@Test
	public void getComidUtDistanceTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13297246/navigate/UT?distance=10",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"9",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13297246_UT_distance_10.json"),
			true,
			false);
	}


	@Test
	public void getComidUtDistanceTestEmptyNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13297246/navigate/UT?distance=",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"359",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13297246_UT_distance_empty.json"),
			true,
			false);

	}

	@Test
	public void getComidUtDistanceTestAboveMaxNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13297246/navigate/UT?distance=10000",
			HttpStatus.BAD_REQUEST.value(),
			null,
			null,
			null,
			"getFlowlines.distance: distance must be between 1 and 9999 kilometers",
			false,
			false);
	}

	@Test
	public void getComidUtDistanceTestBelowMinNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13297246/navigate/UT?distance=-1",
			HttpStatus.BAD_REQUEST.value(),
			null,
			null,
			null,
			"getFlowlines.distance: distance must be between 1 and 9999 kilometers",
			false,
			false);
	}

	@Test
	public void getComidUtDiversionTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13294158/navigate/UT",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"15",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13294158_UT.json"),
			true,
			false);
	}

	//UM Testing
	@Test
	public void getComidUmTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13293474/navigate/UM",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"4",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13293474_UM.json"),
			true,
			false);
	}

	@Test
	public void getComidUmDistanceTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13297246/navigate/UM?f=json&distance=10",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"6",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13297246_UM_distance_10.json"),
			true,
			false);
	}

	//DM Testing
	@Test
	public void getComidDmTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13296790/navigate/DM",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"5",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13296790_DM.json"),
			true,
			false);
	}

	@Test
	public void getComidDmDiversionsNotIncludedTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13294310/navigate/DM",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"42",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13294310_DM.json"),
			true,
			false);
	}

	@Test
	public void getComidDmDistanceTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13293474/navigate/DM?distance=10",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"8",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13293474_DM_distance_10.json"),
			true,
			false);
	}

	//DD Testing
	@Test
	public void getComidDdTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13294310/navigate/DD",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"49",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13294310_DD.json"),
			true,
			false);
	}

	@Test
	public void getComidDdDistanceTestNavigation() throws Exception {
		//We are going to sacrifice a little accuracy for performance, so this does not match the old way...
		assertEntity(restTemplate,
			"/linked-data/comid/13294310/navigate/DD?distance=11",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"13",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13294310_DD_distance_11.json"),
			true,
			false);
	}

	//PP Testing
	@Test
	public void getComidPpStopComidInvalidTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13297246/navigate/PP?stopComid=13297198",
			HttpStatus.BAD_REQUEST.value(),
			null,
			null,
			null,
			null,
			true,
			true);
	}

	@Test
	public void getComidPpStopComidTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13297198/navigate/PP?stopComid=13297246",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"12",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13297198_PP_stop_13297246.json"),
			true,
			false);
	}

	//Interesting diversion/tributary
	//There is another simple diversion between 13294248 and 13294242
	@Test
	public void interestingTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13293844/navigate/DM?distance=5",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"7",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13293844_DM_distance_5.json"),
			true,
			false);

		assertEntity(restTemplate,
			"/linked-data/comid/13293844/navigate/DD?distance=5",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"14",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13293844_DD_distance_5.json"),
			true,
			false);

		assertEntity(restTemplate,
			"/linked-data/comid/13294328/navigate/DM?distance=5",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"6",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13294328_DM_distance_5.json"),
			true,
			false);

		assertEntity(restTemplate,
			"/linked-data/comid/13294328/navigate/DD?distance=5",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"10",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13294328_DD_distance_5.json"),
			true,
			false);

		assertEntity(restTemplate,
			"/linked-data/comid/13294390/navigate/UM?distance=5",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"6",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13294390_UM_distance_5.json"),
			true,
			false);

		assertEntity(restTemplate,
			"/linked-data/comid/13294390/navigate/UT?distance=5",
			HttpStatus.OK.value(),
			FlowLineTransformer.FLOW_LINES_COUNT_HEADER,
			"22",
			BaseController.MIME_TYPE_GEOJSON,
			getCompareFile(RESULT_FOLDER, "comid_13294390_UT_distance_5.json"),
			true,
			false);
	}

	//Parameter Error Testing
	@Test
	public void badNavigationModeTestNavigation() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13297198/navigate/XX",
			HttpStatus.BAD_REQUEST.value(),
			null,
			null,
			null,
			"getFlowlines.navigationMode: must match \"DD|DM|PP|UT|UM\"",
			false,
			false);
	}
}
