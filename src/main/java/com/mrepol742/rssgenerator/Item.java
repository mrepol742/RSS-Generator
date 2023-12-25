package com.mrepol742.rssgenerator;

public class Item {
    String title;
    String description;
    String link;
    String medium_url;

    public Item(String title, String description, String link, String medium_url) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.medium_url = medium_url;
    }
}