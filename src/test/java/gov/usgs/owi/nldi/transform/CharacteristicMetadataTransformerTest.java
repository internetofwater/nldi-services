package gov.usgs.owi.nldi.transform;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CharacteristicMetadataTransformerTest {

	private static final String INIT_JSON = "{\"characteristicMetadata\":[";
	private static final String WRITE_MAP = "{\"characteristic\":{\"characteristic_id\":\"charID\",\"characteristic_description\":\"descript\","
			+ "\"units\":\"uni\",\"dataset_label\":\"lapel\",\"dataset_url\":\"uri\",\"theme_label\":\"latel\",\"theme_url\":\"uril\",\"characteristic_type\":\"typ\"}}";
	protected MockHttpServletResponse response;
	protected CharacteristicMetadataTransformer transformer;

	@BeforeEach
	public void beforeTest() throws IOException {
		response = new MockHttpServletResponse();
		transformer = new CharacteristicMetadataTransformer(response);
	}

	@AfterEach
	public void afterTest() throws Exception {
		transformer.close();
	}

	@Test
	public void initJsonTest() {
		transformer.initJson(transformer.g, null);
		try {
			transformer.g.flush();
			assertEquals(INIT_JSON, response.getContentAsString());
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void writeMapTest() {
		transformer.writeMap(transformer.g, buildMap());
		try {
			transformer.g.flush();
			assertEquals(WRITE_MAP, response.getContentAsString());
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
	}

	public Map<String, Object> buildMap() {
		Map<String, Object> map = new HashMap<>();
		map.put(CharacteristicMetadataTransformer.CHARACTERISTIC_ID, "charID");
		map.put(CharacteristicMetadataTransformer.CHARACTERISTIC_DESCRIPTION, "descript");
		map.put(CharacteristicMetadataTransformer.UNIT, "uni");
		map.put(CharacteristicMetadataTransformer.DATASET_LABEL, "lapel");
		map.put(CharacteristicMetadataTransformer.DATASET_URL, "uri");
		map.put(CharacteristicMetadataTransformer.THEME_LABEL, "latel");
		map.put(CharacteristicMetadataTransformer.THEME_URL, "uril");
		map.put(CharacteristicMetadataTransformer.CHARACTERISTIC_TYPE, "typ");
		return map;
	}
}
