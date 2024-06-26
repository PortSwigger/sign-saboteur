package one.d4d.signsaboteur.itsdangerous.crypto;

import one.d4d.signsaboteur.itsdangerous.Algorithms;
import one.d4d.signsaboteur.itsdangerous.Derivation;
import one.d4d.signsaboteur.itsdangerous.MessageDerivation;
import one.d4d.signsaboteur.itsdangerous.MessageDigestAlgorithm;
import one.d4d.signsaboteur.keys.SecretKey;

import java.util.EnumSet;

public class JSONWebSignatureTokenSigner extends TokenSigner {

    public JSONWebSignatureTokenSigner(SecretKey key) {
        super(key);
        this.keyDerivation = Derivation.NONE;
        this.messageDigestAlgorithm = MessageDigestAlgorithm.NONE;
        this.knownDerivations = EnumSet.of(Derivation.NONE);
    }

    public JSONWebSignatureTokenSigner(byte[] sep) {
        super(Algorithms.SHA256, Derivation.NONE, MessageDerivation.NONE, MessageDigestAlgorithm.NONE, new byte[]{}, new byte[]{}, sep);
        this.knownDerivations = EnumSet.of(Derivation.NONE);
    }
}
