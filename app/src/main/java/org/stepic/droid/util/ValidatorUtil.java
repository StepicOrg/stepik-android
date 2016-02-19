package org.stepic.droid.util;

public class ValidatorUtil {
    public static final int MIN_PASSWORD_LENGTH = 6;

    public static boolean isPasswordValid(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean isPasswordLengthValid(int passwordLength){
        return passwordLength >= MIN_PASSWORD_LENGTH;
    }

}