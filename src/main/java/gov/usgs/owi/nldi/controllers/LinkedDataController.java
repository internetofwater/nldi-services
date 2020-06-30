package gov.usgs.owi.nldi.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiParam;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.usgs.owi.nldi.NavigationMode;
import gov.usgs.owi.nldi.dao.BaseDao;
import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.NavigationDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Navigation;
import gov.usgs.owi.nldi.services.Parameters;
import gov.usgs.owi.nldi.swagger.model.DataSource;
import gov.usgs.owi.nldi.transform.CharacteristicDataTransformer;
import gov.usgs.owi.nldi.transform.FeatureTransformer;
import gov.usgs.owi.nldi.transform.FeatureCollectionTransformer;
import io.swagger.annotations.ApiOperation;

@RestController
public class LinkedDataController extends BaseController {

	private static final String DOWNSTREAM_DIVERSIONS = "downstreamDiversions";
	private static final String DOWNSTREAM_MAIN = "downstreamMain";
	private static final String UPSTREAM_MAIN = "upstreamMain";
	private static final String UPSTREAM_TRIBUTARIES = "upstreamTributaries";

	@Autowired
	public LinkedDataController(LookupDao inLookupDao, StreamingDao inStreamingDao,
			Navigation inNavigation, Parameters inParameters, ConfigurationService configurationService,
			LogService inLogService) {
		super(inLookupDao, inStreamingDao, inNavigation, inParameters, configurationService, inLogService);
	}

