package gov.usgs.owi.nldi.transform;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.core.JsonGenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


public class MapToGeoJsonTransformerTest {

	private static final String TEST_COUNT_HEADER = "abc";
	private static final String TOTAL_ROW_COUNT = "123456";
	private static final String INITIAL_JSON = "{\"type\":\"FeatureCollection\",\"features\":[";
	private static final String ITERATIVE_JSON = "{\"type\":\"Feature\",\"geometry\":{"
			+ "\"type\":\"LineString\",\"coordinates\":[[-89.2572407051921, 43.2039759978652],[-89.2587703019381, 43.204960398376]]"
			+ "},\"properties\":";

	private TestTransformer transformer;
	private MockHttpServletResponse response;

	private class TestTransformer extends MapToGeoJsonTransformer {
		public int writePropertiesCalled = 0;

		public TestTransformer(HttpServletResponse response) throws IOException {
			super(response, TEST_COUNT_HEADER);
		}

		@Override
		protected void writeProperties(JsonGenerator jsonGenerator, Map<String, Object> resultMap) {
			writePropertiesCalled = writePropertiesCalled + 1;
			try {
				jsonGenerator.writeStringField("prop" + writePropertiesCalled, "propValue" + writePropertiesCalled);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@BeforeEach
	public void beforeTest() throws IOException {
		response = new MockHttpServletResponse();
		transformer = new TestTransformer(response);
	}

	@AfterEach
	public void afterTest() throws Exception {
		transformer.close();
	}

	@Test
	public void writeTest() {
		//Don't process null results
		transformer.write((Object) null);

		//Don't process results that aren't a map
		transformer.write((Object) "ABCDEFG");

		Map<String, Object> map = new HashMap<>();
		map.put(MapToGeoJsonTransformer.TOTAL_ROWS, TOTAL_ROW_COUNT);
		map.put(MapToGeoJsonTransformer.SHAPE, "{\"type\":\"LineString\",\"coordinates\":[[-89.2572407051921, 43.2039759978652],[-89.2587703019381, 43.204960398376]]}");
		map.put("A", "1");
		map.put("B", "2");

		transformer.write((Object) map);
		assertEquals(1, transformer.writePropertiesCalled);

		// headers should be added after first call
		assertTrue(response.containsHeader(TEST_COUNT_HEADER));
		assertEquals(TOTAL_ROW_COUNT, response.getHeaderValue(TEST_COUNT_HEADER));

		// initial json should be set after first call, along with first property
		String firstWrite = INITIAL_JSON + ITERATIVE_JSON + "{\"prop1\":\"propValue1\"}}";
		try {
			transformer.g.flush();
			assertEquals(firstWrite, response.getContentAsString());
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}

		transformer.write((Object) map);
		assertEquals(2, transformer.writePropertiesCalled);

		String secondWrite = firstWrite + "," + ITERATIVE_JSON + "{\"prop2\":\"propValue2\"}}";
		try {
			transformer.g.flush();
			assertEquals(secondWrite, response.getContentAsString());
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}

		transformer.end();

		try {
			assertEquals(secondWrite + "]}", response.getContentAsString());
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
	}
}
