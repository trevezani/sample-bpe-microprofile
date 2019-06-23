package br.com.trevezani.bpeqrcode.util;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StringUtils {

    public String lpadTo(String input, int width, char ch) {
        String strPad = "";

        StringBuffer sb = new StringBuffer(input.trim());

        while (sb.length() < width)
            sb.insert(0, ch);
        strPad = sb.toString();

        if (strPad.length() > width) {
            strPad = strPad.substring(0, width);
        }
        return strPad;
    }

}
