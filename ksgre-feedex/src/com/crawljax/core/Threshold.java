package com.crawljax.core;

/**
 * KSG中涉及到的阈值
 */
public enum Threshold {

    /**
     * w(pre)
     * 用于事件相似度排序中，判断某个事件，是和当前关键词更相似，还是和上一个关键词更相似
     */
    INTERVAL_W(0.6),

    /**
     * 用于试探步骤中累计状态和关键词的相似度
     */
    TRY_W(0.7),

    /**
     * 试探步骤中比较状态和关键词语义相似度的两个阈值
     */
    T1(0.2),
    T2(0.6),

    /**
     * w(ss)
     * 状态与关键词语义相似度
     */
    SS_W(0.6),

    /**
     * 状态相似性比较（结构相似度，内容相似度）
     */
    T3(0.8),
    T4(0.8);

    private double value;

    Threshold(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
