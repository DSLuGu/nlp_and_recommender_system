package com.example;

public class NewsVO {

    private String id;
    private String url;
    private String section;
    private String subSection;
    private String title;
    private String summary;
    private String dttm;
    private String media;
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSubSection() {
        return subSection;
    }

    public void setSubSection(String subSection) {
        this.subSection = subSection;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDttm() {
        return dttm;
    }

    public void setDttm(String dttm) {
        this.dttm = dttm;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {

        return "NewsVO {" +
                "id='" + id + "'" +
                ", url='" + url + "'" +
                ", section='" + section + "'" +
                ", subSection='" + subSection + "'" +
                ", title='" + title + "'" +
                ", summary='" + summary + "'" +
                ", dttm='" + dttm + "'" +
                ", media='" + media + "'" +
                ", content='" + content + "'" +
                "}";

    }

}
