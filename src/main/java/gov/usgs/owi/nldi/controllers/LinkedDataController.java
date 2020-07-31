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

import gov.usgs.owi.nldi.NavigationMode;
import gov.usgs.owi.nldi.dao.NavigationDao;
import gov.usgs.owi.nldi.transform.CharacteristicDataTransformer;
import gov.usgs.owi.nldi.transform.FeatureCollectionTransformer;
import gov.usgs.owi.nldi.transform.FeatureTransformer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.usgs.owi.nldi.dao.BaseDao;
import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Navigation;
import gov.usgs.owi.nldi.services.Parameters;
import gov.usgs.owi.nldi.swagger.model.DataSource;
import gov.usgs.owi.nldi.swagger.model.Feature;

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

	//swagger documentation for /linked-data endpoint
	@Operation(summary = "getDataSources", description = "returns a list of data sources")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK", content = { @Content(mediaType = "application/json",
			schema = @Schema(implementation = DataSource.class)) }),
		@ApiResponse(responseCode = "500", description = "Server error", content = @Content)
	})
	@GetMapping(value="linked-data", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, Object>> getDataSources(HttpServletRequest request, HttpServletResponse response) {
		BigInteger logId = logService.logRequest(request);
		List<Map<String, Object>> rtn = new ArrayList<>();
		try {
			Map<String, Object> featureSource = new LinkedHashMap<>();

			//Manually add comid as a feature source.
			featureSource.put(LookupDao.SOURCE, Parameters.COMID);
			featureSource.put(LookupDao.SOURCE_NAME, "NHDPlus comid");
			featureSource.put(BaseDao.FEATURES, String.join("/", configurationService.getLinkedDataUrl(), Parameters.COMID));
			rtn.add(featureSource);

			Map<String, Object> parameterMap = new HashMap<>();
			parameterMap.put(LookupDao.ROOT_URL, configurationService.getLinkedDataUrl());
			rtn.addAll(lookupDao.getList(BaseDao.DATA_SOURCES, parameterMap));

		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
		return rtn;
	}


	//swagger documentation for /linked-data/{featureSource} endpoint
	@Operation(summary = "getFeatures", description = "returns a list of features for a given data source")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK", content = { @Content(mediaType = "application/json",
			schema = @Schema(implementation = Feature.class)) }),
		@ApiResponse(responseCode = "500", description = "Server error", content = @Content) })
	@GetMapping(value="linked-data/{featureSource}", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getFeatures(
		HttpServletRequest request, HttpServletResponse response,
		@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource) throws Exception {
		BigInteger logId = logService.logRequest(request);
		try {

			Map<String, Object> parameterMap = new HashMap<>();
			parameterMap.put(LookupDao.ROOT_URL, configurationService.getLinkedDataUrl());
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

	//swagger documentation for /linked-data/{featureSource}/{featureID} endpoint
	@Operation(summary = "getRegisteredFeature", description = "returns registered feature as WGS84 lat/lon GeoJSON if it exists")
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

	//swagger documentation for /linked-data/{featureSource}/{featureID}/navigate endpoint
	@Operation(summary = "getNavigateTypes (deprecated)", description = "returns valid navigation end points")
	@GetMapping(value="linked-data/{featureSource}/{featureID}/navigate", produces=MediaType.APPLICATION_JSON_VALUE)
	@Deprecated
	public Map<String, Object> getNavigateTypes(HttpServletRequest request, HttpServletResponse response,
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
						String.join("/", configurationService.getLinkedDataUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.UM.toString()));
				rtn.put(UPSTREAM_TRIBUTARIES, 
						String.join("/", configurationService.getLinkedDataUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.UT.toString()));
				rtn.put(DOWNSTREAM_MAIN, 
						String.join("/", configurationService.getLinkedDataUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.DM.toString()));
				rtn.put(DOWNSTREAM_DIVERSIONS, 
						String.join("/", configurationService.getLinkedDataUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.DD.toString()));
			}

		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
		return rtn;
	}

	@Operation(summary = "getNavigationTypes", description = "returns valid navigation end points")
	@GetMapping(value="linked-data/{featureSource}/{featureID}/navigation", produces=MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> getNavigationTypes(
		HttpServletRequest request, HttpServletResponse response,
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
					String.join("/", configurationService.getLinkedDataUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATION, NavigationMode.UM.toString()));
				rtn.put(UPSTREAM_TRIBUTARIES,
					String.join("/", configurationService.getLinkedDataUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATION, NavigationMode.UT.toString()));
				rtn.put(DOWNSTREAM_MAIN,
					String.join("/", configurationService.getLinkedDataUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATION, NavigationMode.DM.toString()));
				rtn.put(DOWNSTREAM_DIVERSIONS,
					String.join("/", configurationService.getLinkedDataUrl(), featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATION, NavigationMode.DD.toString()));
			}

		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
		return rtn;
	}


	//swagger documentation for /linked-data/{featureSource}/{featureID}/{characteristicType} endpoint
	@Operation(summary = "getCharacteristicData", description = "returns all characteristics of the given type for the specified feature")
	@GetMapping(value="linked-data/{featureSource}/{featureID}/{characteristicType}", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getCharacteristicData(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID,
			@PathVariable(Parameters.CHARACTERISTIC_TYPE) String characteristicType,
			@RequestParam(value=Parameters.CHARACTERISTIC_ID, required=false) String[] characteristicIds) throws IOException {
		BigInteger logId = logService.logRequest(request);
		try (CharacteristicDataTransformer transformer = new CharacteristicDataTransformer(response)) {
      
			String comid = getComid(featureSource, featureID);
      
			if (null == comid) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			} else {
				Map<String, Object> parameterMap = new HashMap<> ();
				parameterMap.put(Parameters.CHARACTERISTIC_TYPE, characteristicType.toLowerCase());
				parameterMap.put(Parameters.COMID, NumberUtils.parseNumber(comid, Integer.class));
				parameterMap.put(Parameters.CHARACTERISTIC_ID, characteristicIds);
				addContentHeader(response);
				streamResults(transformer, BaseDao.CHARACTERISTIC_DATA, parameterMap);
			}

		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	//swagger documentation for /linked-data/{featureSource}/{featureID}/basin endpoint
	@Operation(summary = "getBasin", description = "returns the aggregated basin for the specified feature in WGS84 lat/lon GeoJSON")
	@GetMapping(value="linked-data/{featureSource}/{featureID}/basin", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getBasin(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID) throws Exception {

		BigInteger logId = logService.logRequest(request);

		try {
			String comid = getComid(featureSource, featureID);

			if (null == comid) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			} else {
				streamBasin(response, comid);
			}
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	//swagger documentation for /linked-data/{featureSource}/{featureID}/navigate/{navigationMode} endpoint
	@Operation(summary = "getFlowlines (deprecated)", description = "returns the flowlines for the specified navigation in WGS84 lat/lon GeoJSON")
	@GetMapping(value="linked-data/{featureSource}/{featureID}/navigate/{navigationMode}", produces=MediaType.APPLICATION_JSON_VALUE)
	@Deprecated
	public void getFlowlines(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID,
			@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
			@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
			@Parameter(description=Parameters.DISTANCE_DESCRIPTION)
				@RequestParam(value=Parameters.DISTANCE, required=false, defaultValue=Parameters.MAX_DISTANCE)
			@Pattern(message=Parameters.DISTANCE_VALIDATION_MESSAGE, regexp=Parameters.DISTANCE_VALIDATION_REGEX) String distance,
			@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {

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

	//swagger documentation for /linked-data/{featureSource}/{featureID}/navigate/{navigationMode}/{dataSource} endpoint
	@Operation(summary = "getFeatures (deprecated)", description = "Returns all features found along the specified navigation as points in WGS84 lat/lon GeoJSON")
	@GetMapping(value="linked-data/{featureSource}/{featureID}/navigate/{navigationMode}/{dataSource}", produces=MediaType.APPLICATION_JSON_VALUE)
	@Deprecated
	public void getFeaturesDeprecated(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID,
			@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
			@PathVariable(value=DATA_SOURCE) String dataSource,
			@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
			@Parameter(description=Parameters.DISTANCE_DESCRIPTION)
				@RequestParam(value=Parameters.DISTANCE, required=false, defaultValue=Parameters.MAX_DISTANCE)
			@Pattern(message=Parameters.DISTANCE_VALIDATION_MESSAGE, regexp=Parameters.DISTANCE_VALIDATION_REGEX) String distance,
			@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {

		BigInteger logId = logService.logRequest(request);
		try {
			String comid = getComid(featureSource, featureID);
			if (null == comid) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			} else {
				streamFeatures(response, comid, navigationMode, stopComid, distance, dataSource,
						isLegacy(legacy, navigationMode));
			}
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	//swagger documentation for /linked-data/{featureSource}/{featureID}/navigate/{navigationMode}/{dataSource} endpoint
	@Operation(summary = "getFeatures", description = "Returns all features found along the specified navigation as points in WGS84 lat/lon GeoJSON")
	@GetMapping(value="linked-data/{featureSource}/{featureID}/navigation/{navigationMode}/{dataSource}", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getFeatures(
		HttpServletRequest request, HttpServletResponse response,
		@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
		@PathVariable(Parameters.FEATURE_ID) String featureID,
		@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
		@PathVariable(value=DATA_SOURCE) String dataSource,
		@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
		@Parameter(description=Parameters.DISTANCE_DESCRIPTION)
		@RequestParam(value=Parameters.DISTANCE, required=false, defaultValue=Parameters.MAX_DISTANCE)
		@Pattern(message=Parameters.DISTANCE_VALIDATION_MESSAGE, regexp=Parameters.DISTANCE_VALIDATION_REGEX) String distance,
		@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {

		BigInteger logId = logService.logRequest(request);
		try {
			String comid = getComid(featureSource, featureID);
			if (null == comid) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			} else {
				streamFeatures(response, comid, navigationMode, stopComid, distance, dataSource,
					isLegacy(legacy, navigationMode));
			}
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}



	//swagger documentation for /linked-data/{featureSource}/{featureID}/navigation/{navigationMode} endpoint
	@Operation(summary = "getNavigation", description = "returns the navigation options")
	@GetMapping(value="linked-data/{featureSource}/{featureID}/navigation/{navigationMode}", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, Object>> getNavigation(
		HttpServletRequest request, HttpServletResponse response,
		@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
		@PathVariable(Parameters.FEATURE_ID) String featureID,
		@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode)  {

		BigInteger logId = logService.logRequest(request);

		try {
			List<Map<String, Object>> dataSources = getDataSources(request, response);
			List<Map<String, Object>> newDataSources = new ArrayList<>();
			String newNavigationUrl = createNewNavigationUrl(request);
			for (Map<String, Object> dataSource: dataSources) {
				if (Parameters.COMID.equals(dataSource.get(LookupDao.SOURCE))) {
					dataSource.put(LookupDao.SOURCE, "Flowlines");
					dataSource.put(LookupDao.SOURCE_NAME, "NHDPlus flowlines");
					dataSource.put(BaseDao.FEATURES, newNavigationUrl  + "flowlines");
					newDataSources.add(dataSource);
				} else {
					dataSource.put(BaseDao.FEATURES, newNavigationUrl
						+ dataSource.get(LookupDao.SOURCE).toString().toLowerCase());
					newDataSources.add(dataSource);
				}
			}
			return newDataSources;

		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
		return null;
	}

	//swagger documentation for /linked-data/{featureSource}/{featureID}/navigate/{navigationMode} endpoint
	@Operation(summary = "getFlowlines", description = "returns the flowlines for the specified navigation in WGS84 lat/lon GeoJSON")
	@GetMapping(value="linked-data/{featureSource}/{featureID}/navigation/{navigationMode}/flowlines", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getNavigationFlowlines(
		HttpServletRequest request, HttpServletResponse response,
		@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
		@PathVariable(Parameters.FEATURE_ID) String featureID,
		@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
		@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
		@Parameter(description=Parameters.DISTANCE_DESCRIPTION)
		@RequestParam(value=Parameters.DISTANCE, required=false, defaultValue=Parameters.MAX_DISTANCE)
		@Pattern(message=Parameters.DISTANCE_VALIDATION_MESSAGE, regexp=Parameters.DISTANCE_VALIDATION_REGEX) String distance,
		@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {

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


	// We need to create navigation urls for the various options (see test file navigation.json)
	// We do this by starting with the linked-data url from the configuration service
	// then adding all the request-specific elements from the request we received
	private String createNewNavigationUrl(HttpServletRequest request) {
		String newUrl = configurationService.getLinkedDataUrl();
		String requestUrl = request.getRequestURL().toString();
		String[] arr = requestUrl.split("linked-data");
		newUrl += arr[1];
		newUrl += "/";
		return newUrl;
	}


}
