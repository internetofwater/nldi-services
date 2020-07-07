package gov.usgs.owi.nldi.springinit;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

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
		configurer
				.favorPathExtension(false)
				.favorParameter(true)
				.parameterName("f")
				// Browser
				.mediaType("html", MediaType.TEXT_HTML)
				// API
				.mediaType("json", MediaType.APPLICATION_JSON)
				.defaultContentType(MediaType.APPLICATION_JSON)
		;
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