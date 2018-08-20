package org.groundwork.foundation.ws.impl;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestJasyptUtils {

    @Test
    public void testDecryptableRandomPasswords() {
        for (int i = 0; i < 1000; i++) {
            String randomString = JasyptUtils.genRandomString(JasyptUtils.RANDOM_LENGTH);
            assertTrue(JasyptUtils.jasyptDecrypt(JasyptUtils.jasyptEncrypt(randomString)).equals(randomString));
        }
    }

}
