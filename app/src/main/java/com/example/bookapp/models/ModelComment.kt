package com.example.bookapp.models

class ModelComment {
    // should match as in firebase
    var bookId: String = ""
    var comment: String = ""
    var id: String = ""
    var timestamp: String = ""
    var uid: String = ""

    constructor(){}
    constructor(bookId: String, comment: String, id: String, timestamp: String, uid: String) {
        this.bookId = bookId
        this.comment = comment
        this.id = id
        this.timestamp = timestamp
        this.uid = uid
    }


}