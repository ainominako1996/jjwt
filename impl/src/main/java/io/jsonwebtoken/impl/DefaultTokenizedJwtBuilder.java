package io.jsonwebtoken.impl;

import io.jsonwebtoken.MalformedJwtException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultTokenizedJwtBuilder implements TokenizedJwtBuilder {

    private final List<String> tokens = new ArrayList<>(5);

    @Override
    public TokenizedJwtBuilder append(String token) {
        tokens.add(token);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends TokenizedJwt> T build() {

        int size = tokens.size();

        if (size != 3 && size != 5) {
            String msg = "Invalid compact JWT serialization: JWSs must have exactly two periods, and " +
                "JWEs must have exactly 4 periods.  Found: " + (size - 1);
            throw new MalformedJwtException(msg);
        }

        Iterator<String> iterator = tokens.iterator();

        if (size == 3) {
            return (T) new DefaultTokenizedJwt(iterator.next(), iterator.next(), iterator.next());
        }

        return (T) new DefaultTokenizedJwe(iterator.next(), iterator.next(), iterator.next(), iterator.next(), iterator.next());
    }
}
