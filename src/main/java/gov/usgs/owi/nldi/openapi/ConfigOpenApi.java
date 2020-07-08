package gov.usgs.owi.nldi.openapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gov.usgs.owi.nldi.services.ConfigurationService;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class ConfigOpenApi {

    @Autowired
    private ConfigurationService configurationService;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url(configurationService.getRootUrl()))
                .components(new Components())
                .info(new Info()
                        .title("Network Linked Data Index API")
                        .description("Documentation for the Network Linked Data Index API")
                        .version(configurationService.getAppVersion())
                );
    }
}
