package gov.usgs.owi.nldi.transform;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import gov.usgs.owi.nldi.services.ConfigurationService;

public class FeatureCollectionTransformer extends FeatureTransformer {

	public FeatureCollectionTransformer(HttpServletResponse response, ConfigurationService configurationService) {
		super(response, configurationService);
	}

	public void startCollection(Map<String, Object> resultMap) {
		super.initJson(g, resultMap);
	}

	public void endCollection() throws IOException {
		g.writeEndArray();
		g.writeEndObject();
	}

	public void writeFeature(Map<String, Object> resultMap) {
		super.writeMap(g, resultMap);
	}

}
