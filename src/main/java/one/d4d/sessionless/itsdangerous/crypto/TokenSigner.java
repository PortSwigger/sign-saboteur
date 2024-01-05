package one.d4d.sessionless.itsdangerous.crypto;

import com.google.common.primitives.Bytes;
import one.d4d.sessionless.itsdangerous.*;
import one.d4d.sessionless.keys.SecretKey;
import one.d4d.sessionless.utils.Utils;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public class TokenSigner implements Cloneable {
    public Algorithms digestMethod = Algorithms.SHA1;
    public Derivation keyDerivation = Derivation.HMAC;
    public MessageDerivation messageDerivation = MessageDerivation.NONE;
    public MessageDigestAlgorithm messageDigestAlgorithm = MessageDigestAlgorithm.SHA1;
    public byte[] secret_key;
    public byte[] salt = "itsdangerous.Signer".getBytes();
    public byte sep;

    public TokenSigner(Algorithms digestMethod, byte[] secret_key, byte sep) {
        this.digestMethod = digestMethod;
        this.secret_key = secret_key;
        this.sep = sep;
    }

    public TokenSigner(byte[] secret_key, byte sep) {
        this.secret_key = secret_key;
        this.sep = sep;
    }

    public TokenSigner(SecretKey key) {
        this.digestMethod = key.getDigestMethod();
        this.keyDerivation = key.getKeyDerivation();
        this.messageDerivation = key.getMessageDerivation();
        this.secret_key = key.getSecret().getBytes();
        this.salt = key.getSalt().getBytes();
        this.sep = key.getSeparator().getBytes().length > 0 ? key.getSeparator().getBytes()[0] : 46;
        this.messageDigestAlgorithm = key.getMessageDigestAlgorythm();
    }

    public TokenSigner(Algorithms digestMethod, Derivation keyDerivation, byte[] secret_key, byte[] salt, byte sep) {
        this.digestMethod = digestMethod;
        this.keyDerivation = keyDerivation;
        this.secret_key = secret_key;
        this.salt = salt;
        this.sep = sep;
    }

    public TokenSigner(Algorithms digestMethod, Derivation keyDerivation, MessageDerivation messageDerivation, MessageDigestAlgorithm digest, byte[] secret_key, byte[] salt, byte sep) {
        this.digestMethod = digestMethod;
        this.keyDerivation = keyDerivation;
        this.messageDerivation = messageDerivation;
        this.messageDigestAlgorithm = digest;
        this.secret_key = secret_key;
        this.salt = salt;
        this.sep = sep;
    }

    public Algorithms getDigestMethod() {
        return digestMethod;
    }

    public void setDigestMethod(Algorithms digestMethod) {
        this.digestMethod = digestMethod;
    }

    public Derivation getKeyDerivation() {
        return keyDerivation;
    }

    public void setKeyDerivation(Derivation keyDerivation) {
        this.keyDerivation = keyDerivation;
    }

    public void setMessageDerivation(MessageDerivation messageDerivation) {
        this.messageDerivation = messageDerivation;
    }

    public MessageDerivation getMessageDerivation() {
        return messageDerivation;
    }

    public MessageDigestAlgorithm getMessageDigestAlgorythm() {
        return messageDigestAlgorithm;
    }


    public byte getSep() {
        return sep;
    }

    public void setSep(byte sep) {
        this.sep = sep;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getSecretKey() {
        return secret_key;
    }

    public void setSecretKey(byte[] secret_key) {
        this.secret_key = secret_key;
    }

    public void setMessageDigestAlgorithm(MessageDigestAlgorithm messageDigestAlgorithm) {
        this.messageDigestAlgorithm = messageDigestAlgorithm;
    }

    public byte[] derive_message(byte[] value) {
        switch (messageDerivation){
            case TORNADO -> {
                return Bytes.concat(value, new byte[] {sep});
            }
            case CONCAT -> {
                return Bytes.concat(Utils.split(value, sep));
            }
            default -> {
                return value;
            }
        }
    }
    public byte[] derive_key() throws DerivationException {
        try {
            if (messageDigestAlgorithm == MessageDigestAlgorithm.NONE) return secret_key;
            MessageDigest msdDigest = MessageDigest.getInstance(messageDigestAlgorithm.name);
            switch (keyDerivation) {
                case PBKDF2HMAC -> {
                    KeySpec spec = new PBEKeySpec(
                            (new String(secret_key)).toCharArray(),
                            salt,
                            100000,
                            8 * 32
                    );
                    SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                    return f.generateSecret(spec).getEncoded();
                }
                case HASH -> {
                    msdDigest.update(secret_key);
                    return msdDigest.digest();
                }
                case CONCAT -> {
                    msdDigest.update(Bytes.concat(salt, secret_key));
                    return msdDigest.digest();
                }
                case DJANGO -> {
                    msdDigest.update(Bytes.concat(salt, "signer".getBytes(), secret_key));
                    return msdDigest.digest();
                }
                case HMAC -> {
                    SecretKeySpec signingKey = new SecretKeySpec(secret_key, digestMethod.name);
                    Mac mac = Mac.getInstance(digestMethod.name);
                    mac.init(signingKey);
                    return mac.doFinal(salt);
                }
                case NONE -> {
                    return secret_key;
                }
                default -> throw new DerivationException("Unknown key derivation method");
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new DerivationException("No such derivation algorithm");
        } catch (InvalidKeyException e) {
            throw new DerivationException("Invalid derivation key");
        }
    }

    public byte[] get_signature(byte[] value) {
        try {
            byte[] key = derive_key();
            SecretKeySpec signingKey = new SecretKeySpec(key, digestMethod.name);
            Mac mac = Mac.getInstance(digestMethod.name);
            mac.init(signingKey);
            byte[] sig = mac.doFinal(value);
            return Base64.getUrlEncoder().withoutPadding().encode(sig);
        } catch (Exception e) {
            return new byte[]{};
        }
    }

    public byte[] get_signature_unsafe(byte[] value) throws Exception {
        byte[] key = derive_key();
        SecretKeySpec signingKey = new SecretKeySpec(key, digestMethod.name);
        Mac mac = Mac.getInstance(digestMethod.name);
        mac.init(signingKey);
        byte[] sig = mac.doFinal(value);
        return Base64.getUrlEncoder().withoutPadding().encode(sig);
    }

    public byte[] get_signature_bytes(byte[] value) {
        try {
            byte[] message = derive_message(value);
            byte[] key = derive_key();
            SecretKeySpec signingKey = new SecretKeySpec(key, digestMethod.name);
            Mac mac = Mac.getInstance(digestMethod.name);
            mac.init(signingKey);
            return mac.doFinal(message);
        } catch (Exception e) {
            return new byte[]{};
        }
    }

    public byte[] sign(byte[] value) {
        return Bytes.concat(value, new byte[]{sep}, get_signature(value));
    }

    public boolean verify_signature(byte[] value, byte[] sign) {
        try {
            byte[] signature = Base64.getUrlDecoder().decode(sign);
            byte[] expected = get_signature_bytes(value);
            return Arrays.equals(expected, signature);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verify_signature_bytes(byte[] value, byte[] sign) {
        try {
            byte[] expected = get_signature_bytes(value);
            return Arrays.equals(expected, sign);
        } catch (Exception e) {
            return false;
        }
    }

    public byte[] unsign(byte[] value) throws BadSignatureException {
        int i = Bytes.lastIndexOf(value, sep);
        byte[] message = Arrays.copyOfRange(value, 0, i);
        byte[] signature = Arrays.copyOfRange(value, i + 1, value.length);
        return fast_unsign(message, signature);
    }

    public byte[] fast_unsign(byte[] message, byte[] signature) throws BadSignatureException {
        byte[] sign = Utils.normalization(signature);
        switch (sign.length) {
            case 28 -> digestMethod = Algorithms.SHA224;
            case 32 -> digestMethod = Algorithms.SHA256;
            case 48 -> digestMethod = Algorithms.SHA384;
            case 64 -> digestMethod = Algorithms.SHA512;
            default -> digestMethod = Algorithms.SHA1;
        }
        if (verify_signature_bytes(message, sign))
            return message;
        throw new BadSignatureException("Signature didn't match");
    }


    public SecretKey getKey() {
        return new SecretKey(
                UUID.randomUUID().toString(),
                new String(secret_key),
                new String(salt),
                String.valueOf((char) sep),
                digestMethod,
                keyDerivation,
                messageDerivation, messageDigestAlgorithm);
    }

    @Override
    public TokenSigner clone() {
        try {
            TokenSigner clone = (TokenSigner) super.clone();
            clone.setDigestMethod(clone.getDigestMethod());
            clone.setKeyDerivation(clone.getKeyDerivation());
            clone.setMessageDerivation(clone.getMessageDerivation());
            clone.setMessageDigestAlgorithm(clone.getMessageDigestAlgorythm());
            clone.setSep(clone.getSep());
            clone.setSecretKey(clone.getSecretKey());
            clone.setSalt(clone.getSalt());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
