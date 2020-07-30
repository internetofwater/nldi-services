package gov.usgs.owi.nldi.controllers;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;

import gov.usgs.owi.nldi.dao.BaseDao;
import gov.usgs.owi.nldi.swagger.model.Feature;
import gov.usgs.owi.nldi.transform.FeatureTransformer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Navigation;
import gov.usgs.owi.nldi.services.Parameters;

@RestController
public class NetworkController extends BaseController {

	@Autowired
	public NetworkController(LookupDao inLookupDao, StreamingDao inStreamingDao,
			Navigation inNavigation, Parameters inParameters, ConfigurationService configurationService,
			LogService inLogService) {
		super(inLookupDao, inStreamingDao, inNavigation, inParameters, configurationService, inLogService);
	}


	//swagger documentation for /linked-data/comid/navigation/{navigationMode} endpoint
	@Operation(summary = "getNavigation", description = "returns the navigation options")
	@GetMapping(value="linked-data/comid/navigation/{navigationMode}", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getNavigation(
		HttpServletRequest request, HttpServletResponse response,
		@PathVariable(Parameters.COMID) @Range(min=1, max=Integer.MAX_VALUE) String comid,
		@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
		@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
		@Parameter(description=Parameters.DISTANCE_DESCRIPTION)
		@RequestParam(value=Parameters.DISTANCE, required=false, defaultValue=Parameters.MAX_DISTANCE)
		@Pattern(message=Parameters.DISTANCE_VALIDATION_MESSAGE, regexp=Parameters.DISTANCE_VALIDATION_REGEX) String distance,
		@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {

		BigInteger logId = logService.logRequest(request);

		try {
			streamFlowLines(response, comid, navigationMode, stopComid, distance, isLegacy(legacy, navigationMode));

		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}


	//swagger documentation for /linked-data/{featureSource}/{featureID}/navigate/{navigationMode} endpoint
	@Operation(summary = "getFlowlines", description = "returns the flowlines for the specified navigation in WGS84 lat/lon GeoJSON")
	@GetMapping(value="linked-data/comid/{comid}/navigate/{navigationMode}", produces= MediaType.APPLICATION_JSON_VALUE)
	@Deprecated
	public void getFlowlines(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(Parameters.COMID) @Range(min=1, max=Integer.MAX_VALUE) String comid,
			@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
			@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
			@Parameter(description=Parameters.DISTANCE_DESCRIPTION)
				@RequestParam(value=Parameters.DISTANCE, required=false, defaultValue=Parameters.MAX_DISTANCE)
			@Pattern(message=Parameters.DISTANCE_VALIDATION_MESSAGE, regexp=Parameters.DISTANCE_VALIDATION_REGEX) String distance,
			@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {
		BigInteger logId = logService.logRequest(request);
		try {
			streamFlowLines(response, comid, navigationMode, stopComid, distance, isLegacy(legacy, navigationMode));
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}


	//swagger documentation for /linked-data/{featureSource}/{featureID}/navigation/{navigationMode} endpoint
	@Operation(summary = "getFlowlines", description = "returns the flowlines for the specified navigation in WGS84 lat/lon GeoJSON")
	@GetMapping(value="linked-data/comid/{comid}/navigation/{navigationMode}/flowlines", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getNavigationFlowlines(
		HttpServletRequest request, HttpServletResponse response,
		@PathVariable(Parameters.COMID) @Range(min=1, max=Integer.MAX_VALUE) String comid,
		@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
		@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
		@Parameter(description=Parameters.DISTANCE_DESCRIPTION)
		@RequestParam(value=Parameters.DISTANCE, required=false, defaultValue=Parameters.MAX_DISTANCE)
		@Pattern(message=Parameters.DISTANCE_VALIDATION_MESSAGE, regexp=Parameters.DISTANCE_VALIDATION_REGEX) String distance,
		@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {

		BigInteger logId = logService.logRequest(request);
		try {
			streamFlowLines(response, comid, navigationMode, stopComid, distance, isLegacy(legacy, navigationMode));
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}


	//swagger documentation for /linked-data/{featureSource}/{featureID}/navigate/{navigationMode}/{dataSource} endpoint
	@Operation(summary = "getFeatures (deprecated)", description = "Returns all features found along the specified navigation as points in WGS84 lat/lon GeoJSON")
	@GetMapping(value="linked-data/comid/{comid}/navigate/{navigationMode}/{dataSource}", produces= MediaType.APPLICATION_JSON_VALUE)
	@Deprecated
	public void getFeaturesDeprecated(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(Parameters.COMID) @Range(min=1, max=Integer.MAX_VALUE) String comid,
			@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
			@PathVariable(value=DATA_SOURCE) String dataSource,
			@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
			@Parameter(description=Parameters.DISTANCE_DESCRIPTION)
				@RequestParam(value=Parameters.DISTANCE, required=false, defaultValue=Parameters.MAX_DISTANCE)
			@Pattern(message=Parameters.DISTANCE_VALIDATION_MESSAGE, regexp=Parameters.DISTANCE_VALIDATION_REGEX) String distance,
			@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {
		BigInteger logId = logService.logRequest(request);
		try {
			streamFeatures(response, comid, navigationMode, stopComid, distance, dataSource, isLegacy(legacy, navigationMode));
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	//swagger documentation for /linked-data/{featureSource}/{featureID}/navigation/{navigationMode}/{dataSource} endpoint
	@Operation(summary = "getFeatures", description = "Returns all features found along the specified navigation as points in WGS84 lat/lon GeoJSON")
	@GetMapping(value="linked-data/comid/{comid}/navigation/{navigationMode}/{dataSource}", produces= MediaType.APPLICATION_JSON_VALUE)
	public void getFeatures(
		HttpServletRequest request, HttpServletResponse response,
		@PathVariable(Parameters.COMID) @Range(min=1, max=Integer.MAX_VALUE) String comid,
		@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
		@PathVariable(value=DATA_SOURCE) String dataSource,
		@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
		@Parameter(description=Parameters.DISTANCE_DESCRIPTION)
		@RequestParam(value=Parameters.DISTANCE, required=false, defaultValue=Parameters.MAX_DISTANCE)
		@Pattern(message=Parameters.DISTANCE_VALIDATION_MESSAGE, regexp=Parameters.DISTANCE_VALIDATION_REGEX) String distance,
		@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {

		BigInteger logId = logService.logRequest(request);
		try {
			streamFeatures(response, comid, navigationMode, stopComid, distance, dataSource, isLegacy(legacy, navigationMode));
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	//swagger documentation for /linked-data/comid/position endpoint
	@Operation(summary = "getFeatureByCoordinates", description = "returns the feature closest to a provided set of coordinates")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK", content = { @Content(mediaType = "application/json",
			schema = @Schema(implementation = Feature.class)) }),
		@ApiResponse(responseCode = "500", description = "Server error", content = @Content) })
	@GetMapping(value="linked-data/comid/position", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getFeatureByCoordinates(
		HttpServletRequest request, HttpServletResponse response,
		@Parameter(description=Parameters.COORDS_DESCRIPTION)
		@Pattern(message=Parameters.POINT_VALIDATION_MESSAGE, regexp=Parameters.POINT_VALIDATION_REGEX)
		@RequestParam(value=Parameters.COORDS) String coords) throws Exception {
		BigInteger logId = logService.logRequest(request);
		Map<String, Object> map = extractLatitudeAndLongitude(coords);
		try {
        	Integer comid = lookupDao.getComidByLatitudeAndLongitude(map);
			FeatureTransformer transformer = new FeatureTransformer(response, configurationService);

			Map<String, Object> parameterMap = new HashMap<> ();
			parameterMap.put(LookupDao.FEATURE_SOURCE, parameters.COMID);
			parameterMap.put(Parameters.FEATURE_ID, comid);
			addContentHeader(response);
			streamResults(transformer, BaseDao.FEATURE, parameterMap);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	private Map<String, Object> extractLatitudeAndLongitude(String coords)  {
		//Only currently supported format is POINT(x y)
		String tempCoords = coords;
		tempCoords = tempCoords.replace("POINT(", "");
		tempCoords = tempCoords.replace(")", "");
		String[] coordsArray = tempCoords.split(" ");
		Double longitude = Double.parseDouble(coordsArray[0]);
		Double latitude = Double.parseDouble(coordsArray[1]);
		Map<String, Object> map = new HashMap<>();
		map.put(Parameters.LATITUDE, latitude);
		map.put(Parameters.LONGITUDE, longitude);
		return map;
	}
}
