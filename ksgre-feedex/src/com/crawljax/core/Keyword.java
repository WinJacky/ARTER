package com.crawljax.core;

import java.util.ArrayList;
import java.util.List;

public class Keyword {
    private int lineNumber;
    private List<String> keywords;
    private List<String> extendedSeq;

    public Keyword(int lineNumber, List<String> keywords) {
        this.lineNumber = lineNumber;
        this.keywords = keywords;
        this.extendedSeq = new ArrayList<>();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getExtendedSeq() {
        return extendedSeq;
    }

    public void setExtendedSeq(List<String> extendedSeq) {
        this.extendedSeq = extendedSeq;
    }

    public void addToExtendedSeq(String word) {
        this.extendedSeq.add(word);
    }

    public void addToExtendedSeq(List<String> words) {
        this.extendedSeq.addAll(words);
    }
}
