package dz.cirta.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dz.cirta.api.configures.web.serializers.TempRequestBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Abdessamed Diab
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.profiles.active=dev")
class LoginControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(LoginControllerTest.class);
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testLogin() throws Exception {
        mockMvc.perform(
                post("/login")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .secure(true)
                        //.content(new ObjectMapper().writer().writeValueAsString("tokenXXX"))
                        .header("language", 1)
                        .content(
                                new ObjectMapper().writeValueAsString(new TempRequestBody("Key XXXXX TEST"))
                        )
                ).andExpect(status().isNotFound())
                 .andExpect(mvcResult -> mvcResult.getResponse().getContentAsString().contains("empty"));
    }

    @Test
    public void testLoginUsernamePasswordHandler() throws Exception {
        mockMvc.perform(
          post("/login")
              .accept(MediaType.APPLICATION_JSON_VALUE)
              .contentType(MediaType.APPLICATION_JSON_VALUE)
              .secure(true)
              .header("language", 1)
                .content(
                        new ObjectMapper().writeValueAsString(new TempRequestBody("admin", "cirtaflow25"))
                )
        ).andExpect(status().isNotFound())
            .andExpect(mvcResult -> mvcResult.getResponse().getContentAsString().contains("empty"));
    }

    @Test
    void testDataDeletionRequest() throws Exception {
        mockMvc.perform(
                post("/dataDeletionRequest")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .secure(true)
        ).andExpect(status().isOk())
                .andReturn();
    }
}
