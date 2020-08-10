package io.jsonwebtoken.impl.security


import io.jsonwebtoken.security.AeadIvEncryptionResult
import io.jsonwebtoken.security.EncryptionAlgorithms
import io.jsonwebtoken.security.SignatureException
import org.junit.Test

import javax.crypto.SecretKey

/**
 * @since JJWT_RELEASE_VERSION
 */
class HmacAesEncryptionAlgorithmTest {

    @Test(expected = SignatureException)
    void testDecryptWithInvalidTag() {

        def alg = EncryptionAlgorithms.A128CBC_HS256;

        SecretKey key = alg.generateKey()

        def plaintext = "Hello World! Nice to meet you!".getBytes("UTF-8")

        def req = new DefaultEncryptionRequest(plaintext, key, null, null, null, null)
        def result = alg.encrypt(req);
        assert result instanceof AeadIvEncryptionResult

        def realTag = result.getAuthenticationTag();

        //fake it:
        def fakeTag = new byte[realTag.length]
        Randoms.secureRandom().nextBytes(fakeTag)

        def dreq = new DefaultAeadIvRequest(result.getCiphertext(), key, null, null, result.getInitializationVector(), null, fakeTag)
        alg.decrypt(dreq)
    }
}
