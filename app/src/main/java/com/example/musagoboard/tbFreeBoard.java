package com.example.musagoboard;

public class tbFreeBoard {
    int seqNo;
    String subject;
    String author;
//    String content;
//    String fileImg;
//    String issueDate;
    String  regDate;

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }


    //public tbFreeBoard(int seqNo, String subject, String author, String content, String fileImg, String regDate, String issueDate) {
        public tbFreeBoard(int seqNo, String subject, String author,  String regDate) {
        this.seqNo = seqNo;
        this.subject = subject;
        this.author = author;
//        this.content = content;
//        this.fileImg = fileImg;
        this.regDate = regDate;
//        this.issueDate = issueDate;
    }
}
