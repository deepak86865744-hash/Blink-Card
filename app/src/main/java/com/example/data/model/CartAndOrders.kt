package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: String,
    val quantity: Int
)

enum class OrderStatus {
    PLACED,
    PACKED,
    ASSIGNED,
    OUT_FOR_DELIVERY,
    DELIVERED
}

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val orderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val itemsSummary: String,
    val itemCount: Int,
    val itemTotal: Double,
    val deliveryFee: Double,
    val platformFee: Double,
    val discount: Double,
    val tip: Double,
    val totalPaid: Double,
    val paymentMethod: String,
    val status: OrderStatus = OrderStatus.PLACED,
    val etaMinutes: Int = 10,
    val riderName: String = "Ramesh Kumar",
    val riderPhone: String = "+91 98765 43210",
    val riderVehicleNumber: String = "DL 3S AB 4421",
    val deliveryAddress: String = "Flat 402, Green Meadows, Sector 45",
    val deliveryOtp: String = "4829"
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "ORDER", // "ORDER", "PROMO", "LOYALTY"
    val isRead: Boolean = false
)

data class SupportMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val quickReplies: List<String> = emptyList()
)

data class LoyaltyProfile(
    val coins: Int = 450,
    val tier: String = "Gold Member", // Silver, Gold, Platinum, VIP
    val nextTierCoinsRequired: Int = 150,
    val freeDeliveriesLeft: Int = 8,
    val scratchCardsAvailable: Int = 2,
    val lifetimeSavings: Double = 1850.0
)
