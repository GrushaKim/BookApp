package com.example.bookapp.models

class ModelComment {
    // should match as in firebase
    var bookId: String = ""
    var comment: String = ""
    var id: String = ""
    var timestamp: Long = 0
    var uid: String = ""

    constructor(){}
    constructor(bookId: String, comment: String, id: String, timestamp: Long, uid: String) {
        this.bookId = bookId
        this.comment = comment
        this.id = id
        this.timestamp = timestamp
        this.uid = uid
    }


}