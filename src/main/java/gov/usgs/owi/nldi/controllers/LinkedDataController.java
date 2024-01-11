package gov.usgs.owi.nldi.controllers;

import gov.usgs.owi.nldi.NavigationMode;
import gov.usgs.owi.nldi.dao.BaseDao;
import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.NavigationDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.model.DataSource;
import gov.usgs.owi.nldi.model.Feature;
import gov.usgs.owi.nldi.model.FeatureList;
import gov.usgs.owi.nldi.services.*;
import gov.usgs.owi.nldi.transform.CharacteristicDataTransformer;
import gov.usgs.owi.nldi.transform.FeatureCollectionTransformer;
import gov.usgs.owi.nldi.transform.FeatureTransformer;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;
import mil.nga.sf.geojson.Position;
import org.hibernate.validator.constraints.Range;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class LinkedDataController extends BaseController {

  private static final String DOWNSTREAM_DIVERSIONS = "downstreamDiversions";
  private static final String DOWNSTREAM_MAIN = "downstreamMain";
  private static final String UPSTREAM_MAIN = "upstreamMain";
  private static final String UPSTREAM_TRIBUTARIES = "upstreamTributaries";
  private static final float SPLIT_CATCHMENT_THRESHOLD = 200f;

  @Autowired
  public LinkedDataController(
      LookupDao inLookupDao,
      StreamingDao inStreamingDao,
      NavigationDao inNavigationDao,
      Navigation inNavigation,
      Parameters inParameters,
      ConfigurationService configurationService,
      LogService inLogService,
      PyGeoApiService inPygeoapiService) {
    super(
        inLookupDao,
        inStreamingDao,
        inNavigationDao,
        inNavigation,
        inParameters,
        configurationService,
        inLogService,
        inPygeoapiService);
  }

  // swagger documentation for /linked-data endpoint
  @Operation(summary = "getDataSources", description = "returns a list of data sources")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = DataSource.class))
            }),
        @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
      })
  @GetMapping(value = "linked-data", produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody List<DataSource> getDataSources(
      HttpServletRequest request, HttpServletResponse response) {
    BigInteger logId = logService.logRequest(request);
    List<DataSource> dataSources;
    try {
      dataSources = lookupDao.getDataSources(configurationService.getLinkedDataUrl());

      // Manually add comid as a feature source.
      String comidFeatureUri =
          String.join("/", configurationService.getLinkedDataUrl(), Parameters.COMID);
      dataSources.add(new DataSource(Parameters.COMID, "NHDPlus comid", comidFeatureUri));
    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }

    return dataSources;
  }

  // swagger documentation for /linked-data/{featureSource} endpoint
  @Operation(
      summary = "getFeatures",
      description = "returns a list of features for a given data source")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Feature.class))
            }),
        @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
      })
  @GetMapping(value = "linked-data/{featureSource}", produces = MediaType.APPLICATION_JSON_VALUE)
  public void getFeatures(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(LookupDao.FEATURE_SOURCE) @Schema(example = "vigil") String featureSource)
      throws Exception {
    BigInteger logId = logService.logRequest(request);
    try (FeatureCollectionTransformer transformer =
        new FeatureCollectionTransformer(response, configurationService)) {
      lookupDao.validateFeatureSource(featureSource);

      Map<String, Object> parameterMap = new HashMap<>();
      parameterMap.put(LookupDao.ROOT_URL, configurationService.getLinkedDataUrl());
      parameterMap.put(LookupDao.FEATURE_SOURCE, featureSource);
      addContentHeader(response);
      streamResults(transformer, BaseDao.FEATURES_COLLECTION, parameterMap);

    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }
  }

  // swagger documentation for /linked-data/{featureSource}/{featureID} endpoint
  @Operation(
      summary = "getRegisteredFeature",
      description = "returns registered feature as WGS84 lat/lon GeoJSON if it exists")
  @GetMapping(
      value = "linked-data/{featureSource}/{featureID}",
      produces = {MIME_TYPE_GEOJSON, MIME_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Feature getRegisteredFeature(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(LookupDao.FEATURE_SOURCE) @Schema(example = "wqp") String featureSource,
      @PathVariable(Parameters.FEATURE_ID) @Schema(example = "USGS-054279485") String featureID) {

    BigInteger logId = logService.logRequest(request);
    Feature feature = null;

    try {
      lookupDao.validateFeatureSource(featureSource);
      lookupDao.validateFeatureSourceAndId(featureSource, featureID);

      feature = lookupDao.getFeature(featureSource, featureID);
      feature.setNavigation(createNavigationUrl(request.getRequestURI()));
    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }

    return feature;
  }

  // swagger documentation for /linked-data/{featureSource}/{featureID}/navigate endpoint
  @Operation(
      summary = "getNavigateTypes (deprecated)",
      description = "returns valid navigation end points")
  @GetMapping(
      value = "linked-data/{featureSource}/{featureID}/navigate",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  @Hidden
  public Map<String, Object> getNavigateTypes(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(LookupDao.FEATURE_SOURCE) @Schema(example = "wqp") String featureSource,
      @PathVariable(Parameters.FEATURE_ID) @Schema(example = "USGS-054279485") String featureID)
      throws UnsupportedEncodingException {
    BigInteger logId = logService.logRequest(request);
    Map<String, Object> rtn = new LinkedHashMap<>();
    try {
      // Verify that the feature source and identifier are valid
      Map<String, Object> parameterMap = new HashMap<>();
      parameterMap.put(LookupDao.FEATURE_SOURCE, featureSource);
      parameterMap.put(Parameters.FEATURE_ID, featureID);
      List<Map<String, Object>> results = lookupDao.getList(BaseDao.FEATURE, parameterMap);

      if (null == results || results.isEmpty()) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
      } else {
        rtn.put(
            UPSTREAM_MAIN,
            String.join(
                "/",
                configurationService.getLinkedDataUrl(),
                featureSource.toLowerCase(),
                URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING),
                NavigationDao.NAVIGATE,
                NavigationMode.UM.toString()));
        rtn.put(
            UPSTREAM_TRIBUTARIES,
            String.join(
                "/",
                configurationService.getLinkedDataUrl(),
                featureSource.toLowerCase(),
                URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING),
                NavigationDao.NAVIGATE,
                NavigationMode.UT.toString()));
        rtn.put(
            DOWNSTREAM_MAIN,
            String.join(
                "/",
                configurationService.getLinkedDataUrl(),
                featureSource.toLowerCase(),
                URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING),
                NavigationDao.NAVIGATE,
                NavigationMode.DM.toString()));
        rtn.put(
            DOWNSTREAM_DIVERSIONS,
            String.join(
                "/",
                configurationService.getLinkedDataUrl(),
                featureSource.toLowerCase(),
                URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING),
                NavigationDao.NAVIGATE,
                NavigationMode.DD.toString()));
      }

    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }
    return rtn;
  }

  @Operation(summary = "getNavigationTypes", description = "returns valid navigation end points")
  @GetMapping(
      value = "linked-data/{featureSource}/{featureID}/navigation",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> getNavigationTypes(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(LookupDao.FEATURE_SOURCE) @Schema(example = "wqp") String featureSource,
      @PathVariable(Parameters.FEATURE_ID) @Schema(example = "USGS-054279485") String featureID)
      throws UnsupportedEncodingException {
    BigInteger logId = logService.logRequest(request);
    Map<String, Object> rtn = new LinkedHashMap<>();
    try {
      lookupDao.validateFeatureSource(featureSource);
      lookupDao.validateFeatureSourceAndId(featureSource, featureID);

      // Verify that the feature source and identifier are valid
      Map<String, Object> parameterMap = new HashMap<>();
      parameterMap.put(LookupDao.FEATURE_SOURCE, featureSource);
      parameterMap.put(Parameters.FEATURE_ID, featureID);
      List<Map<String, Object>> results = lookupDao.getList(BaseDao.FEATURE, parameterMap);

      if (null == results || results.isEmpty()) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
      } else {
        rtn.put(
            UPSTREAM_MAIN,
            String.join(
                "/",
                configurationService.getLinkedDataUrl(),
                featureSource.toLowerCase(),
                URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING),
                NavigationDao.NAVIGATION,
                NavigationMode.UM.toString()));
        rtn.put(
            UPSTREAM_TRIBUTARIES,
            String.join(
                "/",
                configurationService.getLinkedDataUrl(),
                featureSource.toLowerCase(),
                URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING),
                NavigationDao.NAVIGATION,
                NavigationMode.UT.toString()));
        rtn.put(
            DOWNSTREAM_MAIN,
            String.join(
                "/",
                configurationService.getLinkedDataUrl(),
                featureSource.toLowerCase(),
                URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING),
                NavigationDao.NAVIGATION,
                NavigationMode.DM.toString()));
        rtn.put(
            DOWNSTREAM_DIVERSIONS,
            String.join(
                "/",
                configurationService.getLinkedDataUrl(),
                featureSource.toLowerCase(),
                URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING),
                NavigationDao.NAVIGATION,
                NavigationMode.DD.toString()));
      }

    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }
    return rtn;
  }

  // swagger documentation for /linked-data/{featureSource}/{featureID}/{characteristicType}
  // endpoint
  @Operation(
      summary = "getCharacteristicData",
      description = "returns all characteristics of the given type for the specified feature")
  @GetMapping(
      value = "linked-data/{featureSource}/{featureID}/{characteristicType}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void getCharacteristicData(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(LookupDao.FEATURE_SOURCE) @Schema(example = "wqp") String featureSource,
      @PathVariable(Parameters.FEATURE_ID) @Schema(example = "USGS-054279485") String featureID,
      @PathVariable(Parameters.CHARACTERISTIC_TYPE) @Schema(example = "tot")
          String characteristicType,
      @RequestParam(value = Parameters.CHARACTERISTIC_ID, required = false)
          @Schema(example = "[\"TOT_BFI\",\"TOT_PET\",\"TOT_CONTACT\"]")
          String[] characteristicIds)
      throws Exception {
    BigInteger logId = logService.logRequest(request);
    try (CharacteristicDataTransformer transformer = new CharacteristicDataTransformer(response)) {
      lookupDao.validateFeatureSource(featureSource);
      lookupDao.validateFeatureSourceAndId(featureSource, featureID);

      Integer comid = lookupDao.getComidFromFeature(featureSource, featureID);

      Map<String, Object> parameterMap = new HashMap<>();
      parameterMap.put(Parameters.CHARACTERISTIC_TYPE, characteristicType.toLowerCase());
      parameterMap.put(Parameters.COMID, comid);
      parameterMap.put(Parameters.CHARACTERISTIC_ID, characteristicIds);
      addContentHeader(response);
      streamResults(transformer, BaseDao.CHARACTERISTIC_DATA, parameterMap);

    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }
  }

  // swagger documentation for /linked-data/{featureSource}/{featureID}/basin endpoint
  @Operation(
      summary = "getBasin",
      description =
          "returns the aggregated basin for the specified feature in WGS84 lat/lon GeoJSON")
  @GetMapping(
      value = "linked-data/{featureSource}/{featureID}/basin",
      produces = {MediaType.APPLICATION_JSON_VALUE, MIME_TYPE_GEOJSON})
  public void getBasin(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(LookupDao.FEATURE_SOURCE) @Schema(example = "WQP") String featureSource,
      @PathVariable(Parameters.FEATURE_ID) @Schema(example = "USGS-054279485") String featureID,
      @RequestParam(value = Parameters.SIMPLIFIED, required = false, defaultValue = "true")
          Boolean simplified,
      @RequestParam(value = Parameters.SPLIT_CATCHMENT, required = false, defaultValue = "false")
          Boolean splitCatchment)
      throws Exception {

    BigInteger logId = logService.logRequest(request);

    try {
      lookupDao.validateFeatureSource(featureSource);
      lookupDao.validateFeatureSourceAndId(featureSource, featureID);

      // we should only attempt split catchment for point locations
      if (!featureSource.equals(Parameters.COMID)
          && lookupDao.featureIsPointType(featureSource, featureID)
          && splitCatchment) {
        // try to get point on flowline using measure
        Position finalPosition = lookupDao.getPointOnFlowline(featureSource, featureID);

        if (finalPosition == null) {
          // call st_distance to get distance between feature and flowline
          float distance = lookupDao.getDistanceFromFlowline(featureSource, featureID);

          if (distance <= SPLIT_CATCHMENT_THRESHOLD) {
            // get point on flowline closest to feature
            finalPosition = lookupDao.getClosestPointOnFlowline(featureSource, featureID);
          } else {
            // call nldi-flowtrace for a more accurate point on flowline
            Position locationResult = lookupDao.getFeatureLocation(featureSource, featureID);
            finalPosition =
                pygeoapiService.getNldiFlowTraceIntersectionPoint(
                    locationResult, true, PyGeoApiService.Direction.NONE);
          }
        }

        if (finalPosition == null) {
          throw new Exception("Unable to retrieve point on flowline for catchment splitting.");
        }

        // call nldi-splitcatchment
        JSONObject splitCatchmentResponse = pygeoapiService.nldiSplitCatchment(finalPosition, true);
        handleSplitCatchmentResponse(splitCatchmentResponse, response);
      } else {
        Integer comid = lookupDao.getComidFromFeature(featureSource, featureID);
        streamBasin(response, comid, simplified);
      }
    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }
  }

  // swagger documentation for /linked-data/{featureSource}/{featureID}/navigate/{navigationMode}
  // endpoint
  @Operation(
      summary = "getFlowlines (deprecated)",
      description = "returns the flowlines for the specified navigation in WGS84 lat/lon GeoJSON")
  @GetMapping(
      value = "linked-data/{featureSource}/{featureID}/navigate/{navigationMode}",
      produces = {MediaType.APPLICATION_JSON_VALUE, MIME_TYPE_GEOJSON})
  @Deprecated
  @Hidden
  public void getFlowlines(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
      @PathVariable(Parameters.FEATURE_ID) String featureID,
      @PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp = REGEX_NAVIGATION_MODE)
          String navigationMode,
      @RequestParam(value = Parameters.STOP_COMID, required = false)
          @Range(min = 1, max = Integer.MAX_VALUE)
          String stopComid,
      @Parameter(description = Parameters.DISTANCE_DESCRIPTION)
          @RequestParam(
              value = Parameters.DISTANCE,
              required = false,
              defaultValue = Parameters.MAX_DISTANCE)
          @Pattern(
              message = Parameters.DISTANCE_VALIDATION_MESSAGE,
              regexp = Parameters.DISTANCE_VALIDATION_REGEX)
          String distance,
      @RequestParam(value = Parameters.LEGACY, required = false) String legacy)
      throws Exception {

    BigInteger logId = logService.logRequest(request);

    try {
      Integer comid = lookupDao.getComidFromFeature(featureSource, featureID);
      if (null == comid) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
      } else {
        streamFlowLines(
            response,
            comid.toString(),
            navigationMode,
            stopComid,
            distance,
            isLegacy(legacy, navigationMode));
      }
    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }
  }

  // swagger documentation for
  // /linked-data/{featureSource}/{featureID}/navigate/{navigationMode}/{dataSource} endpoint
  @Operation(
      summary = "getFeatures (deprecated)",
      description =
          "Returns all features found along the specified navigation as points in WGS84 lat/lon"
              + " GeoJSON")
  @GetMapping(
      value = "linked-data/{featureSource}/{featureID}/navigate/{navigationMode}/{dataSource}",
      produces = {MediaType.APPLICATION_JSON_VALUE, MIME_TYPE_GEOJSON})
  @Deprecated
  @Hidden
  public void getFeaturesDeprecated(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
      @PathVariable(Parameters.FEATURE_ID) String featureID,
      @PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp = REGEX_NAVIGATION_MODE)
          String navigationMode,
      @PathVariable(value = DATA_SOURCE) String dataSource,
      @RequestParam(value = Parameters.STOP_COMID, required = false)
          @Range(min = 1, max = Integer.MAX_VALUE)
          String stopComid,
      @Parameter(description = Parameters.DISTANCE_DESCRIPTION)
          @RequestParam(
              value = Parameters.DISTANCE,
              required = false,
              defaultValue = Parameters.MAX_DISTANCE)
          @Pattern(
              message = Parameters.DISTANCE_VALIDATION_MESSAGE,
              regexp = Parameters.DISTANCE_VALIDATION_REGEX)
          String distance,
      @RequestParam(value = Parameters.LEGACY, required = false) String legacy)
      throws Exception {

    BigInteger logId = logService.logRequest(request);
    try {
      Integer comid = lookupDao.getComidFromFeature(featureSource, featureID);
      if (null == comid) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
      } else {
        streamFeatures(
            response,
            comid.toString(),
            navigationMode,
            stopComid,
            distance,
            dataSource,
            isLegacy(legacy, navigationMode));
      }
    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }
  }

  // swagger documentation for
  // /linked-data/{featureSource}/{featureID}/navigate/{navigationMode}/{dataSource} endpoint
  @Operation(
      summary = "getFeatures",
      description =
          "Returns all features found along the specified navigation as points in WGS84 lat/lon"
              + " GeoJSON")
  @GetMapping(
      value = "linked-data/{featureSource}/{featureID}/navigation/{navigationMode}/{dataSource}",
      produces = {MediaType.APPLICATION_JSON_VALUE, MIME_TYPE_GEOJSON, MIME_TYPE_JSONLD})
  public FeatureList getFeatures(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(LookupDao.FEATURE_SOURCE) @Schema(example = "wqp") String featureSource,
      @PathVariable(Parameters.FEATURE_ID) @Schema(example = "USGS-054279485") String featureID,
      @PathVariable(Parameters.NAVIGATION_MODE)
          @Pattern(regexp = REGEX_NAVIGATION_MODE)
          @Schema(
              example = "UM",
              allowableValues = {"UM", "UT", "PP", "DM", "DD"})
          String navigationMode,
      @PathVariable(value = DATA_SOURCE) @Schema(example = "nwissite") String dataSource,
      @RequestParam(value = Parameters.STOP_COMID, required = false)
          @Range(min = 1, max = Integer.MAX_VALUE)
          String stopComid,
      @Parameter(description = Parameters.DISTANCE_DESCRIPTION_NEW)
          @RequestParam(value = Parameters.DISTANCE)
          @Range(min = 1, max = 9999, message = Parameters.DISTANCE_VALIDATION_MESSAGE)
          @Schema(example = "50")
          BigDecimal distance,
      @RequestParam(value = Parameters.LEGACY, required = false) String legacy)
      throws Exception {

    BigInteger logId = logService.logRequest(request);
    FeatureList featureList;
    try {
      lookupDao.validateFeatureSource(featureSource);
      lookupDao.validateFeatureSourceAndId(featureSource, featureID);
      lookupDao.validateDataSource(dataSource);

      Integer comid = lookupDao.getComidFromFeature(featureSource, featureID);

      if (isLegacy(legacy, navigationMode)) {
        streamFeatures(
            response,
            comid.toString(),
            navigationMode,
            stopComid,
            String.valueOf(distance),
            dataSource,
            isLegacy(legacy, navigationMode));
        // the legacy version handles the response manually,
        // so we can return null here to avoid overriding
        return null;
      } else {
        featureList = navigationDao.navigateFeatures(comid, dataSource, navigationMode, distance);

        featureList.setNavigationUrls(configurationService.getLinkedDataUrl());

        return featureList;
      }
    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }
  }

  // swagger documentation for /linked-data/{featureSource}/{featureID}/navigation/{navigationMode}
  // endpoint
  @Operation(summary = "getNavigation", description = "returns the navigation options")
  @GetMapping(
      value = "linked-data/{featureSource}/{featureID}/navigation/{navigationMode}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody List<DataSource> getNavigation(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(LookupDao.FEATURE_SOURCE) @Schema(example = "wqp") String featureSource,
      @PathVariable(Parameters.FEATURE_ID) @Schema(example = "USGS-054279485") String featureID,
      @PathVariable(Parameters.NAVIGATION_MODE)
          @Pattern(regexp = REGEX_NAVIGATION_MODE)
          @Schema(
              example = "UM",
              allowableValues = {"UM", "UT", "PP", "DM", "DD"})
          String navigationMode) {

    BigInteger logId = logService.logRequest(request);
    List<DataSource> dataSources;
    try {
      lookupDao.validateFeatureSource(featureSource);
      lookupDao.validateFeatureSourceAndId(featureSource, featureID);

      dataSources = lookupDao.getDataSources(configurationService.getLinkedDataUrl());

      // construct new navigation url
      StringBuilder builder = new StringBuilder();
      builder.append(configurationService.getLinkedDataUrl()).append("/");
      builder.append(featureSource).append("/");
      builder.append(featureID).append("/");
      builder.append("navigation/");
      builder.append(navigationMode).append("/");
      String baseUrl = builder.toString();

      for (DataSource dataSource : dataSources) {
        dataSource.setUri(baseUrl + dataSource.getSource().toLowerCase());
      }

      // add the flowlines navigation endpoint
      DataSource flowlinesDataSource =
          new DataSource("Flowlines", "NHDPlus flowlines", baseUrl + "flowlines");
      dataSources.add(0, flowlinesDataSource);

    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }

    return dataSources;
  }

  // swagger documentation for /linked-data/{featureSource}/{featureID}/navigate/{navigationMode}
  // endpoint
  @Operation(
      summary = "getFlowlines",
      description = "returns the flowlines for the specified navigation in WGS84 lat/lon GeoJSON")
  @GetMapping(
      value = "linked-data/{featureSource}/{featureID}/navigation/{navigationMode}/flowlines",
      produces = {MediaType.APPLICATION_JSON_VALUE, MIME_TYPE_GEOJSON})
  public void getNavigationFlowlines(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(LookupDao.FEATURE_SOURCE) @Schema(example = "wqp") String featureSource,
      @PathVariable(Parameters.FEATURE_ID) @Schema(example = "USGS-054279485") String featureID,
      @PathVariable(Parameters.NAVIGATION_MODE)
          @Pattern(regexp = REGEX_NAVIGATION_MODE)
          @Schema(
              example = "UM",
              allowableValues = {"UM", "UT", "DM", "DD"})
          String navigationMode,
      @RequestParam(value = Parameters.STOP_COMID, required = false)
          @Range(min = 1, max = Integer.MAX_VALUE)
          String stopComid,
      @Parameter(description = Parameters.DISTANCE_DESCRIPTION_NEW)
          @RequestParam(value = Parameters.DISTANCE)
          @Pattern(
              message = Parameters.DISTANCE_VALIDATION_MESSAGE,
              regexp = Parameters.DISTANCE_VALIDATION_REGEX)
          @Schema(example = "5")
          String distance,
      @RequestParam(value = Parameters.TRIM_START, required = false) @Schema(example = "false")
          Boolean trimStart,
      @RequestParam(value = Parameters.TRIM_TOLERANCE, required = false) @Schema(example = "0.1")
          String trimTolerance,
      @RequestParam(value = Parameters.LEGACY, required = false) String legacy)
      throws Exception {

    BigInteger logId = logService.logRequest(request);

    try {
      lookupDao.validateFeatureSource(featureSource);
      lookupDao.validateFeatureSourceAndId(featureSource, featureID);

      Integer comid = lookupDao.getComidFromFeature(featureSource, featureID);

      if (null != trimStart && trimStart == true) {
        String measure = lookupDao.getMeasure(featureSource, featureID, true);
        streamFlowLines(
            response,
            comid.toString(),
            navigationMode,
            stopComid,
            distance,
            measure,
            trimTolerance,
            isLegacy(legacy, navigationMode));
      } else {
        streamFlowLines(
            response,
            comid.toString(),
            navigationMode,
            stopComid,
            distance,
            isLegacy(legacy, navigationMode));
      }
    } finally {
      logService.logRequestComplete(logId, response.getStatus());
    }
  }
}