	@ApiOperation(value="getDataSources", response=DataSource.class, responseContainer="List")
	@GetMapping(value="linked-data", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, Object>> getDataSources(HttpServletRequest request, HttpServletResponse response) {
		BigInteger logId = logService.logRequest(request);
		List<Map<String, Object>> rtn = new ArrayList<>();
		try {
			Map<String, Object> featureSource = new LinkedHashMap<>();

			//Manually add comid as a feature source.
			featureSource.put(LookupDao.SOURCE, Parameters.COMID);
			featureSource.put(LookupDao.SOURCE_NAME, "NHDPlus comid");
			featureSource.put(BaseDao.FEATURES, String.join("/", configurationService.getRootUrl(), Parameters.COMID));
			rtn.add(featureSource);

			Map<String, Object> parameterMap = new HashMap<>();
			parameterMap.put(LookupDao.ROOT_URL, configurationService.getRootUrl());
			rtn.addAll(lookupDao.getList(BaseDao.DATA_SOURCES, parameterMap));

		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
		return rtn;
	}

	@GetMapping(value="linked-data/{featureSource}", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getFeatures(HttpServletRequest request, HttpServletResponse response,
							@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource) {
		BigInteger logId = logService.logRequest(request);

		try {
			Map<String, Object> parameterMap = new HashMap<>();

			parameterMap.put(LookupDao.ROOT_URL, configurationService.getRootUrl());
			parameterMap.put(LookupDao.FEATURE_SOURCE, featureSource);
			FeatureCollectionTransformer transformer = new FeatureCollectionTransformer(response, configurationService);
			addContentHeader(response);
			streamResults(transformer, BaseDao.FEATURES_COLLECTION, parameterMap);

		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	@GetMapping(value="linked-data/{featureSource}/{featureID}", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getRegisteredFeature(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID) throws Exception {
		BigInteger logId = logService.logRequest(request);
		try (FeatureTransformer transformer = new FeatureTransformer(response, configurationService)) {
			Map<String, Object> parameterMap = new HashMap<> ();
			parameterMap.put(LookupDao.FEATURE_SOURCE, featureSource);
			parameterMap.put(Parameters.FEATURE_ID, featureID);
			addContentHeader(response);
			streamResults(transformer, BaseDao.FEATURE, parameterMap);
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	@GetMapping(value="linked-data/{featureSource}/{featureID}/navigate", produces=MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> getNavigationTypes(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID) throws UnsupportedEncodingException {
		BigInteger logId = logService.logRequest(request);
		Map<String, Object> rtn = new LinkedHashMap<>();
		try {
			//Verify that the feature source and identifier are valid
			Map<String, Object> parameterMap = new HashMap<>();
			parameterMap.put(LookupDao.FEATURE_SOURCE, featureSource);
			parameterMap.put(Parameters.FEATURE_ID, featureID);
			List<Map<String, Object>> results = lookupDao.getList(BaseDao.FEATURE, parameterMap);

			if (null == results || results.isEmpty()) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			} else {
				rtn.put(UPSTREAM_MAIN, 
						String.join("/", configurationService.getRootUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.UM.toString()));
				rtn.put(UPSTREAM_TRIBUTARIES, 
						String.join("/", configurationService.getRootUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.UT.toString()));
				rtn.put(DOWNSTREAM_MAIN, 
						String.join("/", configurationService.getRootUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.DM.toString()));
				rtn.put(DOWNSTREAM_DIVERSIONS, 
						String.join("/", configurationService.getRootUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.DD.toString()));
			}

		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
		return rtn;
	}

	@GetMapping(value="linked-data/{featureSource}/{featureID}/{characteristicType}")
	public void getCharacteristicData(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID,
			@PathVariable(Parameters.CHARACTERISTIC_TYPE) String characteristicType,
			@RequestParam(value=Parameters.CHARACTERISTIC_ID, required=false) String[] characteristicIds) throws IOException {
		BigInteger logId = logService.logRequest(request);
		try (CharacteristicDataTransformer transformer = new CharacteristicDataTransformer(response)) {
			String comid = getComid(featureSource, featureID);
			if (null == comid) {
				response.sendError(HttpStatus.NOT_FOUND.value(), "Feature not found");
			}
			Map<String, Object> parameterMap = new HashMap<> ();
			parameterMap.put(Parameters.CHARACTERISTIC_TYPE, characteristicType.toLowerCase());
			parameterMap.put(Parameters.COMID, NumberUtils.parseNumber(comid, Integer.class));
			parameterMap.put(Parameters.CHARACTERISTIC_ID, characteristicIds);
			addContentHeader(response);
			streamResults(transformer, BaseDao.CHARACTERISTIC_DATA, parameterMap);
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	@GetMapping(value="linked-data/{featureSource}/{featureID}/basin")
	public void getBasin(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID) throws Exception {

		BigInteger logId = logService.logRequest(request);

		try {
			String comid = getComid(featureSource, featureID);

			if (null != comid) {
				streamBasin(response, comid);
			} else {
				response.sendError(HttpStatus.NOT_FOUND.value(), "Basin not found");
			}
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	@GetMapping(value="linked-data/{featureSource}/{featureID}/navigate/{navigationMode}", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getFlowlines(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID,
			@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
			@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
			@ApiParam(value=Parameters.DISTANCE_DESCRIPTION)
								 @RequestParam(value=Parameters.DISTANCE, required=false)
								 @Range(min=1, max=9999, message="distance must be between 1 and 9999 kilometers") String distance,
			@RequestParam(value=Parameters.LEGACY, required=false) String legacy) {

		BigInteger logId = logService.logRequest(request);
		try {
			String comid = getComid(featureSource, featureID);
			if (null == comid) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			} else {
				streamFlowLines(response, comid, navigationMode, stopComid, distance, isLegacy(legacy, navigationMode));
			}
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	@GetMapping(value="linked-data/{featureSource}/{featureID}/navigate/{navigationMode}/{dataSource}", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getFeatures(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID,
			@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
			@PathVariable(value=DATA_SOURCE) String dataSource,
			@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
			@ApiParam(value=Parameters.DISTANCE_DESCRIPTION)
								@RequestParam(value=Parameters.DISTANCE, required=false)
								@Range(min=1, max=9999, message="distance must be between 1 and 9999 kilometers") String distance,
			@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {

		BigInteger logId = logService.logRequest(request);
		try {
			String comid = getComid(featureSource, featureID);
			if (null == comid) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			} else {
				streamFeatures(response, comid, navigationMode, stopComid, distance, dataSource, isLegacy(legacy, navigationMode));
			}
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}
}
