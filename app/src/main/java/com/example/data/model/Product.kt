package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val hindiName: String,
    val category: String, // "Vegetables & Fruits", "Dairy & Bread", "Snacks & Munchies", "Beverages", "Instant Food", "Household Essentials"
    val price: Double,
    val originalPrice: Double,
    val discountPercent: Int,
    val unit: String, // "500 g", "1 L", "1 kg", "Pack of 4"
    val isVeg: Boolean = true,
    var stockQuantity: Int = 50,
    val rating: Float = 4.8f,
    val ratingCount: Int = 1240,
    val deliveryMinutes: Int = 10,
    val badge: String = "FAST DELIVERY",
    val description: String = ""
)
