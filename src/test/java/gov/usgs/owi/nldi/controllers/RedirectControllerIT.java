package gov.usgs.owi.nldi.controllers;

import org.junit.Before;
import org.junit.Test;
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
public class RedirectControllerIT extends BaseIT {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private RedirectController controller;

	private static final String RESULT_FOLDER  = "feature/other/";

	@Before
	public void setUp() {
		urlRoot = "http://localhost:" + port + context;
	}

	@Test
	public void getCharacteristicDataFilteredTest() throws Exception {
		// configurationService.getRootUrl() will always be set to owi-test.usgs.gov:8080
		// when the tests run.  This is hardcoded in the TestConfigurationService class
		// and many tests seem to rely on it.
		// But this test requires the root url to be set to what the test is running on,
		// http://localhost:<random port>
		controller.setRootUrl(restTemplate.getRootUri());
		assertEntity(restTemplate,
				"/swagger",
				HttpStatus.OK.value(),
				null,
				null,
				null,
				null,
				false,
				false);
	}

}
