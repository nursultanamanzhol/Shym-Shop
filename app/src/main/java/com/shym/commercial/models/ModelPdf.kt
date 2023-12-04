package com.shym.commercial.models

class ModelPdf {


    //variable
    var uid: String = ""
    var id: String = ""
    var title: String = ""
    var price: String = ""
    var discount: String = ""
    var description: String = ""
    var categoryId: String = ""
    var url: String = ""
    var timestamp: Long = 0
    var viewsCount: Long = 0
    var downloadsCount: Long = 0

    //empty constructor(required by firebase)
    constructor()

    //parameterized constructor

    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        price: String,
        categoryId: String,
        url: String,
        timestamp: Long,
        viewsCount: Long,
        downloadsCount: Long
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.price = price
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.downloadsCount = downloadsCount
    }


}