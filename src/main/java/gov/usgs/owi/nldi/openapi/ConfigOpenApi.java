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
                        .description("The NLDI is a search service that takes a watershed outlet identifier as a starting point, a navigation mode to perform, and the type of data desired in response to the request. It can provide geospatial representations of the navigation or linked data sources found along the navigation. It also has the ability to return landscape characteristics for the catchment the watershed outlet is contained in or the total upstream basin.")
                        .version(configurationService.getAppVersion())
                );
    }
}
