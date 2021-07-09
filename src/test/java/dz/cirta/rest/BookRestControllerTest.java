package dz.cirta.rest;

import dz.cirta.data.models.Book;
import dz.cirta.data.models.CirtaUser;
import dz.cirta.data.models.Comment;
import dz.cirta.data.service.BusinessLogic;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = "spring.profiles.active=dev")
//@SpringJUnitWebConfig
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.profiles.active=dev") // integration test because we have filters.
class BookRestControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(BookRestController.class);
    private MockMvc mockMvc;

    @Value("${dz.cirta.app.volume}")
    private String volumeDirectory;

    @Autowired
    private BusinessLogic dao;

    @Autowired
    private MappingJackson2HttpMessageConverter myMappingJackson2HttpMessageConverter;

    @Autowired
    private Session hibernateSession;

    @BeforeEach
    public void beforeEach(WebApplicationContext webApplicationContext) {
        // mockMvc = MockMvcBuilders.standaloneSetup(new BookRestController()).build(); we have filters to configure, we need to pass to integration testing.
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }



    @Test
    public void testStreamBookByBookId() throws Exception {
        mockMvc.perform(
                get("/book/stream/{bookId}", new Long(1))
                .header("Authorization", "Bearer 34i3j4iom2323==")
                .contentType(MediaType.APPLICATION_JSON_VALUE) // consume
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE) // produce
                .secure(true)
                .header("source-url", volumeDirectory+"books/1.pdf")
        ).andExpect(status().isOk());
    }

    @Test
    public void testStreamPagesOfBook() throws Exception {
        Long bookId = 5L; // arg1
        int startPage = 150; // arg2

        mockMvc.perform(
                get("/book/stream/{bookId}/{startPage}", bookId, startPage)
                        .header("Authorization", "Bearer 34i3j4iom2323==")
                        .contentType(MediaType.APPLICATION_JSON_VALUE) // consume
                        .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE) // produce
                        .secure(true)
                        .header("source-url", volumeDirectory+"books/spring-social-reference.pdf")
        ).andExpect(status().isOk());
    }

    @Test
    public void testStreamPagesOfBookDoesNotExist() throws Exception {
        Long bookId = 12354L; // arg1 book does not exist.
        int startPage = 1; // arg2

        mockMvc.perform(
                get("/book/stream/{bookId}/{startPage}", bookId, startPage)
                        .header("Authorization", "Bearer 34i3j4iom2323==")
                        .contentType(MediaType.APPLICATION_JSON_VALUE) // consume
                        .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE) // produce
                        .header("source-url", volumeDirectory+"books/14568.pdf")
                        .secure(true)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "tester", password = "rahba")
    public void testExtractBookmark() throws Exception {
        Long bookId = 2L; // arg1 book does not exist.
        String bookmark = mockMvc.perform(
                get("/book/bookmark/{bookId}", bookId)
                        .header("Authorization", "Bearer 34i3j4iom2323==")
                        .contentType(MediaType.APPLICATION_JSON_VALUE) // consume
                        .accept(MediaType.APPLICATION_JSON_VALUE) // produce
                        .header("source-url", volumeDirectory+"books/spring-social-reference.pdf")
                        .secure(true)
        ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        logger.info(bookmark);
    }

    @Test
    public void testExtractBookmarkNotAcceptable() throws Exception {
        Long bookId = 1L; // arg1 book does not exist.
        mockMvc.perform(
                get("/book/bookmark/{bookId}", bookId)
                        .header("Authorization", "Bearer 34i3j4iom2323==")
                        .contentType(MediaType.APPLICATION_JSON_VALUE) // consume
                        .accept(MediaType.APPLICATION_JSON_VALUE) // produce
                        .header("source-url", volumeDirectory+"books/1.pdf")
                        .secure(true)
        ).andExpect(status().isNotAcceptable());
    }

    @Test
    public void testPostAndFetchComment() throws Exception {
        CirtaUser author = new CirtaUser("798546", "di ab", "abdes samed", "damy name");
        author.setLanguage((byte) 1); // dont use arabic localeDateTime pattern for testing.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        author, null, Arrays.asList(new SimpleGrantedAuthority("DEVELOPER"))
                )
        );

        Book book = hibernateSession.find(Book.class, 2L);
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setBook(book);
        comment.setBadge(Comment.BADGES.BADGE_LIGHT.value);
        comment.setContent("what the fuck!");
        comment.setPageNumber(13);
        comment.setPublishedAt(LocalDateTime.now().minusMinutes(10));


        hibernateSession.getTransaction().begin();
        hibernateSession.save(author);
        hibernateSession.getTransaction().commit();
        String content = myMappingJackson2HttpMessageConverter.getObjectMapper().writeValueAsString(comment);
        String result = mockMvc.perform(
                post("/book/{bookId}/comment", book.getId())
                        .header("Authorization", "Bearer 34i3j4iom2323==")
                        .contentType(MediaType.APPLICATION_JSON_VALUE) // consume
                        .accept(MediaType.APPLICATION_JSON_VALUE) // produce
                        .secure(true)
                        .content(content)
        ).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Comment postedComment  = myMappingJackson2HttpMessageConverter.getObjectMapper().readValue(result, Comment.class);
        logger.info("posted comment: " + postedComment.getId());
        assertNotNull(postedComment.getId());

        String fetch = mockMvc.perform(
                get("/book/{bookId}/comments/{pageNumber}", book.getId(), 13)
                        .header("Authorization", "Bearer 34i3j4iom2323==")
                        .contentType(MediaType.APPLICATION_JSON_VALUE) // consume
                        .accept(MediaType.APPLICATION_JSON_VALUE) // produce
                        .secure(true)
        ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        logger.info(fetch);
    }

}
