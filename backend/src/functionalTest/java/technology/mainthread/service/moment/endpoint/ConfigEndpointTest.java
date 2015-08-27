package technology.mainthread.service.moment.endpoint;

import org.junit.Before;
import org.junit.Test;

import technology.mainthread.service.moment.data.response.ConfigResponse;

import static org.junit.Assert.assertNotNull;

public class ConfigEndpointTest {

    private ConfigEndpoint sut;

    @Before
    public void setUp() throws Exception {
        sut = new ConfigEndpoint();
    }

    @Test
    public void config() throws Exception {
        ConfigResponse config = sut.config();
        assertNotNull(config);
    }
}
