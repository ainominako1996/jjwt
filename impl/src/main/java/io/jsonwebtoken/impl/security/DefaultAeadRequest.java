/*
 * Copyright (C) 2016 jsonwebtoken.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jsonwebtoken.impl.security;

import io.jsonwebtoken.lang.Assert;
import io.jsonwebtoken.security.AeadRequest;

import java.security.Key;
import java.security.Provider;
import java.security.SecureRandom;

/**
 * @since JJWT_RELEASE_VERSION
 */
class DefaultAeadRequest<T, K extends Key> extends DefaultCryptoRequest<T, K> implements AeadRequest<T, K> {

    private final byte[] aad;

    DefaultAeadRequest(T data, K key, Provider provider, SecureRandom secureRandom, byte[] aad) {
        super(data, key, provider, secureRandom);
        this.aad = Assert.notNull(aad, "Additional Authenticated Data byte array cannot be null.");
    }

    @Override
    public byte[] getAssociatedData() {
        return this.aad;
    }
}
