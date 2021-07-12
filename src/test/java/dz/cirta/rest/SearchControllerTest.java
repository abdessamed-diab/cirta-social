package dz.cirta.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Abdessamed Diab
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.profiles.active=dev")
class SearchControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(SearchControllerTest.class);
    private MockMvc mockMvc;

    @Value("${dz.cirta.app.volume}")
    private String volumeDirectory;

    @BeforeEach
    public void beforeEach(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testSearchByKeyword() throws Exception {
        mockMvc.perform(
                get("/search/{keyword}", "spring".toLowerCase())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .content(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .header("Authorization", "Bearer tokenXXXXX")
                .secure(false)
        ).andExpect(status().isOk());
    }

}
