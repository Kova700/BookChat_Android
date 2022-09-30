package toy.bookchat.bookchat.domain.bookshelf.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static toy.bookchat.bookchat.domain.user.ROLE.USER;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import toy.bookchat.bookchat.config.OpenIdTokenConfig;
import toy.bookchat.bookchat.domain.AuthenticationTestExtension;
import toy.bookchat.bookchat.domain.bookshelf.ReadingStatus;
import toy.bookchat.bookchat.domain.bookshelf.Star;
import toy.bookchat.bookchat.domain.bookshelf.service.BookShelfService;
import toy.bookchat.bookchat.domain.bookshelf.service.dto.BookShelfRequestDto;
import toy.bookchat.bookchat.domain.bookshelf.service.dto.BookShelfSearchResponseDto;
import toy.bookchat.bookchat.domain.user.User;
import toy.bookchat.bookchat.domain.user.repository.UserRepository;
import toy.bookchat.bookchat.security.SecurityConfig;
import toy.bookchat.bookchat.security.oauth.OAuth2Provider;
import toy.bookchat.bookchat.security.token.jwt.JwtTokenManager;
import toy.bookchat.bookchat.security.token.openid.OpenIdTokenManager;
import toy.bookchat.bookchat.security.user.UserPrincipal;

@WebMvcTest(controllers = BookShelfController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class}))
@AutoConfigureRestDocs
public class BookShelfControllerTest extends AuthenticationTestExtension {

    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    OpenIdTokenManager openIdTokenManager;
    @MockBean
    JwtTokenManager jwtTokenManager;
    @MockBean
    OpenIdTokenConfig openIdTokenConfig;
    @MockBean
    UserRepository userRepository;
    @MockBean
    BookShelfService bookShelfService;
    @Autowired
    private MockMvc mockMvc;

    private User getUser() {
        return User.builder()
                .email("test@gmail.com")
                .role(USER)
                .name("testUser")
                .profileImageUrl("somethingImageUrl@naver.com")
                .build();
    }

    private UserPrincipal getUserPrincipal() {
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        User user = User.builder()
            .email("test@gmail.com")
            .name("testUser")
            .profileImageUrl("somethingImageUrl@naver.com")
            .build();

        return new UserPrincipal(1L, user.getEmail(), user.getName(), user.getNickname(), user.getProfileImageUrl(),
            user.getDefaultProfileImageType(), authorities, user);
    }

    private BookShelfRequestDto getBookShelfRequestDto(ReadingStatus readingStatus) {

        if(readingStatus == ReadingStatus.COMPLETE) {
            return BookShelfRequestDto.builder()
                    .isbn("124151214")
                    .title("effectiveJava")
                    .authors(List.of("Joshua"))
                    .publisher("oreilly")
                    .bookCoverImageUrl("bookCoverImage.com")
                    .readingStatus(readingStatus)
                    .star(Star.FOUR)
                    .singleLineAssessment("very good")
                    .build();
        }
        return BookShelfRequestDto.builder()
            .isbn("124151214")
            .title("effectiveJava")
            .authors(List.of("Joshua"))
            .publisher("oreilly")
            .bookCoverImageUrl("bookCoverImage.com")
            .readingStatus(readingStatus)
            .build();
    }

    private String getTestToken() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub","test");
        claims.put("name","google123");
        claims.put("provider", OAuth2Provider.GOOGLE);
        claims.put("email","test@gmail.com");

