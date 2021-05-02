package com.stocking.modules.buythen;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class NewsRes implements Serializable {
    private static final long serialVersionUID = 6281770904300640185L;
    
    private String lastBuildDate;
    private long total;
    private int start;
    private int display;
    private List<Item> items;
    
    @Data
    public static class Item implements Serializable  {
        private static final long serialVersionUID = 5566600873154194032L;
        private String title;
        private String originallink;
        private String link;
        private String description;
        private String pubDate;
    }
}
