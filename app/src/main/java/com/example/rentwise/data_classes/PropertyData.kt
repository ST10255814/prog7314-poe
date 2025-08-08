package com.example.rentwise.data_classes

import java.io.Serializable

data class PropertyData(
    val imageResId: Int,
    val title: String,
    val address: String,
    val label1: String,
    val label2: String,
    val price: String,
    val isFavourite: Boolean
) : Serializable
