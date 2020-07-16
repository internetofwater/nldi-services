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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class MapToJsonTransformerTest {

	private static final String INITIAL_JSON = "{\"baz\":[";
	private static final String TEST_HEADER_NAME = "thName";
	private static final String TEST_HEADER_VALUE = "thValue";

	protected TestTransformer testTransformer;
	protected MockHttpServletResponse response;

	private class TestTransformer extends MapToJsonTransformer {
		public int addResponseHeadersCalledCount = 0;
		public int initJsonCalledCount = 0;
		public int writeMapCalledCount = 0;

		public TestTransformer(HttpServletResponse response) throws IOException {
			super(response);
		}

		void assertMethodCallCounts(int addResponseCount, int initJsonCount, int writeMapCount) {
			assertEquals(addResponseCount, addResponseHeadersCalledCount);
			assertEquals(initJsonCount, initJsonCalledCount);
			assertEquals(writeMapCount, writeMapCalledCount);
		}

		@Override
		void addResponseHeaders(HttpServletResponse response, Map<String, Object> resultMap) {
			addResponseHeadersCalledCount = addResponseHeadersCalledCount + 1;
			response.addHeader(TEST_HEADER_NAME, TEST_HEADER_VALUE);
		}

		@Override
		void initJson(JsonGenerator jsonGenerator, Map<String, Object> resultMap) {
			initJsonCalledCount = initJsonCalledCount + 1;
			try {
				jsonGenerator.writeStartObject();
				jsonGenerator.writeFieldName("baz"); 
				jsonGenerator.writeStartArray();
			} catch (IOException e) {
				fail(e.getLocalizedMessage());
			}
		}

		@Override
		void writeMap(JsonGenerator jsonGenerator, Map<String, Object> resultMap) {
			writeMapCalledCount = writeMapCalledCount + 1;
			try {
				jsonGenerator.writeStartObject();
				jsonGenerator.writeStringField("prop" + writeMapCalledCount, "propValue" + writeMapCalledCount);
				jsonGenerator.writeEndObject();
			} catch (IOException e) {
				fail(e.getLocalizedMessage());
			}
		}
	}

	@BeforeEach
	public void beforeTest() throws IOException {
		response = new MockHttpServletResponse();
		testTransformer = new TestTransformer(response);
	}

	@AfterEach
	public void afterTest() throws Exception {
		testTransformer.close();
	}

	@Test
	public void writeTest() {
		//Don't process null results
		testTransformer.write((Object) null);
		testTransformer.assertMethodCallCounts(0, 0, 0);

		//Don't process results that aren't a map
		testTransformer.write((Object) "ABCDEFG");
		testTransformer.assertMethodCallCounts(0, 0, 0);

		Map<String, Object> result = new HashMap<>();
		result.put("A", "1");
		result.put("B", "2");

		testTransformer.write((Object) result);
		testTransformer.assertMethodCallCounts(1, 1, 1);

		// headers should be added after first call
		assertTrue(response.containsHeader(TEST_HEADER_NAME));
		assertEquals(TEST_HEADER_VALUE, response.getHeaderValue(TEST_HEADER_NAME));

		// initial json should be set after first call, along with first property
		try {
			testTransformer.g.flush();
			assertEquals(INITIAL_JSON + "{\"prop1\":\"propValue1\"}", response.getContentAsString());
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}

		testTransformer.write((Object) result);
		testTransformer.assertMethodCallCounts(1, 1, 2);

		try {
			testTransformer.g.flush();
			assertEquals(INITIAL_JSON + "{\"prop1\":\"propValue1\"},{\"prop2\":\"propValue2\"}", response.getContentAsString());
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}

		testTransformer.end();

		try {
			assertEquals(INITIAL_JSON + "{\"prop1\":\"propValue1\"},{\"prop2\":\"propValue2\"}]}", response.getContentAsString());
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void getValueTest() {
		Map<String, Object> map = new HashMap<>();
		map.put("NotNull", "abc/");
		map.put("Null", null);
		assertEquals("abc/", testTransformer.getValue(map, "NotNull"));
		assertEquals("", testTransformer.getValue(map, "Null"));
		assertEquals("", testTransformer.getValue(map, "NoWay"));
	}
}
