
package com.fujun.browser.model;

public class NaviItem {

    public String title;
    public String url;
    public int color;

    public NaviItem() {
    }

    public NaviItem(NaviItem item) {
        title = item.title;
        url = item.url;
        color = item.color;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[ title = " + title)
                .append("; url = " + url)
                .append("; color = " + color + " ]");
        return buffer.toString();
    }
}
