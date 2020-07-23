package gov.usgs.owi.nldi.springinit;

import javax.sql.DataSource;

import gov.usgs.owi.nldi.services.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
public class SpringConfig implements WebMvcConfigurer {

	@Autowired
	DataSource dataSource;

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean
	public InternalResourceViewResolver setupViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.strategies(Arrays.asList(new ContentNegotiationStrategy() {
			@Override
			public List<MediaType> resolveMediaTypes(NativeWebRequest webRequest)
				throws HttpMediaTypeNotAcceptableException {
						
				//If the user specifies output with the format parameter, give them what they asked for.
				Map<String, String[]> map = webRequest.getParameterMap();
				if (map != null) {
					String[] values = map.get(Parameters.FORMAT);
					if (values != null) {
						if (values[0].toLowerCase().equals("json")) {
							return Arrays.asList(MediaType.APPLICATION_JSON);
						} else if (values[0].toLowerCase().equals("html")) {
							return Arrays.asList(MediaType.TEXT_HTML);
						}
					}
				}

				//Browsers have 'text/html' as the first element in their accept headers,
				// so if it is the first element, the user has stumbled to this url in the
				// browser and may not expect a json dump.
				String accept = webRequest.getHeader(HttpHeaders.ACCEPT);
				if (accept != null && accept.startsWith(MediaType.TEXT_HTML_VALUE)) {
					return Arrays.asList(MediaType.TEXT_HTML);
				} else {
					return Arrays.asList(MediaType.APPLICATION_JSON);
				}
			}
		}));
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry
			.addMapping("/**")
			.allowedOrigins("*")
			.allowedMethods("GET", "OPTIONS")
			.allowedHeaders("Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers")
			.exposedHeaders("feature_count", "flowLine_count");
	}
}
