import burp.config.KeysModel;
import com.google.gson.Gson;
import one.d4d.signsaboteur.itsdangerous.Algorithms;
import one.d4d.signsaboteur.itsdangerous.Derivation;
import one.d4d.signsaboteur.itsdangerous.MessageDerivation;
import one.d4d.signsaboteur.itsdangerous.MessageDigestAlgorithm;
import one.d4d.signsaboteur.keys.SecretKey;
import one.d4d.signsaboteur.utils.GsonHelper;
import one.d4d.signsaboteur.utils.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KeyPersistenceStoreTest {
    @Test
    void KeyDerivationTest() {
        KeysModel model = new KeysModel();
        model.setSalts(Utils.readResourceForClass("/salts", this.getClass()));
        model.setSecrets(Utils.readResourceForClass("/secrets", this.getClass()));
        model.addKey(new SecretKey("test", "secret","salt",".", Algorithms.SHA1, Derivation.HMAC, MessageDerivation.NONE, MessageDigestAlgorithm.SHA1));

        Gson gson = GsonHelper.customGson;
        String serial = gson.toJson(model);
        KeysModel restoredModel = gson.fromJson(serial, KeysModel.class);
        assertEquals(model.getSigningKeys().size(), restoredModel.getSigningKeys().size());

    }
}
