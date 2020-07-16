package gov.usgs.owi.nldi.dao;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.Parameters;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE,
		classes={DbTestConfig.class, LookupDao.class, ConfigurationService.class})
@DatabaseSetup("classpath:/testData/crawlerSource.xml")
@DatabaseSetup("classpath:/testData/featureWqp.xml")
public class LookupDaoIT extends BaseIT {


	@Autowired
	private LookupDao lookupDao;

	@Autowired
	private ConfigurationService configurationService;

	@Test
	public void getFeatureTest() throws IOException, JSONException {
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put(LookupDao.FEATURE_SOURCE, "wqp");
		parameterMap.put(Parameters.FEATURE_ID, "USGS-05427880");
		Map<String, Object> results = lookupDao.getComid(BaseDao.FEATURE, parameterMap);
		assertEquals(1,results.size());
		assertEquals(13294132, results.get(BaseDao.COMID));
	}

	@Test
	public void getComidTest() throws IOException, JSONException {
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put(LookupDao.FEATURE_SOURCE, "comid");
		parameterMap.put(Parameters.FEATURE_ID, "13297246");
		Map<String, Object> results = lookupDao.getComid(BaseDao.FEATURE, parameterMap);
		assertEquals(1,results.size());
		assertEquals(13297246, results.get(BaseDao.COMID));
	}

	@Test
	public void getDataSourcesTest() {
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put(LookupDao.ROOT_URL, configurationService.getLinkedDataUrl());
		List<Map<String, Object>> results = lookupDao.getList(BaseDao.DATA_SOURCES, parameterMap);
		assertFalse(results.isEmpty());
		assertEquals(4, results.size());
		assertEquals("huc12pp", results.get(0).get(LookupDao.SOURCE));
		assertEquals("huc12pp", results.get(0).get(LookupDao.SOURCE_NAME));
		assertEquals(String.join("/", configurationService.getLinkedDataUrl(), "huc12pp"), results.get(0).get(BaseDao.FEATURES));
		assertEquals("np21_nwis", results.get(1).get(LookupDao.SOURCE));
		assertEquals("HNDPlusV2_NWIS_Gages", results.get(1).get(LookupDao.SOURCE_NAME));
		assertEquals(String.join("/", configurationService.getLinkedDataUrl(), "np21_nwis"), results.get(1).get(BaseDao.FEATURES));
		assertEquals("TEST", results.get(2).get(LookupDao.SOURCE));
		assertEquals("TEST Source", results.get(2).get(LookupDao.SOURCE_NAME));
		assertEquals(String.join("/", configurationService.getLinkedDataUrl(), "test"), results.get(2).get(BaseDao.FEATURES));
		assertEquals("WQP", results.get(3).get(LookupDao.SOURCE));
		assertEquals("Water Quality Portal", results.get(3).get(LookupDao.SOURCE_NAME));
		assertEquals(String.join("/", configurationService.getLinkedDataUrl(), "wqp"), results.get(3).get(BaseDao.FEATURES));
	}


}
