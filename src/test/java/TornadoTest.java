import one.d4d.signsaboteur.itsdangerous.Attack;
import one.d4d.signsaboteur.itsdangerous.BruteForce;
import one.d4d.signsaboteur.itsdangerous.crypto.TornadoTokenSigner;
import one.d4d.signsaboteur.itsdangerous.model.SignedToken;
import one.d4d.signsaboteur.itsdangerous.model.SignedTokenObjectFinder;
import one.d4d.signsaboteur.itsdangerous.model.TornadoSignedToken;
import one.d4d.signsaboteur.keys.SecretKey;
import one.d4d.signsaboteur.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TornadoTest {

    @Test
    void TornadoParserTest() {
        byte[] secret = "secret".getBytes();
        byte[] sep = new byte[]{(byte) '|'};
        String value = "2|1:0|10:1686150202|7:session|4:e30=|5e05eeef41715bc4b109138f00a37bbc580ca7e94ba9a21d5ec062b7aebff557";
        Optional<SignedToken> optionalToken = SignedTokenObjectFinder.parseTornadoSignedToken("test", value);
        if (optionalToken.isPresent()) {
            TornadoSignedToken token = (TornadoSignedToken) optionalToken.get();
            TornadoTokenSigner s = new TornadoTokenSigner(secret, sep);
            token.setSigner(s);
            Assertions.assertDoesNotThrow(() -> {
                s.unsign(value.getBytes());
            });
        } else {
            Assertions.fail("Token not found.");
        }
    }

    @Test
    void BruteForceMultiThreatTornado() {
        String value = "2|1:0|10:1686150202|7:session|4:e30=|5e05eeef41715bc4b109138f00a37bbc580ca7e94ba9a21d5ec062b7aebff557";
        Assertions.assertDoesNotThrow(() -> {
            Optional<SignedToken> optionalToken = SignedTokenObjectFinder.parseTornadoSignedToken("test", value);
            if (optionalToken.isPresent()) {
                TornadoTokenSigner s = new TornadoTokenSigner();
                optionalToken.get().setSigner(s);
                final Set<String> secrets = Utils.readResourceForClass("/secrets", this.getClass());
                final Set<String> salts = Utils.readResourceForClass("/salts", this.getClass());
                final List<SecretKey> knownKeys = new ArrayList<>();

                BruteForce bf = new BruteForce(secrets, salts, knownKeys, Attack.FAST, optionalToken.get());
                SecretKey sk = bf.parallel();
                Assertions.assertNotNull(sk);
            }

        });
    }
}
