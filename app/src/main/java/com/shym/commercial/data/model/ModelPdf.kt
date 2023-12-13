package com.shym.commercial.data.model

import org.intellij.lang.annotations.Language

class ModelPdf {


    //variable
    var uid: String = ""
    var id: String = ""
    var title: String = ""
    var price: Int = 0
    var discount: Int = 0
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
        price: Int,
        discount: Int,
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
        this.discount = discount
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.downloadsCount = downloadsCount
    }


}