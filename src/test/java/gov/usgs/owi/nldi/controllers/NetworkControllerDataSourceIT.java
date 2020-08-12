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
import gov.usgs.owi.nldi.transform.FeatureTransformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableWebMvc
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@DatabaseSetup("classpath:/testData/crawlerSource.xml")
@DatabaseSetup("classpath:/testData/featureWqp.xml")
public class NetworkControllerDataSourceIT extends BaseIT {

	@Value("${serverContextPath}")
	private String context;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;
	private static final String RESULT_FOLDER  = "network/feature/wqp/";
	private static final String RESULT_FLOWLINE_FOLDER  = "network/flowline/";


	@BeforeEach
	public void setUp() {
		urlRoot = "http://localhost:" + port + context;
	}

	//UT Testing
	@Test
	public void getComidUtTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13293474/navigation/UT/wqp?distance=9999",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"22",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13293474_UT.json"),
				true,
				false);
	}

	@Test
	public void getComidUtDistanceTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigation/UT/wqp?distance=10",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"6",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13297246_UT_distance_10.json"),
				true,
				false);
	}

	@Test
	public void getComidUtDistanceTestEmpty() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigation/UT/wqp?distance=",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"91",
				BaseController.MIME_TYPE_GEOJSON,
                getCompareFile(RESULT_FOLDER, "comid_13297246_UT_distance_empty.json"),
				true,
				false);
	}

	@Test
	public void getComidUtDistanceTestAboveMax() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigation/UT/wqp?distance=10000",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFeatures.distance: distance must be between 1 and 9999 kilometers",
				false,
				false);
	}


	@Test
	public void getComidUtDistanceTestBelowMin() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigation/UT/wqp?distance=-1",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFeatures.distance: distance must be between 1 and 9999 kilometers",
				false,
				false);
	}

	//UM Testing
	@Test
	public void getComidUmTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13293474/navigation/UM/wqp?distance=9999",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"17",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13293474_UM.json"),
				true,
				false);
	}

	@Test
	public void getComidUmDistanceTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigation/UM/wqp?distance=10",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
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
				"/linked-data/comid/13296790/navigation/DM/wqp?distance=9999",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"6",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13296790_DM.json"),
				true,
				false);
	}

	@Test
	public void getComidDmDistanceTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13293474/navigation/DM/wqp?distance=10",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"31",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13293474_DM_distance_10.json"),
				true,
				false);
	}

	//DD Testing
	@Test
	public void getComidDdTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13294310/navigation/DD/wqp?distance=9999",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"17",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13294310_DD.json"),
				true,
				false);
	}

	@Test
	public void getComidDdDistanceTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13294310/navigation/DD/wqp?distance=11",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"1",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13294310_DD_distance_11.json"),
				true,
				false);
	}

	//PP Testing
	@Test
	public void getComidPpStopComidInvalidTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigation/PP/wqp?distance=9999&stopComid=13297198",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				null,
				true,
				true);
		assertEquals("400 BAD_REQUEST \"The stopComid must be downstream of the start comid.\"", actualbody);
	}

	@Test
	public void getComidPpStopComidTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297198/navigation/PP/wqp?distance=9999&stopComid=13297246",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"16",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13297198_PP_stop_13297246.json"),
				true,
				false);
	}

	//Parameter Error Testing
	@Test
	public void badNavigationModeTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297198/navigation/XX/wqp?distance=9999",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFeatures.navigationMode: must match \"DD|DM|PP|UT|UM\"",
				false,
				false);
	}

	@Test
	public void getBasinTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigation/UT/basin?distance=9999",
				HttpStatus.OK.value(),
				null,
				null,
				BaseController.MIME_TYPE_GEOJSON,
				null,
				false,
				false);
	}


	//Navigation Types Testing
	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getNavigationTypesTest() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/13294390/navigation",
			HttpStatus.OK.value(),
			null,
			null,
			MediaType.APPLICATION_JSON_VALUE,
			getCompareFile(RESULT_FLOWLINE_FOLDER, "navigation_types.json"),
			true,
			false);
	}

	@Test
	public void getNavigationTypesNotFoundTest() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/comid/123/navigation",
			HttpStatus.NOT_FOUND.value(),
			null,
			null,
			MediaType.APPLICATION_JSON_VALUE,
			null,
			true,
			false);

	}

}
