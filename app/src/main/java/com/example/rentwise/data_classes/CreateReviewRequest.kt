package com.example.rentwise.data_classes

data class CreateReviewRequest(
   val rating: Int?,
    val comment: String?
)

data class ReviewResponse(
    val message: String?
)

