package toy.bookchat.bookchat.config;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import toy.bookchat.bookchat.security.exception.NotVerifiedRequestFormatException;
import toy.bookchat.bookchat.security.oauth.OAuth2Provider;
import toy.bookchat.bookchat.security.token.openid.keys.GooglePublicKeys;
import toy.bookchat.bookchat.security.token.openid.keys.KakaoPublicKeys;

@Slf4j
@Component
public class OpenIdTokenConfig {

    public static final String RSA = "RSA";
    private final OAuth2Config oAuth2Config;
    private final RestTemplate restTemplate;
    private final ConcurrentHashMap<OAuth2Provider, LocalDateTime> publicKeysCachedTime;
    private final KeyFactory keyFactory;
    private KakaoPublicKeys kakaoPublicKeys;
    private GooglePublicKeys googlePublicKeys;

    public OpenIdTokenConfig(RestTemplateBuilder restTemplateBuilder, OAuth2Config oAuth2Config) {
        this.restTemplate = restTemplateBuilder.build();
        this.oAuth2Config = oAuth2Config;
        this.publicKeysCachedTime = new ConcurrentHashMap<>();
        this.keyFactory = createKeyFactory();
    }

    public Key getPublicKey(String keyId, OAuth2Provider oAuth2Provider) {

        if (OAuth2Provider.KAKAO.equals(oAuth2Provider)) {
            checkKakaoPublicKeyCache();
            log.info(this.kakaoPublicKeys.getKeys().get(0).getN());
            return this.kakaoPublicKeys.getKey(keyId, this.keyFactory);
        }

        if (OAuth2Provider.GOOGLE.equals(oAuth2Provider)) {
            checkGooglePublicKeyCache();
            return this.googlePublicKeys.getKey(keyId, this.keyFactory);
        }

        throw new NotVerifiedRequestFormatException(keyId);
    }

    private KeyFactory createKeyFactory() {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(RSA);
        } catch (NoSuchAlgorithmException ignore) {
        }
        return keyFactory;
    }

    private void checkGooglePublicKeyCache() {
        if (isGooglePublicKeyCacheExpired()) {
            refreshGooglePublicKeys();
        }
    }

    private boolean isGooglePublicKeyCacheExpired() {
        return LocalDateTime.now()
            .isAfter(publicKeysCachedTime.getOrDefault(OAuth2Provider.GOOGLE, LocalDateTime.MIN));
    }

    private void refreshGooglePublicKeys() {
        this.googlePublicKeys = fetchGooglePublicKey();
        this.publicKeysCachedTime.put(OAuth2Provider.GOOGLE, LocalDateTime.now().plusDays(3L));
    }

    private GooglePublicKeys fetchGooglePublicKey() {
        return null;
    }

    private void checkKakaoPublicKeyCache() {
        if (isKakaoPublicKeyCacheExpired()) {
            refreshKakaoPublicKeys();
        }
    }

    private boolean isKakaoPublicKeyCacheExpired() {
        return LocalDateTime.now()
            .isAfter(publicKeysCachedTime.getOrDefault(OAuth2Provider.KAKAO, LocalDateTime.MIN));
    }

    private void refreshKakaoPublicKeys() {
        this.kakaoPublicKeys = fetchKakaoPublicKey();
        this.publicKeysCachedTime.put(OAuth2Provider.KAKAO, LocalDateTime.now().plusDays(3L));
    }

    private KakaoPublicKeys fetchKakaoPublicKey() {
        return restTemplate.exchange(oAuth2Config.getKakaoURI(),
            HttpMethod.GET,
            null, KakaoPublicKeys.class).getBody();
    }
}