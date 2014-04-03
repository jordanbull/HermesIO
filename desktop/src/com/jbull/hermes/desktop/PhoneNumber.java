package com.jbull.hermes.desktop;


public class PhoneNumber {
    // TODO: set code
    private static String DEFAULT_COUNTRY_CODE = "1";

    public static String format(String s) {
        String replaced = s.replaceAll("[^0-9#]", "");
        if (replaced.contains("#")) {
            return replaced;
        }
        if (replaced.length() > 10) {
            String countryCode = replaced.substring(0,replaced.length()-10);
            String rest = replaced.substring(replaced.length()-10);
            return countryCode + " " + formatRegular(rest);
        } else if (replaced.length() == 10) {
            return DEFAULT_COUNTRY_CODE + " " + formatRegular(replaced);
        } else if (replaced.length() == 7) {
            return String.format("%s-%s", s.substring(0, 3), s.substring(3, 6));
        } else {
            return s;
        }
    }

    private static String formatRegular(String s) {
        assert s.length() == 10;
        return String.format("(%s) %s-%s", s.substring(0, 3), s.substring(3, 6),
                s.substring(6, 10));
    }
}
