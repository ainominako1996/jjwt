package io.jsonwebtoken.impl.security;

import io.jsonwebtoken.impl.crypto.EllipticCurveProvider;
import io.jsonwebtoken.lang.Assert;
import io.jsonwebtoken.security.AsymmetricKeySignatureAlgorithm;
import io.jsonwebtoken.security.CryptoRequest;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.VerifySignatureRequest;
import io.jsonwebtoken.security.WeakKeyException;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECKey;
import java.security.spec.ECGenParameterSpec;

@SuppressWarnings("unused") //used via reflection in the io.jsonwebtoken.security.SignatureAlgorithms class
public class EllipticCurveSignatureAlgorithm extends AbstractSignatureAlgorithm implements AsymmetricKeySignatureAlgorithm {

    private static final String EC_PUBLIC_KEY_REQD_MSG =
        "Elliptic Curve signature validation requires an ECPublicKey instance.";

    private static final int MIN_KEY_LENGTH_BITS = 256;

    private final String curveName;

    private final int minKeyLength; //in bits

    private final int signatureLength;

    public EllipticCurveSignatureAlgorithm(String name, String jcaName, String curveName, int minKeyLength, int signatureLength) {
        super(name, jcaName);
        Assert.hasText(curveName, "Curve name cannot be null or empty.");
        this.curveName = curveName;
        if (minKeyLength < MIN_KEY_LENGTH_BITS) {
            String msg = "minKeyLength bits must be greater than the JWA mandatory minimum key length of " + MIN_KEY_LENGTH_BITS;
            throw new IllegalArgumentException(msg);
        }
        this.minKeyLength = minKeyLength;
        Assert.isTrue(signatureLength > 0, "signatureLength must be greater than zero.");
        this.signatureLength = signatureLength;
    }

    @Override
    public KeyPair generateKeyPair() {
        KeyPairGenerator keyGenerator;
        try {
            keyGenerator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec spec = new ECGenParameterSpec(this.curveName);
            keyGenerator.initialize(spec, Randoms.secureRandom());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to obtain an EllipticCurve KeyPairGenerator: " + e.getMessage(), e);
        }
        return keyGenerator.genKeyPair();
    }

    @Override
    protected void validateKey(Key key, boolean signing) {

        if (!(key instanceof ECKey)) {
            String msg = "EC " + keyType(signing) + " keys must be an ECKey.  The specified key is of type: " +
                key.getClass().getName();
            throw new InvalidKeyException(msg);
        }

        if (signing) {
            // https://github.com/jwtk/jjwt/issues/68
            // Instead of checking for an instance of ECPrivateKey, check for PrivateKey (and ECKey assertion is above):
            if (!(key instanceof PrivateKey)) {
                String msg = "Asymmetric key signatures must be created with PrivateKeys. The specified key is of type: " +
                    key.getClass().getName();
                throw new InvalidKeyException(msg);
            }
        } else { //verification
            if (!(key instanceof PublicKey)) {
                throw new InvalidKeyException(EC_PUBLIC_KEY_REQD_MSG);
            }
        }

        final String name = getName();
        ECKey ecKey = (ECKey) key;
        int size = ecKey.getParams().getOrder().bitLength();
        if (size < this.minKeyLength) {
            String msg = "The " + keyType(signing) + " key's size (ECParameterSpec order) is " + size +
                " bits which is not secure enough for the " + name + " algorithm.  The JWT " +
                "JWA Specification (RFC 7518, Section 3.4) states that keys used with " +
                name + " MUST have a size >= " + this.minKeyLength +
                " bits.  Consider using the SignatureAlgorithms." + name + ".generateKeyPair() " +
                "method to create a key pair guaranteed to be secure enough for " + name + ".  See " +
                "https://tools.ietf.org/html/rfc7518#section-3.4 for more information.";
            throw new WeakKeyException(msg);
        }
    }

    @Override
    protected byte[] doSign(CryptoRequest<byte[], Key> request) throws Exception {
        PrivateKey privateKey = (PrivateKey) request.getKey();
        Signature sig = createSignatureInstance(request.getProvider(), null);
        sig.initSign(privateKey);
        sig.update(request.getData());
        return EllipticCurveProvider.transcodeSignatureToConcat(sig.sign(), signatureLength);
    }

    @Override
    protected boolean doVerify(VerifySignatureRequest request) throws Exception {
        final Key key = request.getKey();
        if (key instanceof PrivateKey) {
            return super.doVerify(request);
        }

        PublicKey publicKey = (PublicKey) key;
        Signature sig = createSignatureInstance(request.getProvider(), null);
        byte[] signature = request.getSignature();
        /*
         * If the expected size is not valid for JOSE, fall back to ASN.1 DER signature.
         * This fallback is for backwards compatibility ONLY (to support tokens generated by previous versions of jjwt)
         * and backwards compatibility will possibly be removed in a future version of this library.
         */
        byte[] derSignature = this.signatureLength != signature.length && signature[0] == 0x30 ? signature : EllipticCurveProvider.transcodeSignatureToDER(signature);
        sig.initVerify(publicKey);
        sig.update(request.getData());
        return sig.verify(derSignature);
    }
}
