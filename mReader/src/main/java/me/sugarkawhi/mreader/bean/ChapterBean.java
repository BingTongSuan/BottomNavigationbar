package me.sugarkawhi.mreader.bean;

import java.io.Serializable;

/**
 * 章节
 * Created by ZhaoZongyao on 2018/1/11.
 */

public class ChapterBean implements Serializable {
    private String bookName;
    private String chapterName;
    private String chapterContent;
    //是否是第一章
    private boolean isFirstChapter;
    //是否是最后一章
    private boolean isLastChapter;
    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getChapterContent() {
        return chapterContent;
    }

    public void setChapterContent(String chapterContent) {
        this.chapterContent = chapterContent;
    }
}
