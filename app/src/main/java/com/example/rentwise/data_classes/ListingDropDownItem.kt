package com.example.rentwise.data_classes

data class ListingDropDownItem(
    val id: String,
    val name: String
) {
    // Ensures the dropdown displays the name of the item
    override fun toString(): String {
        return name
    }
}
