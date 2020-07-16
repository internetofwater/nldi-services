package gov.usgs.owi.nldi.dao;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.springinit.DbTestConfig;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment=WebEnvironment.NONE,
		classes={DbTestConfig.class, StreamingDao.class})
public class LegacyStreamingDaoIT extends BaseIT{

	@Autowired
	private StreamingDao streamingDao;

	private class TestResultHandler implements ResultHandler<Object> {
		//TODO put the results somewhere to check them and allow them to be cleared between queries
//		public ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		@Override
		public void handleResult(ResultContext<?> context) {
//			results.add((Map<String, Object>) context.getResultObject());
		}
	}

	TestResultHandler handler;

	@BeforeEach
	public void setUp() {
		handler = new TestResultHandler();
	}

	@AfterEach
	public void tearDown() {
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
			streamingDao.stream(BaseDao.FLOW_LINES_LEGACY, null, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
					fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}
		try {
			streamingDao.stream(BaseDao.FLOW_LINES_LEGACY, parameterMap, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}

		streamingDao.stream(BaseDao.FLOW_LINES_LEGACY, parameterMap, handler);

		parameterMap.put(StreamingDao.SESSION_ID, "abc");
		streamingDao.stream(BaseDao.FLOW_LINES_LEGACY, parameterMap, handler);

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
			streamingDao.stream(BaseDao.FEATURES_LEGACY, null, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}
		try {
			streamingDao.stream(BaseDao.FEATURES_LEGACY, parameterMap, null);
		} catch (RuntimeException e) {
			if (!"A ResultHandler is required for the StreamingDao.stream".equalsIgnoreCase(e.getMessage())) {
				fail("Expected a RuntimeException, but got " + e.getLocalizedMessage());
			}
		}

		streamingDao.stream(BaseDao.FEATURES_LEGACY, parameterMap, handler);

		parameterMap.put(StreamingDao.SESSION_ID, "abc");
		streamingDao.stream(BaseDao.FEATURES_LEGACY, parameterMap, handler);
	}

}
