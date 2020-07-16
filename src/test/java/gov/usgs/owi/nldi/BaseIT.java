package gov.usgs.owi.nldi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONObjectAs;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.dbunit.dataset.ReplacementDataSet;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.dataset.ReplacementDataSetModifier;

@ActiveProfiles("it")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, 
	TransactionDbUnitTestExecutionListener.class
	})
@DbUnitConfiguration(dataSetLoader=ColumnSensingFlatXMLDataSetLoader.class)
@AutoConfigureTestDatabase(replace=Replace.NONE)
@Transactional(propagation=Propagation.NOT_SUPPORTED)
public abstract class BaseIT {

	public static final String RESULT_FOLDER_WQP  = "feature/feature/wqp/";
	public static final String RESULT_FOLDER_HUC  = "feature/feature/huc12pp/";
	public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Value("${serverContextPath}")
	protected String context;
	protected String urlRoot;

	protected BigInteger id;

	public String getCompareFile(String folder, String file) throws IOException {
		return new String(FileCopyUtils.copyToByteArray(new ClassPathResource("testResult/" + folder + file).getInputStream()));
	}

	protected class IdModifier extends ReplacementDataSetModifier {
		@Override
		protected void addReplacements(ReplacementDataSet dataSet) {
			dataSet.addReplacementSubstring("[id]", id.toString());
		}
	}

	protected class DateTimeModifier extends ReplacementDataSetModifier {
		@Override
		protected void addReplacements(ReplacementDataSet dataSet) {
			dataSet.addReplacementSubstring("[dateTime]", LocalDateTime.now(Clock.systemUTC()).format(dtf));
		}
	}

	protected String assertEntity(TestRestTemplate restTemplate, String url, int httpStatus, String countHeader, String countValue,
			String contentType, String expectedBody, boolean isJson, boolean extraFieldsAllowed) throws JSONException {
		ResponseEntity<String> entity = restTemplate.getForEntity(urlRoot + url, String.class);
		assertEquals(httpStatus, entity.getStatusCodeValue());
		if (null != countHeader) {
			assertEquals(countValue, entity.getHeaders().get(countHeader).get(0).toString());
		}
		if (null != contentType) {
			assertEquals(contentType, entity.getHeaders().getContentType().toString());
		}
		assertTrue(entity.getHeaders().get(HttpHeaders.SET_COOKIE).get(0).startsWith("XSRF-TOKEN"));
		if (null != expectedBody) {
			assertBody(entity, expectedBody, isJson, extraFieldsAllowed);
		}
		return entity.getBody();
	}

	protected void assertBody(ResponseEntity<String> entity, String expectedBody, boolean isJson, boolean extraFieldsAllowed) throws JSONException {
		if (isJson) {
			if (extraFieldsAllowed) {
				assertThat(new JSONObject(entity.getBody()),
						sameJSONObjectAs(new JSONObject(expectedBody)).allowingAnyArrayOrdering().allowingExtraUnexpectedFields());
			} else {
				assertThat(new JSONObject(entity.getBody()),
						sameJSONObjectAs(new JSONObject(expectedBody)).allowingAnyArrayOrdering());
			}
		} else {
			assertEquals(expectedBody, entity.getBody());
		}
	}
}
