package gov.usgs.owi.nldi.transform;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.usgs.owi.nldi.dao.BaseDao;
import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.NavigationDao;
import gov.usgs.owi.nldi.services.ConfigurationService;

public class FeatureTransformer extends MapToGeoJsonTransformer {

	public static final String FEATURE_COUNT_HEADER = BaseDao.FEATURES + COUNT_SUFFIX;
	public static final String DEFAULT_ENCODING = "UTF-8";

	protected static final String COMID = "comid";
	protected static final String IDENTIFIER = "identifier";
	protected static final String MEASURE = "measure";
	protected static final String NAME = "name";
	protected static final String REACHCODE = "reachcode";
	protected static final String SOURCE_NAME_DB = "source_name";
	protected static final String URI = "uri";

	private static final String SOURCE_NAME = "sourceName";
	private static final String NAVIGATION = "navigation";

	private final ConfigurationService configurationService;

	public FeatureTransformer(HttpServletResponse response, ConfigurationService configurationService) {
		super(response, FEATURE_COUNT_HEADER);
		this.configurationService = configurationService;
	}

	@Override
	protected void writeProperties(JsonGenerator jsonGenerator, Map<String, Object> resultMap) {
		try {
			String source = getValue(resultMap, LookupDao.SOURCE);
			String identifier = getValue(resultMap, IDENTIFIER);
			jsonGenerator.writeStringField(LookupDao.SOURCE, source);
			jsonGenerator.writeStringField(SOURCE_NAME, getValue(resultMap, SOURCE_NAME_DB));
			jsonGenerator.writeStringField(IDENTIFIER, identifier);
			jsonGenerator.writeStringField(NAME, getValue(resultMap, NAME));
			jsonGenerator.writeStringField(URI, getValue(resultMap, URI));
			jsonGenerator.writeStringField(COMID, getValue(resultMap, COMID));
			if (StringUtils.hasText(getValue(resultMap, REACHCODE))) {
				jsonGenerator.writeStringField(REACHCODE, getValue(resultMap, REACHCODE));
			}
			if (StringUtils.hasText(getValue(resultMap, MEASURE))) {
				jsonGenerator.writeStringField(MEASURE, getValue(resultMap, MEASURE));
			}
			jsonGenerator.writeStringField(NAVIGATION, String.join("/", configurationService.getLinkedDataUrl(), source.toLowerCase(), URLEncoder.encode(identifier, DEFAULT_ENCODING), NavigationDao.NAVIGATE));
		} catch (IOException e) {
			throw new RuntimeException("Error writing properties", e);
		}
	}

}
