package io.jsonwebtoken.impl.security;

import io.jsonwebtoken.Named;
import io.jsonwebtoken.lang.Assert;
import io.jsonwebtoken.security.CryptoRequest;

import java.security.SecureRandom;

abstract class CryptoAlgorithm implements Named {

    private final String name;

    private final String jcaName;

    CryptoAlgorithm(String name, String jcaName) {
        Assert.hasText(name, "name cannot be null or empty.");
        this.name = name;
        assert this.name != null;
        Assert.hasText(jcaName, "jcaName cannot be null or empty.");
        this.jcaName = jcaName;
        assert this.jcaName != null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    String getJcaName() {
        return this.jcaName;
    }

    SecureRandom ensureSecureRandom(CryptoRequest request) {
        SecureRandom random = request.getSecureRandom();
        return random != null ? random : Randoms.secureRandom();
    }
}
