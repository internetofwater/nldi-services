package gov.usgs.owi.nldi.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONArrayAs;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.transform.BasinTransformer;

@EnableWebMvc
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@DatabaseSetup("classpath:/testData/crawlerSource.xml")
public class LinkedDataControllerOtherIT extends BaseIT {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private static final String RESULT_FOLDER  = "feature/other/";

	@Before
	public void setUp() {
		urlRoot = "http://localhost:" + port + context;
	}

	@Test
	public void getCharacteristicDataTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13302592/tot",
				HttpStatus.OK.value(),
				null,
				null,
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "data/comid_13302592_tot.json"),
				true,
				false);
	}

	@Test
	public void getCharacteristicDataInvalidTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13302592/tot?f=bad",
				HttpStatus.NOT_ACCEPTABLE.value(),
				null,
				null,
				null,
				null,
				false,
				false);
	}


	@Test
	public void getCharacteristicDataHtmlTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/linked-data/comid/13302592/tot?f=html",
				HttpStatus.OK.value(),
				null,
				null,
				null,
				null,
				false,
				false);
		testGenericHtml(actualbody, "/linked-data/comid/13302592/tot");

	}


	@Test
	public void getCharacteristicDataMissingTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/133999999/tot",
				HttpStatus.NOT_FOUND.value(),
				null,
				null,
				null,
				null,
				true,
				true);
	}

	@Test
	public void getCharacteristicDataFilteredTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13302592/tot?characteristicId=TOT_N97&characteristicId=TOT_ET",
				HttpStatus.OK.value(),
				null,
				null,
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "data/comid_13302592_tot_filtered.json"),
				true,
				false);
	}

	@Test
	public void getBasinTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13302592/basin",
				HttpStatus.OK.value(),
				BasinTransformer.BASIN_COUNT_HEADER,
				"1",
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "basin/comid_13302592.json"),
				true,
				false);
	}

	@Test
	public void getBasinHtmlTest() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Accept", "text/html");
		restTemplate.exchange("/linked-data/comid/13302592/basin", HttpMethod.GET, new HttpEntity<>(headers), String.class);
		String actualbody = assertEntity(restTemplate,
				"/linked-data/comid/13302592/basin?f=html",
				HttpStatus.OK.value(),
				null,
				null,
				null,
				null,
				false,
				false);
		testGenericHtml(actualbody, "/linked-data/comid/13302592/basin");
	}

	@Test
	public void getBasinMissingTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/1330259299/basin",
				HttpStatus.NOT_FOUND.value(),
				null,
				null,
				null,
				null,
				true,
				true);
	}

	@Test
	public void getBasinBadFormatTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/1330259299/basin?f=png",
				HttpStatus.NOT_ACCEPTABLE.value(),
				null,
				null,
				null,
				null,
				true,
				true);
	}

	//DataSources Testing
	@Test
	public void getDataSourcesTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/linked-data",
				HttpStatus.OK.value(),
				null,
				null,
				MediaType.APPLICATION_JSON_UTF8_VALUE,
				null,
				false,
				false);
		assertThat(new JSONArray(actualbody),
				sameJSONArrayAs(new JSONArray(getCompareFile(RESULT_FOLDER, "dataSources.json"))).allowingAnyArrayOrdering());
	}

	@Test
	public void getDataSourcesTestInvalid() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/linked-data?f=badformat",
				HttpStatus.NOT_ACCEPTABLE.value(),
				null,
				null,
				null,
				null,
				false,
				false);
	}

	@Test
	public void getDataSourcesTestHtml() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/linked-data?f=html",
				HttpStatus.OK.value(),
				null,
				null,
				null,
				null,
				false,
				false);
		testGenericHtml(actualbody, "/linked-data");
	}



	//Features Testing
	@Test
	public void getFeaturesTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp?f=json",
				HttpStatus.OK.value(),
				null,
				null,
			 	BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "wqpFeatureCollection.json"),
				true,
				false);
	}

	@Test
	public void getFeaturesTestGoodHtml() throws Exception {
		String url = "/linked-data/wqp";
		String actualbody = assertEntity(restTemplate,
				url + "?f=html",
				HttpStatus.OK.value(),
				null,
				null,
				null,
				null,
				false,
				false);

		testGenericHtml(actualbody, url);
	}

	@Test
	public void getFeaturesTestInvalid() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp?f=badformat",
				HttpStatus.NOT_ACCEPTABLE.value(),
				null,
				null,
				null,
				null,
				false,
				false);
	}

	//Object Testing Catchment
	@Test
	public void getComidTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246",
				HttpStatus.OK.value(),
				null,
				null,
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13297246.json"),
				true,
				false);

	}

	@Test
	public void getComidTestGoodHtml() throws Exception {
		String url="/linked-data/comid/13297246";
		String actualbody = assertEntity(restTemplate,
				url + "?f=html",
				HttpStatus.OK.value(),
				null,
				null,
				null,
				null,
				false,
				false);

		testGenericHtml(actualbody, url);
	}

	private void testGenericHtml(String actualbody, String url) {
		assertThat("contains <html> tag", actualbody.startsWith("<html>"));
		assertThat("contains </html> tag", actualbody.endsWith("</html>"));
		assertThat("opens link", actualbody.contains("<a"));
		assertThat("actual url",actualbody.contains(url + "?f=json"));
		assertThat("closes link", actualbody.contains("</a>"));
		assertThat("contains format", actualbody.contains("f=json"));
		assertThat("contains linked-data", actualbody.contains("/linked-data"));
	}


	@Test
	public void getComidTestInvalid() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/comid/13297246?f=badformat",
				HttpStatus.NOT_ACCEPTABLE.value(),
				null,
				null,
				null,
				null,
				false,
				false);
	}

	//Linked Object Testing WQP
	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880",
				HttpStatus.OK.value(),
				null,
				null,
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER_WQP, "wqp_USGS-05427880.json"),
				true,
				false);
	}

	public void getWqpTestInvalid() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880?f=badformat",
				HttpStatus.NOT_ACCEPTABLE.value(),
				null,
				null,
				null,
				null,
				false,
				false);
	}

	public void getWqpTestHtml() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880?f=html",
				HttpStatus.NOT_ACCEPTABLE.value(),
				null,
				null,
				null,
				null,
				false,
				false);
		testGenericHtml(actualbody, "/linked-data/wqp/USGS-05427880");
	}



	//Linked Object Testing huc12pp
	@Test
	@DatabaseSetup("classpath:/testData/featureHuc12pp.xml")
	public void gethuc12ppTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/huc12pp/070900020604",
				HttpStatus.OK.value(),
				null,
				null,
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER_HUC, "huc12pp_070900020604.json"),
				true,
				false);
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureHuc12pp.xml")
	public void gethuc12ppTestInvalid() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/huc12pp/070900020604?f=badformat",
				HttpStatus.NOT_ACCEPTABLE.value(),
				null,
				null,
				null,
				null,
				false,
				false);
	}


	@Test
	@DatabaseSetup("classpath:/testData/featureHuc12pp.xml")
	public void gethuc12ppTestHtml() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/linked-data/huc12pp/070900020604?f=html",
				HttpStatus.OK.value(),
				null,
				null,
				null,
				null,
				false,
				false);
		testGenericHtml(actualbody, "/linked-data/huc12pp/070900020604");
	}


	//Navigation Types Testing
	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getNavigationTypesTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880/navigate",
				HttpStatus.OK.value(),
				null,
				null,
				MediaType.APPLICATION_JSON_UTF8_VALUE,
				getCompareFile(RESULT_FOLDER, "wqp_USGS-05427880.json"),
				true,
				false);
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getNavigationTypesTestInvalid() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880/navigate?f=badformat",
				HttpStatus.NOT_ACCEPTABLE.value(),
				null,
				null,
				null,
				null,
				false,
				false);
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getNavigationTypesTestHtml() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/linked-data/wqp/USGS-05427880/navigate?f=html",
				HttpStatus.OK.value(),
				null,
				null,
				null,
				null,
				false,
				false);
		testGenericHtml(actualbody, "/linked-data/wqp/USGS-05427880/navigate");
	}

	@Test
	public void getNavigationTypesNotFoundTest() throws Exception {
		assertEntity(restTemplate,
				"/linked-data/wqx/USGS-05427880/navigate",
				HttpStatus.NOT_FOUND.value(),
				null,
				null,
				MediaType.APPLICATION_JSON_UTF8_VALUE,
				null,
				true,
				false);

		assertEntity(restTemplate,
				"/linked-data/wqp/USGX-05427880/navigate",
				HttpStatus.NOT_FOUND.value(),
				null,
				null,
				MediaType.APPLICATION_JSON_UTF8_VALUE,
				null,
				true,
				false);
	}
}
