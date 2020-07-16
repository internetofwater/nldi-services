package gov.usgs.owi.nldi.services;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import gov.usgs.owi.nldi.dao.NavigationDao;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class NavigationTest {

	@Mock
	private NavigationDao navigationDao;

	private Navigation navigation;
	private HttpServletResponse response;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		navigation = new Navigation(navigationDao);
		response = new MockHttpServletResponse();
	}

	@Test
	public void interpretResultTestGood() {
		try {
			assertEquals("{4d06cca2-001e-11e6-b9d0-0242ac110003}", navigation.interpretResult(goodResult(), response));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void interpretResultTestBad1() throws UnsupportedEncodingException {
		try {
			assertNull(navigation.interpretResult(badResult1(), response));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertEquals(HttpStatus.BAD_REQUEST.value(), ((MockHttpServletResponse) response).getStatus());
		assertEquals("{\"errorCode\":-1, \"errorMessage\":\"Valid navigation type codes are UM, UT, DM, DD and PP.\"}", ((MockHttpServletResponse) response).getErrorMessage());
	}

	@Test
	public void interpretResultTestBad2() throws UnsupportedEncodingException {
		try {
			assertNull(navigation.interpretResult(badResult2(), response));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertEquals(HttpStatus.BAD_REQUEST.value(), ((MockHttpServletResponse) response).getStatus());
		assertEquals("{\"errorCode\":310, \"errorMessage\":\"Start ComID must have a hydroseq greater than the hydroseq for stop ComID.\"}", ((MockHttpServletResponse) response).getErrorMessage());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void navigateTest() {
		when(navigationDao.getCache(anyMap())).thenReturn("{4d06cca2-001e-11e6-b9d0-0242ac110099}", (String) null);
		when(navigationDao.navigate(anyMap())).thenReturn(goodResult(), badResult1());

		assertEquals("{navigate_cached=(,,,,0,,{4d06cca2-001e-11e6-b9d0-0242ac110099})}", navigation.navigate(new HashMap<String, Object>()).toString());
		verify(navigationDao).getCache(anyMap());
		verify(navigationDao, never()).navigate(anyMap());

		assertEquals("{navigate_cached=(13297246,0.0000000000,,,0,,{4d06cca2-001e-11e6-b9d0-0242ac110003})}", navigation.navigate(new HashMap<String, Object>()).toString());
		verify(navigationDao, times(2)).getCache(anyMap());
		verify(navigationDao).navigate(anyMap());

		assertEquals("{navigate_cached=(,,,,-1,\"Valid navigation type codes are UM, UT, DM, DD and PP.\",)}", navigation.navigate(new HashMap<String, Object>()).toString());
		verify(navigationDao, times(3)).getCache(anyMap());
		verify(navigationDao, times(2)).navigate(anyMap());
	}

	protected Map<String, String> goodResult() {
		Map<String,String> navigationResult = new LinkedHashMap<>();
		navigationResult.put(NavigationDao.NAVIGATE_CACHED, "(13297246,0.0000000000,,,0,,{4d06cca2-001e-11e6-b9d0-0242ac110003})");
		return navigationResult;
	}

	protected Map<String, String> badResult1() {
		Map<String,String> navigationResult = new LinkedHashMap<>();
		navigationResult.put(NavigationDao.NAVIGATE_CACHED, "(,,,,-1,\"Valid navigation type codes are UM, UT, DM, DD and PP.\",)");
		return navigationResult;
	}

	protected Map<String, String> badResult2() {
		Map<String,String> navigationResult = new LinkedHashMap<>();
		navigationResult.put(NavigationDao.NAVIGATE_CACHED, "13297246,1.1545800000,13297198,48.5846800000,310,\"Start ComID must have a hydroseq greater than the hydroseq for stop ComID.\",{f170f490-00ad-11e6-8f62-0242ac110003})");
		return navigationResult;
	}

}
