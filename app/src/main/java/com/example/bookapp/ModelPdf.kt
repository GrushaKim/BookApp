package com.example.bookapp

class ModelPdf{
    // vars
    var uid: String = ""
    var id: String = ""
    var title: String = ""
    var description: String = ""
    var categoryId: String = ""
    var url: String = ""
    var timestamp: Long = 0
    var viewCnt: Long = 0
    var downloadCnt: Long = 0

    // constructor
    constructor()
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        url: String,
        timestamp: Long,
        viewCnt: Long,
        downloadCnt: Long
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewCnt = viewCnt
        this.downloadCnt = downloadCnt
    }

    // parameterized constructor

}
