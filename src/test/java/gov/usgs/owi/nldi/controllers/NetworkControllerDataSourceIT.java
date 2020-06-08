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
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.transform.FeatureTransformer;

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

	@Before
	public void setup() {
		urlRoot = "http://localhost:" + port + context;
	}

	//UT Testing
	@Test
	public void getComidUtTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13293474/navigate/UT/wqp",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"22",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13293474_UT.json"),
				true,
				false);
	}

	@Test
	public void getComidUtEmptyParamsTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13293474/navigate/UT/wqp?distance=&stopComid=&legacy=",
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
				"/linked-data/comid/13297246/navigate/UT/wqp?distance=10",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"6",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13297246_UT_distance_10.json"),
				true,
				false);
	}

	//UM Testing
	@Test
	public void getComidUmTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13293474/navigate/UM/wqp",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"17",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13293474_UM.json"),
				true,
				false);
	}

	@Test
	public void getComidUmEmptyParamsTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13293474/navigate/UM/wqp?distance=&stopComid=&legacy=",
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
				"/linked-data/comid/13297246/navigate/UM/wqp?distance=10",
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
				"/linked-data/comid/13296790/navigate/DM/wqp",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"6",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13296790_DM.json"),
				true,
				false);
	}

	@Test
	public void getComidDmEmptyParamsTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13296790/navigate/DM/wqp?distance=&stopComid=&legacy=",
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
				"/linked-data/comid/13293474/navigate/DM/wqp?distance=10",
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
				"/linked-data/comid/13294310/navigate/DD/wqp",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"17",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13294310_DD.json"),
				true,
				false);
	}

	@Test
	public void getComidDdEmptyParmsTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13294310/navigate/DD/wqp?distance=&stopComid=&legacy=",
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
				"/linked-data/comid/13294310/navigate/DD/wqp?distance=11",
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
		assertEntity(restTemplate,
				"/linked-data/comid/13297246/navigate/PP/wqp?stopComid=13297198",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				MediaType.APPLICATION_JSON_UTF8_VALUE,
				getCompareFile(RESULT_FOLDER, "comid_13297246_PP_stop_13297198.json"),
				true,
				true);
	}

	@Test
	public void getComidPpStopComidTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297198/navigate/PP/wqp?stopComid=13297246",
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
				"/linked-data/comid/13297198/navigate/XX/wqp",
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
				"/linked-data/comid/13297246/navigate/UT/basin",
				HttpStatus.OK.value(),
				null,
				null,
				BaseController.MIME_TYPE_GEOJSON,
				null,
				false,
				false);
	}
}
