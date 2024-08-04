package com.example.cp670_multilingual_ocr;

public class HelpItem {
    private String detail;
    private int imageResId;

    public HelpItem(String detail, int imageResId) {
        this.detail = detail;
        this.imageResId = imageResId;
    }

    public String getDetail() {
        return detail;
    }

    public int getImageResId() {
        return imageResId;
    }
}