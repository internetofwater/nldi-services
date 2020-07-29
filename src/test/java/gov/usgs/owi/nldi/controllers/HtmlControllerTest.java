package gov.usgs.owi.nldi.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

public class HtmlControllerTest {



	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);

	}

	@Test
	public void ensureIsHttpsTest() throws Exception {
		HtmlController controller = new HtmlController();
		StringBuffer url = new StringBuffer("http://labs-dev.wma.chs.usgs.gov/api/nldi/linked-data/nwissite?f=json");
		String link = controller.ensureIsHttps(url);
		assertEquals("https://labs-dev.wma.chs.usgs.gov/api/nldi/linked-data/nwissite?f=json", link);

		url = new StringBuffer("https://labs-dev.wma.chs.usgs.gov/api/nldi/linked-data/nwissite?f=json");
		link = controller.ensureIsHttps(url);
		assertEquals("https://labs-dev.wma.chs.usgs.gov/api/nldi/linked-data/nwissite?f=json", link);

		url = new StringBuffer("http://localhost:8080/api/nldi/linked-data/nwissite?f=json");
		link = controller.ensureIsHttps(url);
		assertEquals("http://localhost:8080/api/nldi/linked-data/nwissite?f=json", link);

	}
}
