package education.uwithme.bot.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SHA256Utility {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static final String HMAC_SHA_256 = "HmacSHA256";
    public static final String SHA_256 = "SHA-256";

    @Value("${bot.key}")
    private String key;

    @SneakyThrows
    public void checkTelegramData(Map<String, String> request) {
        var requestCopy = new HashMap<>(request);
        String hash = requestCopy.get("hash");
        requestCopy.remove("hash");
        requestCopy.remove("code");

        String str = requestCopy.entrySet().stream()
                                .sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
                                .map(kvp -> kvp.getKey() + "=" + kvp.getValue())
                                .collect(Collectors.joining("\n"));

        var spec = new SecretKeySpec(MessageDigest.getInstance(SHA_256).digest(key.getBytes(StandardCharsets.UTF_8)),
                                     HMAC_SHA_256);
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(spec);

        var result = mac.doFinal(str.getBytes(StandardCharsets.UTF_8));
        String resultStr = bytesToHex(result);

        if (hash.compareToIgnoreCase(resultStr) != 0) {
            throw new RuntimeException();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        var hexChars = new char[bytes.length * 2];
        for (var j = 0; j < bytes.length; j++) {
            var v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}
