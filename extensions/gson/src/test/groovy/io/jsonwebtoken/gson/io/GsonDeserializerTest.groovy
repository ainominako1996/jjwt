/*
 * Copyright (C) 2014 jsonwebtoken.io
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
package io.jsonwebtoken.gson.io

import com.google.gson.Gson
import io.jsonwebtoken.io.DeserializationException
import io.jsonwebtoken.io.Deserializer
import io.jsonwebtoken.lang.Strings
import org.junit.Test

import java.text.DecimalFormat
import java.text.NumberFormat

import static org.easymock.EasyMock.*
import static org.junit.Assert.*
import static org.hamcrest.CoreMatchers.instanceOf

class GsonDeserializerTest {

    @Test
    void loadService() {
        def deserializer = ServiceLoader.load(Deserializer).iterator().next()
        assertThat(deserializer, instanceOf(GsonDeserializer))
    }

    @Test
    void testDefaultConstructor() {
        def deserializer = new GsonDeserializer()
        assertNotNull deserializer.gson
    }

    @Test
    void testObjectMapperConstructor() {
        def customGSON = new Gson()
        def deserializer = new GsonDeserializer(customGSON)
        assertSame customGSON, deserializer.gson
    }

    @Test(expected = IllegalArgumentException)
    void testObjectMapperConstructorWithNullArgument() {
        new GsonDeserializer<>(null)
    }

    @Test
    void testDeserialize() {
        byte[] serialized = '{"hello":"世界"}'.getBytes(Strings.UTF_8)
        def expected = [hello: '世界']
        def result = new GsonDeserializer().deserialize(serialized)
        assertEquals expected, result
    }

    @Test
    void testDeserializeFailsWithJsonProcessingException() {

        def ex = createMock(java.io.IOException)

        expect(ex.getMessage()).andReturn('foo')

        def deserializer = new GsonDeserializer() {
            @Override
            protected Object readValue(byte[] bytes) throws java.io.IOException {
                throw ex
            }
        }

        replay ex

        try {
            deserializer.deserialize('{"hello":"世界"}'.getBytes(Strings.UTF_8))
            fail()
        } catch (DeserializationException se) {
            assertEquals 'Unable to deserialize bytes into a java.lang.Object instance: foo', se.getMessage()
            assertSame ex, se.getCause()
        }

        verify ex
    }

    private static boolean canBeDouble(Number number) {
        return number instanceof BigDecimal &&
                number.compareTo(BigDecimal.valueOf(Double.MIN_VALUE)) >= 0 &&
                number.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) <= 0
    }

    private static boolean canBeFloat(Number number) {
        return number instanceof BigDecimal &&
                number.compareTo(BigDecimal.valueOf(Float.MIN_VALUE)) >= 0 &&
                number.compareTo(BigDecimal.valueOf(Float.MAX_VALUE)) <= 0
    }

    private static boolean canBeLong(Number number) {
        return number instanceof BigInteger &&
                number.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0 &&
                number.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0
    }

    private static boolean canBeInt(Number number) {
        return number instanceof BigInteger &&
                number.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0 &&
                number.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0
    }

    private static String getIdealType(Number number) {
        if (canBeInt(number)) {
            return "Integer"
        } else if (canBeLong(number)) {
            return "Long"
        } else if (canBeFloat(number)) {
            return "Float"
        } else if (canBeDouble(number)) {
            return "Double"
        } else if (number instanceof BigInteger) {
            return "BigInteger";
        } else {
            return "BigDecimal";
        }
    }

    private static void println(Number number) {
        String sval;
        if (number instanceof BigInteger) {
            NumberFormat format = DecimalFormat.getIntegerInstance(Locale.US)
            format.setGroupingUsed(false)
            sval = format.format(number)
        } else {
            sval = ((BigDecimal)number).toPlainString()
        }
        println getIdealType(number) + ": " + sval
    }

    @Test
    void testLimits() {
        println BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE)     //less than Long can handle
        println BigInteger.valueOf(Long.MIN_VALUE)                              //min Long can handle
        println BigInteger.valueOf(Long.MIN_VALUE).add(BigInteger.ONE)          //just before Long min
        println BigInteger.valueOf(Integer.MIN_VALUE).subtract(BigInteger.ONE)  //less than Integer can handle
        println BigInteger.valueOf(Integer.MIN_VALUE)                           //min Integer
        println BigInteger.valueOf(Integer.MIN_VALUE).add(BigInteger.ONE)       //just before Integer min
        println BigInteger.valueOf(Integer.MAX_VALUE).subtract(BigInteger.ONE)  //just before Integer ax
        println BigInteger.valueOf(Integer.MAX_VALUE)                           //max Integer
        println BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE)       //more than Integer can handle
        println BigInteger.valueOf(Long.MAX_VALUE).subtract(BigInteger.ONE)     //just before Long max
        println BigInteger.valueOf(Long.MAX_VALUE)                              //max Long can handle
        println BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)          //more than Long can handle
        println ''
        println BigDecimal.valueOf(Double.MIN_VALUE).subtract(BigDecimal.valueOf(0.0000000001d))
        println BigDecimal.valueOf(Double.MIN_VALUE)
        println BigDecimal.valueOf(Float.MIN_VALUE).subtract(new BigDecimal(0.00000000000000000000000000000000000000000000000000000001d))
        println BigDecimal.valueOf(Float.MIN_VALUE)
        println BigDecimal.valueOf(Float.MAX_VALUE)
        println BigDecimal.valueOf(Float.MAX_VALUE).add(new BigDecimal(0.00000000000000000000000000000000000000000000000000000001d))
        println BigDecimal.valueOf(Double.MAX_VALUE)
        println BigDecimal.valueOf(Double.MAX_VALUE).add(BigDecimal.valueOf(0.0000000001d))

        int ival = 5 as int;

        long lval = new Long(ival)

        println "lval: $lval"
    }
}
