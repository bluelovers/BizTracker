/**
 * 
 */
package com.xiaolei.android.common;

import android.text.TextUtils;

/**
 * @author xiaolei
 * 
 */
public final class PasswordStrengthChecker {
	private static PasswordStrengthChecker instance = new PasswordStrengthChecker();
	private String alpha = "abcdefghijklmnopqrstuvwxyz";
	private String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private String upper_punct = "~`!@#$%^&*()-_+=";
	private String digits = "1234567890";

	private int totalChars = 0x7f - 0x20;
	private int alphaChars = alpha.length();
	private int upperChars = upper.length();
	private int upper_punctChars = upper_punct.length();
	private int digitChars = digits.length();
	private int otherChars = totalChars
			- (alphaChars + upperChars + upper_punctChars + digitChars);

	public PasswordStrengthChecker() {

	}

	public static PasswordStrengthChecker getInstance() {
		return instance;
	}

	private double calculateBits(String passWord) {
		if (TextUtils.isEmpty(passWord)) {
			return 0;
		}

		Boolean fAlpha = false;
		Boolean fUpper = false;
		Boolean fUpperPunct = false;
		Boolean fDigit = false;
		Boolean fOther = false;
		int charset = 0;

		for (int i = 0; i < passWord.length(); i++) {
			char chr = passWord.charAt(i);

			if (alpha.indexOf(chr) != -1)
				fAlpha = true;
			else if (upper.indexOf(chr) != -1)
				fUpper = true;
			else if (digits.indexOf(chr) != -1)
				fDigit = true;
			else if (upper_punct.indexOf(chr) != -1)
				fUpperPunct = true;
			else
				fOther = true;
		}

		if (fAlpha)
			charset += alphaChars;
		if (fUpper)
			charset += upperChars;
		if (fDigit)
			charset += digitChars;
		if (fUpperPunct)
			charset += upper_punctChars;
		if (fOther)
			charset += otherChars;

		double bits = Math.log(charset) * (passWord.length() / Math.log(2));

		return Math.floor(bits);
	}

	public PasswordStrength EvaluatePasswordStrength(String plaintextPassword) {
		PasswordStrength result = PasswordStrength.NOT_RATED;
		if (TextUtils.isEmpty(plaintextPassword)) {
			return result;
		}

		double bits = calculateBits(plaintextPassword);

		if (bits >= 128) {
			return PasswordStrength.BEST;
		} else if (bits < 128 && bits >= 64) {
			return PasswordStrength.STRONG;
		} else if (bits < 64 && bits >= 56) {
			return PasswordStrength.MEDIUM;
		} else if (bits < 56) {
			return PasswordStrength.WEAK;
		} else {
			return PasswordStrength.NOT_RATED;
		}
	}

	public enum PasswordStrength {
		NOT_RATED, WEAK, MEDIUM, STRONG, BEST
	}
}
