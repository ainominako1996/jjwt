package io.jsonwebtoken.security

import static org.junit.Assert.*
import org.junit.Test

class SignatureAlgorithmsTest {

    @Test
    void testForNameCaseInsensitive() {
        for(SignatureAlgorithm alg : SignatureAlgorithms.STANDARD_ALGORITHMS.values()) {
            assertSame alg, SignatureAlgorithms.forName(alg.getName().toLowerCase())
        }
    }
}
