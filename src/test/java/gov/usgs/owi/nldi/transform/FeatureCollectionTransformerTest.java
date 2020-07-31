package gov.usgs.owi.nldi.transform;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.usgs.owi.nldi.services.Parameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import gov.usgs.owi.nldi.services.TestConfigurationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class FeatureCollectionTransformerTest {

	protected TestConfigurationService configurationService;
	protected FeatureCollectionTransformer transformer;
	protected MockHttpServletResponse response;

	@BeforeEach
	public void beforeTest() throws IOException {
		configurationService = new TestConfigurationService();
		response = new MockHttpServletResponse();
		transformer = new FeatureCollectionTransformer(response, configurationService);
	}

	@AfterEach
	public void afterTest() throws Exception {
		transformer.close();
	}

	@Test
	public void writeFeatureCollectionTest() {
		Map<String, Object> map = new HashMap<>();
		map.put("type", "Feature Collection");


		try {
			transformer.startCollection(map);
			transformer.writeFeature(goodFeature());
			transformer.endCollection();
			//need to flush the JsonGenerator to get at output. 
			transformer.g.flush();
			assertEquals("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{},\"properties\":{\"source\":\"\",\"sourceName\":\"\",\"identifier\":\"\",\"name\":\"\",\"uri\":\"\",\"comid\":\"12345\",\"navigation\":\"http://owi-test.usgs.gov:8080/test-url/linked-data///navigation\"}}]}",
				response.getContentAsString());
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
	}

	public static Map<String, Object> goodFeature() {
		Map<String, Object> rtn = new LinkedHashMap<>();
		rtn.put(Parameters.COMID, "12345");
		return rtn;
	}
}
