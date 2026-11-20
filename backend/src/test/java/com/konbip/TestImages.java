package com.konbip;

import java.util.Base64;

public final class TestImages {

	/** 1x1 투명 PNG */
	public static final byte[] PNG = Base64.getDecoder().decode(
			"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");

	private TestImages() {
	}
}
