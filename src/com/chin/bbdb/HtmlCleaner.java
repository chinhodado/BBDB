package com.chin.bbdb;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

public class HtmlCleaner {
    static void cleanHtml(Element content) {
        content.select("script").remove();               // remove <script> tags
        content.select("noscript").remove();             // remove <noscript> tags
        content.select("sup").remove();                  // remove the sup tags

        removeComments(content);                         // remove comments
    }

    private static void removeComments(Node node) {
        for (int i = 0; i < node.childNodes().size();) {
            Node child = node.childNode(i);
            if (child.nodeName().equals("#comment"))
                child.remove();
            else {
                removeComments(child);
                i++;
            }
        }
    }
}
