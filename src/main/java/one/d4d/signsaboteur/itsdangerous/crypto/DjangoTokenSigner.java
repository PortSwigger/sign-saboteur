package one.d4d.signsaboteur.itsdangerous.crypto;

import one.d4d.signsaboteur.itsdangerous.Algorithms;
import one.d4d.signsaboteur.itsdangerous.Derivation;
import one.d4d.signsaboteur.itsdangerous.MessageDerivation;
import one.d4d.signsaboteur.itsdangerous.MessageDigestAlgorithm;
import one.d4d.signsaboteur.keys.SecretKey;

import java.util.EnumSet;

public class DjangoTokenSigner extends DangerousTokenSigner {

    public DjangoTokenSigner(SecretKey key) {
        super(key);
        this.knownDerivations = EnumSet.of(Derivation.DJANGO);
    }

    public DjangoTokenSigner(byte[] secret_key, byte[] salt, byte[] sep) {
        this(Algorithms.SHA1, Derivation.DJANGO, MessageDerivation.NONE, MessageDigestAlgorithm.SHA1, secret_key, salt, sep);
    }

    public DjangoTokenSigner(
            Algorithms algorithm,
            Derivation keyDerivation,
            MessageDerivation messageDerivation,
            MessageDigestAlgorithm digest,
            byte[] secret_key,
            byte[] salt,
            byte[] sep) {
        super(algorithm, keyDerivation, messageDerivation, digest, secret_key, salt, sep);
        this.knownDerivations = EnumSet.of(Derivation.DJANGO);
    }
}