        String testToken = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, "test")
                .compact();

        return testToken;
    }

    @Test
    public void 로그인하지_않은_사용자_요청_401() throws Exception {
        mockMvc.perform(post("/v1/api/bookshelf/books")
                .content(objectMapper.writeValueAsString(getBookShelfRequestDto(ReadingStatus.READING)))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void 읽고_있는_책_등록_성공() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(getBookShelfRequestDto(ReadingStatus.READING)))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isCreated())
            .andDo(document("bookshelf-reading",
                requestHeaders(
                    headerWithName("Authorization").description("Bearer [JWT token]")),
                requestFields(fieldWithPath("isbn").description("ISBN"),
                    fieldWithPath("title").type(STRING).description("제목"),
                    fieldWithPath("authors[]").type(ARRAY).description("저자"),
                    fieldWithPath("publisher").type(STRING).description("출판사"),
                    fieldWithPath("bookCoverImageUrl").type(STRING).optional().description("책 커버 이미지 URI"),
                    fieldWithPath("readingStatus").type(STRING).description("READING"),
                    fieldWithPath("star").ignored(),
                    fieldWithPath("singleLineAssessment").ignored())));

        verify(bookShelfService).putBookOnBookShelf(any(BookShelfRequestDto.class),
            any(User.class));
    }

    @Test
    public void 읽은_책_등록_성공() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(
                    objectMapper.writeValueAsString(getBookShelfRequestDto(ReadingStatus.COMPLETE)))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isCreated())
            .andDo(document("bookshelf-complete",
                requestHeaders(
                    headerWithName("Authorization").description("Bearer [JWT token]")
                ),
                requestFields(fieldWithPath("isbn").type(STRING).description("ISBN"),
                    fieldWithPath("title").type(STRING).description("제목"),
                    fieldWithPath("authors[]").type(ARRAY).description("저자"),
                    fieldWithPath("publisher").type(STRING).description("출판사"),
                    fieldWithPath("bookCoverImageUrl").type(STRING).optional().description("책 커버 이미지 URI"),
                    fieldWithPath("readingStatus").type(STRING).description("COMPLETE"),
                    fieldWithPath("star").type(STRING).description("평점"),
                    fieldWithPath("singleLineAssessment").type(STRING).description("한 줄 평"))));

        verify(bookShelfService).putBookOnBookShelf(any(BookShelfRequestDto.class),
            any(User.class));
    }

    @Test
    public void 읽은_책_등록시_평점_한줄평_없으면_400반환() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));
        doThrow(IllegalArgumentException.class).when(bookShelfService).putBookOnBookShelf(any(),any());

        BookShelfRequestDto bookShelfRequestDto = BookShelfRequestDto.builder()
                .isbn("124151214")
                .title("effectiveJava")
                .authors(List.of("Joshua"))
                .publisher("oreilly")
                .bookCoverImageUrl("bookCoverImage.com")
                .readingStatus(ReadingStatus.COMPLETE)
                .build();

        mockMvc.perform(post("/v1/api/bookshelf/books")
                        .header("Authorization", "Bearer " + getTestToken())
                        .content(
                                objectMapper.writeValueAsString(bookShelfRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(getUserPrincipal())))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void 읽을_책_등록_성공() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(getBookShelfRequestDto(ReadingStatus.WISH)))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isCreated())
            .andDo(document("bookshelf-wish",
                requestHeaders(
                    headerWithName("Authorization").description("Bearer [openid token]")
                ),
                requestFields(fieldWithPath("isbn").type(STRING).description("ISBN"),
                    fieldWithPath("title").type(STRING).description("제목"),
                    fieldWithPath("authors[]").type(ARRAY).description("저자"),
                    fieldWithPath("publisher").type(STRING).description("출판사"),
                    fieldWithPath("bookCoverImageUrl").type(STRING).optional().description("책 커버 이미지 URI"),
                    fieldWithPath("readingStatus").type(STRING).description("WISH"),
                    fieldWithPath("star").ignored(),
                    fieldWithPath("singleLineAssessment").ignored())));

        verify(bookShelfService).putBookOnBookShelf(any(BookShelfRequestDto.class),
            any(User.class));
    }

    @Test
    public void null로_요청_실패() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void 존재하지않은_readingstatus_책_등록_실패() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        BookShelfTestRequestDto bookShelfTestRequestDto = new BookShelfTestRequestDto("124151214",
            "effectiveJava", List.of("Joshua"), "oreilly",
            "bookCoverImage.com", "NOT_EXISTED_READING_STATUS");

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(bookShelfTestRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void readingStatus_없이_요청_실패() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        BookShelfRequestDto bookShelfRequestDto = BookShelfRequestDto.builder()
            .isbn("135135414")
            .title("effectiveJava")
            .authors(List.of("Joshua"))
            .publisher("oreilly")
            .bookCoverImageUrl("bookCoverImage.com")
            .build();

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(bookShelfRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isBadRequest());

    }

    @Test
    public void isbn_없이_책_등록_요청_실패() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        BookShelfRequestDto bookShelfRequestDto = BookShelfRequestDto.builder()
            .title("effectiveJava")
            .authors(List.of("Joshua"))
            .publisher("oreilly")
            .bookCoverImageUrl("bookCoverImage.com")
            .readingStatus(ReadingStatus.WISH)
            .build();

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(bookShelfRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void isbn_빈_문자열_요청_실패() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        BookShelfRequestDto bookShelfRequestDto = BookShelfRequestDto.builder()
            .isbn("")
            .title("effectiveJava")
            .authors(List.of("Joshua"))
            .publisher("oreilly")
            .bookCoverImageUrl("bookCoverImage.com")
            .readingStatus(ReadingStatus.WISH)
            .build();

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(bookShelfRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void 제목_없이_요청_실패() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        BookShelfRequestDto bookShelfRequestDto = BookShelfRequestDto.builder()
            .isbn("124151214")
            .authors(List.of("Joshua"))
            .publisher("oreilly")
            .bookCoverImageUrl("bookCoverImage.com")
            .readingStatus(ReadingStatus.WISH)
            .build();

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(bookShelfRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void 제목_빈_문자열_요청_실패() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        BookShelfRequestDto bookShelfRequestDto = BookShelfRequestDto.builder()
            .isbn("124151214")
            .title("")
            .authors(List.of("Joshua"))
            .publisher("oreilly")
            .bookCoverImageUrl("bookCoverImage.com")
            .readingStatus(ReadingStatus.WISH)
            .build();

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(bookShelfRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isBadRequest());

    }

    @Test
    public void 작가명_없이_요청_실패() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        BookShelfRequestDto bookShelfRequestDto = BookShelfRequestDto.builder()
            .isbn("124151214")
            .title("effectiveJava")
            .publisher("oreilly")
            .bookCoverImageUrl("bookCoverImage.com")
            .readingStatus(ReadingStatus.WISH)
            .build();

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(bookShelfRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isBadRequest());

    }

    @Test
    public void 작가명_빈_문자열_요청_실패() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        BookShelfRequestDto bookShelfRequestDto = BookShelfRequestDto.builder()
            .isbn("124151214")
            .title("effectiveJava")
            .authors(List.of(""))
            .publisher("oreilly")
            .bookCoverImageUrl("bookCoverImage.com")
            .readingStatus(ReadingStatus.WISH)
            .build();

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(bookShelfRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isBadRequest());

    }

    @Test
    public void 출판사_없이_요청_실패() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        BookShelfRequestDto bookShelfRequestDto = BookShelfRequestDto.builder()
            .isbn("124151214")
            .title("effectiveJava")
            .authors(List.of("Joshua"))
            .bookCoverImageUrl("bookCoverImage.com")
            .readingStatus(ReadingStatus.WISH)
            .build();

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(bookShelfRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isBadRequest());

    }

    @Test
    public void 출판사_빈_문자열_요청_실패() throws Exception {
        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));

        BookShelfRequestDto bookShelfRequestDto = BookShelfRequestDto.builder()
            .isbn("124151214")
            .title("effectiveJava")
            .authors(List.of("Joshua"))
            .publisher("")
            .bookCoverImageUrl("bookCoverImage.com")
            .readingStatus(ReadingStatus.WISH)
            .build();

        mockMvc.perform(post("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .content(objectMapper.writeValueAsString(bookShelfRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(getUserPrincipal())))
            .andExpect(status().isBadRequest());

    }

    @Test
    public void 읽고있는_책_조회_성공() throws Exception {
        List<BookShelfSearchResponseDto> result = new ArrayList<>();

        BookShelfSearchResponseDto bookShelfSearchResponseDto = BookShelfSearchResponseDto.builder()
            .title("effectiveJava")
            .authors(List.of("joshua"))
            .bookCoverImageUrl("testBookCoverImage@naver.com")
            .publisher("jpub")
            .star(null)
            .singleLineAssessment(null).build();

        result.add(bookShelfSearchResponseDto);

        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));
        when(bookShelfService.takeBooksOutOfBookShelf(any(ReadingStatus.class), any(Pageable.class),
            any(User.class))).thenReturn(result);

        mockMvc.perform(get("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .queryParam("readingStatus", "READING")
                .queryParam("size", "5")
                .queryParam("page", "1")
                .queryParam("sort", "id,DESC")
                .with(user(getUserPrincipal())))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("get-bookshelf-reading",
                requestHeaders(
                    headerWithName("Authorization").description("Bearer [JWT token]")
                ),
                requestParameters(
                    parameterWithName("readingStatus").description("READING"),
                    parameterWithName("size").description("page 당 size"),
                    parameterWithName("page").description("한번에 조회할 page수"),
                    parameterWithName("sort").description("등록순-id")
                ),
                responseFields(
                    fieldWithPath("[].title").type(STRING).description("제목"),
                    fieldWithPath("[].bookCoverImageUrl").type(STRING).optional().description("책 커버 이미지 URI"),
                    fieldWithPath("[].authors[]").type(ARRAY).description("저자"),
                    fieldWithPath("[].publisher").type(STRING).description("출판사"),
                    fieldWithPath("[].star").type(STRING).optional().description("평점"),
                    fieldWithPath("[].singleLineAssessment").type(STRING).optional().description("한 줄 평")
                    ))
            );

        verify(bookShelfService).takeBooksOutOfBookShelf(any(ReadingStatus.class),
            any(Pageable.class), any(User.class));
    }

    @Test
    public void 읽은_책_조회_성공() throws Exception {
        List<BookShelfSearchResponseDto> result = new ArrayList<>();

        BookShelfSearchResponseDto bookShelfSearchResponseDto = BookShelfSearchResponseDto.builder()
            .title("effectiveJava")
            .authors(List.of("joshua"))
            .bookCoverImageUrl("testBookCoverImage@naver.com")
            .publisher("jpub")
            .star(Star.FOUR_HALF)
            .singleLineAssessment("it's is best").build();

        result.add(bookShelfSearchResponseDto);

        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));
        when(bookShelfService.takeBooksOutOfBookShelf(any(ReadingStatus.class), any(Pageable.class),
            any(User.class))).thenReturn(result);

        mockMvc.perform(get("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .queryParam("readingStatus", "COMPLETE")
                .queryParam("size", "5")
                .queryParam("page", "1")
                .queryParam("sort", "id,DESC")
                .with(user(getUserPrincipal())))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("get-bookshelf-complete",
                requestHeaders(
                    headerWithName("Authorization").description("Bearer [JWT token]")
                ),
                requestParameters(
                    parameterWithName("readingStatus").description("COMPLETE"),
                    parameterWithName("size").description("page 당 size"),
                    parameterWithName("page").description("한번에 조회할 page수"),
                    parameterWithName("sort").description("등록순-id")
                ),
                responseFields(
                    fieldWithPath("[].title").type(STRING).description("제목"),
                    fieldWithPath("[].bookCoverImageUrl").type(STRING).optional().description("책 커버 이미지 URI"),
                    fieldWithPath("[].authors[]").type(ARRAY).description("저자"),
                    fieldWithPath("[].publisher").type(STRING).description("출판사"),
                    fieldWithPath("[].star").type(STRING).description("평점"),
                    fieldWithPath("[].singleLineAssessment").type(STRING).description("한 줄 평")
                ))
            );

        verify(bookShelfService).takeBooksOutOfBookShelf(any(ReadingStatus.class),
            any(Pageable.class), any(User.class));
    }

    @Test
    public void 읽을_책_조회_성공() throws Exception {
        List<BookShelfSearchResponseDto> result = new ArrayList<>();

        BookShelfSearchResponseDto bookShelfSearchResponseDto = BookShelfSearchResponseDto.builder()
            .title("effectiveJava")
            .authors(List.of("joshua"))
            .bookCoverImageUrl("testBookCoverImage@naver.com")
            .publisher("jpub")
            .star(null)
            .singleLineAssessment(null).build();

        result.add(bookShelfSearchResponseDto);

        when(userRepository.findByName(any())).thenReturn(Optional.ofNullable(getUser()));
        when(bookShelfService.takeBooksOutOfBookShelf(any(ReadingStatus.class), any(Pageable.class),
            any(User.class))).thenReturn(result);

        mockMvc.perform(get("/v1/api/bookshelf/books")
                .header("Authorization", "Bearer " + getTestToken())
                .queryParam("readingStatus", "WISH")
                .queryParam("size", "5")
                .queryParam("page", "1")
                .queryParam("sort", "id,DESC")
                .with(user(getUserPrincipal())))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("get-bookshelf-wish",
                requestHeaders(
                    headerWithName("Authorization").description("Bearer [JWT token]")
                ),
                requestParameters(
                    parameterWithName("readingStatus").description("WISH"),
                    parameterWithName("size").description("page 당 size"),
                    parameterWithName("page").description("한번에 조회할 page수"),
                    parameterWithName("sort").description("등록순-id")
                ),
                responseFields(
                    fieldWithPath("[].title").type(STRING).description("제목"),
                    fieldWithPath("[].bookCoverImageUrl").type(STRING).optional().description("책 커버 이미지 URI"),
                    fieldWithPath("[].authors[]").type(ARRAY).description("저자"),
                    fieldWithPath("[].publisher").type(STRING).description("출판사"),
                    fieldWithPath("[].star").type(STRING).optional().description("평점"),
                    fieldWithPath("[].singleLineAssessment").type(STRING).optional().description("한 줄 평")
                    ))
            );

        verify(bookShelfService).takeBooksOutOfBookShelf(any(ReadingStatus.class),
            any(Pageable.class), any(User.class));
    }

    static class BookShelfTestRequestDto {

        @JsonProperty
        String isbn;
        @JsonProperty
        String title;
        @JsonProperty
        List<String> author;
        @JsonProperty
        String publisher;
        @JsonProperty
        String bookCoverImageUrl;
        @JsonProperty
        String readingStatus;

        public BookShelfTestRequestDto(String isbn, String title, List<String> author,
            String publisher, String bookCoverImageUrl, String readingStatus) {
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.publisher = publisher;
            this.bookCoverImageUrl = bookCoverImageUrl;
            this.readingStatus = readingStatus;
        }
    }

}
