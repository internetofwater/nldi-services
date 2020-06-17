package gov.usgs.owi.nldi.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;
import gov.usgs.owi.nldi.NavigationMode;
import gov.usgs.owi.nldi.dao.BaseDao;
import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.dao.StreamingResultHandler;
import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Navigation;
import gov.usgs.owi.nldi.services.Parameters;
import gov.usgs.owi.nldi.transform.BasinTransformer;
import gov.usgs.owi.nldi.transform.FeatureTransformer;
import gov.usgs.owi.nldi.transform.FlowLineTransformer;
import gov.usgs.owi.nldi.transform.ITransformer;

@Validated
public abstract class BaseController {
	private static final Logger LOG = LoggerFactory.getLogger(BaseController.class);

	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String MIME_TYPE_GEOJSON = "application/vnd.geo+json";
	public static final String REGEX_NAVIGATION_MODE = "DD|DM|PP|UT|UM";

	static final String DATA_SOURCE = "dataSource";

	protected final LookupDao lookupDao;
	protected final StreamingDao streamingDao;
	protected final Navigation navigation;
	protected final Parameters parameters;
	protected final ConfigurationService configurationService;
	protected final LogService logService;

	private final KeyLockManager lockManager = KeyLockManagers.newLock();

	public BaseController(LookupDao inLookupDao, StreamingDao inStreamingDao, Navigation inNavigation, Parameters inParameters, ConfigurationService inConfigurationService, LogService inLogService) {
		lookupDao = inLookupDao;
		streamingDao = inStreamingDao;
		navigation = inNavigation;
		parameters = inParameters;
		configurationService = inConfigurationService;
		logService = inLogService;
	}

	protected void streamFlowLines(HttpServletResponse response,
			String comid, String navigationMode, String stopComid, String distance, boolean legacy) throws Exception {
		Map<String, Object> parameterMap = parameters.processParameters(comid, navigationMode, distance, stopComid);
		FlowLineTransformer transformer = new FlowLineTransformer(response);
		if (legacy) {
			String sessionId = getSessionId(parameterMap, response);
			if (null != sessionId) {
				parameterMap.put(StreamingDao.SESSION_ID, sessionId);
				addContentHeader(response);
				streamResults(transformer, BaseDao.FLOW_LINES_LEGACY, parameterMap);
			}
		} else {
			addContentHeader(response);
			streamResults(transformer, BaseDao.FLOW_LINES, parameterMap);
		}
	}

	protected void streamFeatures(HttpServletResponse response,
			String comid, String navigationMode, String stopComid, String distance, String dataSource, boolean legacy) throws Exception {
		Map<String, Object> parameterMap = parameters.processParameters(comid, navigationMode, distance, stopComid);
		FeatureTransformer transformer = new FeatureTransformer(response, configurationService);
		if (legacy) {
			String sessionId = getSessionId(parameterMap, response);
			if (null != sessionId) {
				parameterMap.put(StreamingDao.SESSION_ID, sessionId);
				parameterMap.put(DATA_SOURCE, dataSource.toLowerCase());
				addContentHeader(response);
				streamResults(transformer, BaseDao.FEATURES_LEGACY, parameterMap);
			}
		} else {
			parameterMap.put(DATA_SOURCE, dataSource.toLowerCase());
			addContentHeader(response);
			streamResults(transformer, BaseDao.FEATURES, parameterMap);
		}
	}

	protected void streamBasin(HttpServletResponse response, String comid) throws Exception {
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put(Parameters.COMID, NumberUtils.parseNumber(comid, Integer.class));
		BasinTransformer transformer = new BasinTransformer(response);
		addContentHeader(response);
		streamResults(transformer, BaseDao.BASIN, parameterMap);
	}

	protected void streamResults(ITransformer transformer, String featureType, Map<String, Object> parameterMap) {
		LOG.trace("start streaming");
		ResultHandler<?> handler = new StreamingResultHandler(transformer);
		streamingDao.stream(featureType, parameterMap, handler);
		transformer.end();
		LOG.trace("done streaming");
	}

	protected void addContentHeader(HttpServletResponse response) {
		response.setHeader(HEADER_CONTENT_TYPE, MIME_TYPE_GEOJSON);
	}

	protected String getSessionId(Map<String, Object> parameterMap, HttpServletResponse response) throws Exception {
		// No NPE testing - should never get here without parameters.
		int key = parameterMap.hashCode();
		Map<String, String> navigationResult = lockManager.executeLocked(key, () -> navigation.navigate(parameterMap));
		return navigation.interpretResult(navigationResult, response);
	}

	protected boolean isLegacy(String legacy, String navigationMode) {
		return (StringUtils.hasText(legacy) && "true".contentEquals(legacy.trim().toLowerCase()))
				|| NavigationMode.PP.toString().equalsIgnoreCase(navigationMode);
	}

	protected String getComid(String featureSource, String featureID) {
		Map<String, Object> parameterMap = new HashMap<> ();
		parameterMap.put(LookupDao.FEATURE_SOURCE, featureSource);
		parameterMap.put(Parameters.FEATURE_ID, featureID);

		Map<String, Object> feature = lookupDao.getComid(BaseDao.FEATURE, parameterMap);
		if (null == feature || !feature.containsKey(Parameters.COMID)) {
			return null;
		} else {
			return feature.get(Parameters.COMID).toString();
		}
	}
}
