package gov.usgs.owi.nldi.transform;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.usgs.owi.nldi.dao.BaseDao;

public class FlowLineTransformer extends MapToGeoJsonTransformer {

	public static final String FLOW_LINES_COUNT_HEADER = BaseDao.FLOW_LINES + COUNT_SUFFIX;

	protected static final String NHDPLUS_COMID = "nhdplus_comid";

	public FlowLineTransformer(HttpServletResponse response) {
		super(response, FLOW_LINES_COUNT_HEADER);
	}

	@Override
	protected void writeProperties(JsonGenerator jsonGenerator, Map<String, Object> resultMap) {
		try {
			jsonGenerator.writeStringField(NHDPLUS_COMID, getValue(resultMap, NHDPLUS_COMID));
		} catch (IOException e) {
			throw new RuntimeException("Error writing properties", e);
		}
	}

}
