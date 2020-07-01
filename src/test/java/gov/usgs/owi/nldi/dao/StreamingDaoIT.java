package gov.usgs.owi.nldi.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.NavigationMode;
import gov.usgs.owi.nldi.services.Parameters;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.fail;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE,
		classes={DbTestConfig.class, StreamingDao.class})
public class StreamingDaoIT extends BaseIT {

	@Autowired
	StreamingDao streamingDao;

	private class TestResultHandler implements ResultHandler<Object> {
		//TODO put the results somewhere to check them and allow them to be cleared between queries
		public ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		@Override
		public void handleResult(ResultContext<?> context) {
			results.add((Map<String, Object>) context.getResultObject());
		}
	}

	TestResultHandler handler;

	@Before
	public void init() {
		handler = new TestResultHandler();
	}

	@After
	public void cleanup() {
		handler = null;
	}

	@Test
	public void streamFlowLinesTest() {
		//TODO - Real verification - this test just validates that the query has no syntax errors, not that it is logically correct.
		Map<String, Object> parameterMap = new HashMap<>();

		//MyBatis is happy with no parms or ResultHandler - it will read the entire database, load up the list,
		// and not complain or expose it to you (unless you run out of memory). We have a check to make sure the 
		// resultHandler is not null. (The tests were failing on Jenkins with "java.lang.OutOfMemoryError: Java heap space")
		try {
			streamingDao.stream(null, null, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}
		try {
		streamingDao.stream(BaseDao.FLOW_LINES, null, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}
		try {
		streamingDao.stream(BaseDao.FLOW_LINES, parameterMap, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}

		//No limits
		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.DM.toString());
		streamingDao.stream(BaseDao.FLOW_LINES, parameterMap, handler);

		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.UM.toString());
		streamingDao.stream(BaseDao.FLOW_LINES, parameterMap, handler);

		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.UT.toString());
		streamingDao.stream(BaseDao.FLOW_LINES, parameterMap, handler);

		//With distance
		parameterMap.put(Parameters.DISTANCE, 5);

		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.DM.toString());
		streamingDao.stream(BaseDao.FLOW_LINES, parameterMap, handler);

		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.UM.toString());
		streamingDao.stream(BaseDao.FLOW_LINES, parameterMap, handler);

		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.UT.toString());
		streamingDao.stream(BaseDao.FLOW_LINES, parameterMap, handler);
	}

	@Test
	public void streamFeaturesTest() {
		//TODO - Real verification - this test just validates that the query has no syntax errors, not that it is logically correct.
		Map<String, Object> parameterMap = new HashMap<>();

		//MyBatis is happy with no parms or ResultHandler - it will read the entire database, load up the list,
		// and not complain or expose it to you (unless you run out of memory). We have a check to make sure the 
		// resultHandler is not null. (The tests were failing on Jenkins with "java.lang.OutOfMemoryError: Java heap space")
		try {
			streamingDao.stream(null, null, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}
		try {
		streamingDao.stream(BaseDao.FEATURES, null, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}
		try {
		streamingDao.stream(BaseDao.FEATURES, parameterMap, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}

		//No limits
		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.DM.toString());
		streamingDao.stream(BaseDao.FEATURES, parameterMap, handler);

		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.UM.toString());
		streamingDao.stream(BaseDao.FEATURES, parameterMap, handler);

		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.UT.toString());
		streamingDao.stream(BaseDao.FEATURES, parameterMap, handler);

		//With distance
		parameterMap.put(Parameters.DISTANCE, 5);

		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.DM.toString());
		streamingDao.stream(BaseDao.FEATURES, parameterMap, handler);

		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.UM.toString());
		streamingDao.stream(BaseDao.FEATURES, parameterMap, handler);

		parameterMap.put(Parameters.NAVIGATION_MODE, NavigationMode.UT.toString());
		streamingDao.stream(BaseDao.FEATURES, parameterMap, handler);
	}

	@Test
	public void streamFeaturesCollectionTest() {
		//TODO - Real verification - this test just validates that the query has no syntax errors, not that it is logically correct.
		Map<String, Object> parameterMap = new HashMap<>();

		//MyBatis is happy with no parms or ResultHandler - it will read the entire database, load up the list,
		// and not complain or expose it to you (unless you run out of memory). We have a check to make sure the
		// resultHandler is not null. (The tests were failing on Jenkins with "java.lang.OutOfMemoryError: Java heap space")
		try {
			streamingDao.stream(null, null, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}
		try {
			streamingDao.stream(BaseDao.FEATURES_COLLECTION, null, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}
		try {
			streamingDao.stream(BaseDao.FEATURES_COLLECTION, parameterMap, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}

		//No limits
		parameterMap.put(LookupDao.FEATURE_SOURCE, "wqp");

		streamingDao.stream(BaseDao.FEATURES_COLLECTION, parameterMap, handler);

	}

}
