package technology.mainthread.service.moment.endpoint;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import technology.mainthread.service.moment.Config;
import technology.mainthread.service.moment.data.response.ConfigResponse;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod;

@Api(
        name = "configApi",
        description = "Gives client version numbers for force update",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = Config.OWNER_DOMAIN,
                ownerName = Config.OWNER_NAME
        )
)
public class ConfigEndpoint {

    /**
     * Get a ConfigResponse
     *
     * @return ConfigResponse - contains backend version and min android version
     */
    @ApiMethod(
            name = "config",
            httpMethod = HttpMethod.GET
    )
    public ConfigResponse config() {
        return new ConfigResponse();
    }

}
