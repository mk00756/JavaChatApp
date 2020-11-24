package org.com1028.cw2;

import static org.junit.Assert.*;
import org.junit.Test;

public class AESTest {
	
	String secret = "q23%rew";
	String msg = "Test message";
	String eMsg = "S8WVf5rQm/frNo7ADCCWyg==";
	
	@Test
	public void encryptTest() {
		assertEquals("Not encrypted", AES.encrypt(msg, secret), eMsg);
	}
	
	@Test
	public void decryptTest() {
		assertEquals("Not decrypted", AES.decrypt(eMsg, secret), msg);
		
	}

}
