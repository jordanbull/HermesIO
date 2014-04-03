package com.jbull.hermes.desktop;

import junit.framework.TestCase;

/**
 * Created by Jordan on 4/2/14.
 */
public class PhoneNumberTest extends TestCase {

    public void testFormat() throws Exception {
        final String proper = "(123) 123-4567";
        final String countryCodeProper = "12 (345) 678-9012";
        final String att = "#10";

        final String noFormat = "1231234567";
        assertEquals(proper, PhoneNumber.format(noFormat));

        final String noSpacing = "(123)123-4567";
        assertEquals(proper, PhoneNumber.format(noSpacing));

        final String onlySpaces = "123 123 4567";
        assertEquals(proper, PhoneNumber.format(onlySpaces));

        final String onlyDashes = "123-123-4567";
        assertEquals(proper, PhoneNumber.format(onlyDashes));

        final String countryCodeNoSpace = "12(345)6789012";
        assertEquals(countryCodeProper, PhoneNumber.format(countryCodeNoSpace));

        final String noDelimitCountryCode = "123456789012";
        assertEquals(countryCodeProper, PhoneNumber.format(noDelimitCountryCode));

        assertEquals(att, PhoneNumber.format(att));
    }
}
