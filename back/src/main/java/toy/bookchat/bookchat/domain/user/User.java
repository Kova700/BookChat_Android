package toy.bookchat.bookchat.domain.user;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import toy.bookchat.bookchat.domain.bookshelf.BookShelf;
import toy.bookchat.bookchat.security.oauth.OAuth2Provider;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /* TODO: 2022-08-30 사용자 이름에 oauth2 회원번호 넣기
     */
    private String name;
    private String nickName;
    private String email;
    private String profileImageUrl;
    private ROLE role;
    @Enumerated(EnumType.STRING)
    private OAuth2Provider provider;
    @OneToMany(mappedBy = "user")
    private List<BookShelf> bookShelves = new ArrayList<>();
    @ElementCollection
    private List<ReadingTaste> readingTastes = new ArrayList<>();

    @Builder
    public User(String name, String email, String profileImageUrl, ROLE role,
        OAuth2Provider provider, String nickName, List<ReadingTaste> readingTastes) {
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.provider = provider;
        this.nickName = nickName;
        this.readingTastes = readingTastes;
    }

    public void setBookShelf(BookShelf bookShelf) {
        this.getBookShelves().add(bookShelf);
        bookShelf.setUser(this);
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateImageUrl(String imageUrl) {
        this.profileImageUrl = imageUrl;
    }
}
