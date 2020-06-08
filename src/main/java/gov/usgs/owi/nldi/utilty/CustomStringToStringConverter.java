package gov.usgs.owi.nldi.utilty;
import org.springframework.util.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CustomStringToStringConverter implements Converter<String, String>{
	@Override
	public String convert(String source) {
		return source == null || source.isBlank() ? null : source;
	}

}
