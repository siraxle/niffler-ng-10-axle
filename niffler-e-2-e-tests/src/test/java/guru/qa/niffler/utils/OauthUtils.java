package guru.qa.niffler.utils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@ParametersAreNonnullByDefault
public class OauthUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Nonnull
    public static String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    @Nonnull
    public static String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(digest);
        }catch (NoSuchAlgorithmException e){
            throw new IllegalStateException("Algorithm not supported", e);
        }
    }
}