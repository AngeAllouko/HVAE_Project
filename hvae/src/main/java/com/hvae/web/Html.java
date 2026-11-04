package com.hvae.web;

import com.hvae.model.Outcome;

/** Small view helpers used by the JSP pages. */
public final class Html {
    private Html() { }

    /** Escapes text for safe output inside HTML (prevents XSS). */
    public static String esc(Object o) {
        if (o == null) return "";
        String s = o.toString();
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String badge(Outcome o) {
        return "<span class=\"badge " + o.getCssClass() + "\">" + esc(o.getLabel()) + "</span>";
    }

    public static String badgeByName(String outcomeName) {
        return badge(Outcome.valueOf(outcomeName));
    }
}
