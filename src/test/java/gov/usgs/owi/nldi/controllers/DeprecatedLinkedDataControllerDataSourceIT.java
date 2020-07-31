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

@EnableWebMvc
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@DatabaseSetup("classpath:/testData/crawlerSource.xml")

// This test class contains tests for the deprecated "navigate" endpoints.  Don't add
// new tests here and delete this class when we drop support for those endpoints.
// The new tests that are tied to the new "navigation" endpoints are in
// LinkedDataControllerDataSourceIT
public class DeprecatedLinkedDataControllerDataSourceIT extends BaseIT {

	@Value("${serverContextPath}")
	private String context;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;


	private static final String RESULT_FOLDER  = "feature/other/";

	@BeforeEach
	public void setUp() {
		urlRoot = "http://localhost:" + port + context;
	}



	//Navigation Within Datasource Testing
	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpUtTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880/navigate/UT/wqp",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"13",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER_WQP, "wqp_USGS-05427880_UT_wqp.json"),
				true,
				false);
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpUtTestDistance1() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880/navigate/UT/wqp?distance=1",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"6",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER_WQP, "wqp_USGS-05427880_UT_wqp_distance_1.json"),
				true,
				false);

	}


	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpUtTestDistanceEmpty() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880/navigate/UT/wqp?distance=",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"13",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER_WQP, "wqp_USGS-05427880_UT_wqp_distance_empty.json"),
				true,
				false);
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpUtTestDistanceAboveMax() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880/navigate/UT/wqp?distance=10000",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFeaturesDeprecated.distance: distance must be between 1 and 9999 kilometers",
				false,
				false);
	}


	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpUtTestDistanceBelowMin() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880/navigate/UT/wqp?distance=-1",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFeaturesDeprecated.distance: distance must be between 1 and 9999 kilometers",
				false,
				false);
	}

	//Navigation Different Datasource Testing
	@Test
	@DatabaseSetup("classpath:/testData/featureHuc12pp.xml")
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpDmTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880/navigate/DM/huc12pp",
				HttpStatus.OK.value(),
				FeatureTransformer.FEATURE_COUNT_HEADER,
				"9",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER_WQP, "wqp_USGS-05427880_DM_huc12pp.json"),
				true,
				false);
	}

	@Test
	public void badInputTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqx/USGS-05427880/navigate/DM/huc12pp",
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
				"/linked-data/wqp/USGS-05427880/navigate/XX/huc12pp",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"getFeaturesDeprecated.navigationMode: must match \"DD|DM|PP|UT|UM\"",
				false,
				false);
	}

	//Navigation Types Testing
	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getNavigationTypesTest() throws Exception {
		String compareFile = getCompareFile(RESULT_FOLDER, "wqp_USGS-05427880.json");
		// The compare file is shared between the current test in LinkedDataControllerOtherIT.java
		// and the deprecated one here, so adjust it.
		compareFile = compareFile.replace("navigation", "navigate");
		assertEntity(restTemplate,
			"/linked-data/wqp/USGS-05427880/navigate",
			HttpStatus.OK.value(),
			null,
			null,
			MediaType.APPLICATION_JSON_VALUE,
			compareFile,
			true,
			false);
	}

	@Test
	public void getNavigationTypesNotFoundTest() throws Exception {
		assertEntity(restTemplate,
			"/linked-data/wqx/USGS-05427880/navigate",
			HttpStatus.NOT_FOUND.value(),
			null,
			null,
			MediaType.APPLICATION_JSON_VALUE,
			null,
			true,
			false);

		assertEntity(restTemplate,
			"/linked-data/wqp/USGX-05427880/navigate",
			HttpStatus.NOT_FOUND.value(),
			null,
			null,
			MediaType.APPLICATION_JSON_VALUE,
			null,
			true,
			false);
	}


}
