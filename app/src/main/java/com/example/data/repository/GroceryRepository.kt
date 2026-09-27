package com.example.data.repository

import com.example.data.local.GroceryDao
import com.example.data.model.CartItem
import com.example.data.model.LoyaltyProfile
import com.example.data.model.NotificationItem
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.SupportMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class GroceryRepository(
    private val groceryDao: GroceryDao,
    private val scope: CoroutineScope
) {
    val allProducts: Flow<List<Product>> = groceryDao.getAllProducts()
    val cartItems: Flow<List<CartItem>> = groceryDao.getCartItems()
    val allOrders: Flow<List<Order>> = groceryDao.getAllOrders()
    val notifications: Flow<List<NotificationItem>> = groceryDao.getAllNotifications()

    private val _loyaltyProfile = MutableStateFlow(
        LoyaltyProfile(
            coins = 520,
            tier = "Gold Saver",
            nextTierCoinsRequired = 80,
            freeDeliveriesLeft = 5,
            scratchCardsAvailable = 2,
            lifetimeSavings = 1920.0
        )
    )
    val loyaltyProfile: StateFlow<LoyaltyProfile> = _loyaltyProfile.asStateFlow()

    private val _supportMessages = MutableStateFlow<List<SupportMessage>>(
        listOf(
            SupportMessage(
                id = "m1",
                text = "Namaste! Welcome to BlinkCart 24x7 Express Support. How can we help you today?",
                isUser = false,
                quickReplies = listOf(
                    "Where is my order?",
                    "Item missing or damaged",
                    "Refund status",
                    "Change delivery address",
                    "Talk to Live Agent"
                )
            )
        )
    )
    val supportMessages: StateFlow<List<SupportMessage>> = _supportMessages.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            val existing = groceryDao.getAllProducts().first()
            if (existing.isEmpty()) {
                seedInitialProducts()
                seedInitialNotifications()
            }
        }
    }

    suspend fun addToCart(productId: String) {
        val currentItems = groceryDao.getCartItems().first()
        val current = currentItems.find { it.productId == productId }
        val newQuantity = (current?.quantity ?: 0) + 1
        groceryDao.insertOrUpdateCart(CartItem(productId, newQuantity))
    }

    suspend fun decrementCart(productId: String) {
        val currentItems = groceryDao.getCartItems().first()
        val current = currentItems.find { it.productId == productId }
        if (current != null) {
            if (current.quantity > 1) {
                groceryDao.insertOrUpdateCart(CartItem(productId, current.quantity - 1))
            } else {
                groceryDao.removeCartItem(productId)
            }
        }
    }

    suspend fun removeFromCart(productId: String) {
        groceryDao.removeCartItem(productId)
    }

    suspend fun clearCart() {
        groceryDao.clearCart()
    }

    suspend fun updateProductStock(productId: String, newStock: Int) {
        groceryDao.updateProductStock(productId, newStock)
    }

    suspend fun placeOrder(
        items: List<Pair<Product, Int>>,
        itemTotal: Double,
        deliveryFee: Double,
        platformFee: Double,
        discount: Double,
        tip: Double,
        paymentMethod: String,
        address: String
    ): Order {
        val orderId = "ORD-" + System.currentTimeMillis().toString().takeLast(6)
        val itemsSummary = items.joinToString(", ") { "${it.first.name} (x${it.second})" }
        val totalPaid = (itemTotal + deliveryFee + platformFee + tip - discount).coerceAtLeast(0.0)

        val newOrder = Order(
            orderId = orderId,
            itemsSummary = itemsSummary,
            itemCount = items.sumOf { it.second },
            itemTotal = itemTotal,
            deliveryFee = deliveryFee,
            platformFee = platformFee,
            discount = discount,
            tip = tip,
            totalPaid = totalPaid,
            paymentMethod = paymentMethod,
            status = OrderStatus.PLACED,
            etaMinutes = 10,
            riderName = "Vikram Sharma",
            riderPhone = "+91 98112 34567",
            riderVehicleNumber = "DL 4C BK 9821",
            deliveryAddress = address,
            deliveryOtp = ((1000..9999).random()).toString()
        )

        groceryDao.insertOrder(newOrder)
        groceryDao.clearCart()

        // Award loyalty coins (10 coins per ₹100 spent)
        val earnedCoins = (totalPaid / 10).toInt()
        _loyaltyProfile.value = _loyaltyProfile.value.copy(
            coins = _loyaltyProfile.value.coins + earnedCoins,
            scratchCardsAvailable = _loyaltyProfile.value.scratchCardsAvailable + 1,
            lifetimeSavings = _loyaltyProfile.value.lifetimeSavings + discount
        )

        // Add Notification
        groceryDao.insertNotification(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = "Order Confirmed! (#$orderId)",
                message = "Your 10-minute delivery is being packed at Dark Store Sector 45.",
                type = "ORDER"
            )
        )

        // Launch simulated real-time progression
        simulateOrderLifecycle(orderId)

        return newOrder
    }

    private fun simulateOrderLifecycle(orderId: String) {
        scope.launch(Dispatchers.IO) {
            delay(12000) // 12s -> Packed
            groceryDao.updateOrderStatus(orderId, OrderStatus.PACKED)
            groceryDao.insertNotification(
                NotificationItem(
                    id = UUID.randomUUID().toString(),
                    title = "Order Packed (#$orderId)",
                    message = "Packed carefully at dark store with quality seal.",
                    type = "ORDER"
                )
            )

            delay(12000) // 12s -> Assigned
            groceryDao.updateOrderStatus(orderId, OrderStatus.ASSIGNED)
            groceryDao.insertNotification(
                NotificationItem(
                    id = UUID.randomUUID().toString(),
                    title = "Rider Assigned (#$orderId)",
                    message = "Vikram Sharma (DL 4C BK 9821) has picked up your order.",
                    type = "ORDER"
                )
            )

            delay(14000) // 14s -> Out for Delivery
            groceryDao.updateOrderStatus(orderId, OrderStatus.OUT_FOR_DELIVERY)
            groceryDao.insertNotification(
                NotificationItem(
                    id = UUID.randomUUID().toString(),
                    title = "Out for Delivery! (#$orderId)",
                    message = "Your rider is 3 minutes away from your location.",
                    type = "ORDER"
                )
            )

            delay(18000) // 18s -> Delivered
            groceryDao.updateOrderStatus(orderId, OrderStatus.DELIVERED)
            groceryDao.insertNotification(
                NotificationItem(
                    id = UUID.randomUUID().toString(),
                    title = "Order Delivered! (#$orderId)",
                    message = "Delivered in record time. Enjoy your fresh groceries!",
                    type = "ORDER"
                )
            )
        }
    }

    suspend fun updateAdminOrderStatus(orderId: String, status: OrderStatus) {
        groceryDao.updateOrderStatus(orderId, status)
        groceryDao.insertNotification(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = "Order Status Updated (#$orderId)",
                message = "Status changed to: ${status.name}",
                type = "ORDER"
            )
        )
    }

    fun scratchCardWin(winAmount: Int) {
        _loyaltyProfile.value = _loyaltyProfile.value.copy(
            coins = _loyaltyProfile.value.coins + winAmount,
            scratchCardsAvailable = (_loyaltyProfile.value.scratchCardsAvailable - 1).coerceAtLeast(0)
        )
    }

    fun sendSupportMessage(userText: String) {
        val userMsg = SupportMessage(
            id = UUID.randomUUID().toString(),
            text = userText,
            isUser = true
        )
        _supportMessages.value = _supportMessages.value + userMsg

        scope.launch {
            delay(1000)
            val botReply = generateSupportReply(userText)
            val replyMsg = SupportMessage(
                id = UUID.randomUUID().toString(),
                text = botReply.first,
                isUser = false,
                quickReplies = botReply.second
            )
            _supportMessages.value = _supportMessages.value + replyMsg
        }
    }

    private fun generateSupportReply(query: String): Pair<String, List<String>> {
        val lower = query.lowercase()
        return when {
            lower.contains("where") || lower.contains("track") || lower.contains("order") -> {
                "Your latest order is being routed in real-time by your delivery rider. You can track their live location in the 'Live Tracking' tab with an instant 10-minute guarantee." to listOf("View Live Map", "Call Rider", "Cancel Order")
            }
            lower.contains("missing") || lower.contains("damaged") -> {
                "We sincerely apologize! Under the BlinkCart Instant Freshness Guarantee, our system can initiate an instant refund of ₹120 or dispatch a replacement within 10 minutes." to listOf("Initiate Instant Refund", "Send Replacement", "Upload Photo")
            }
            lower.contains("refund") -> {
                "Your recent refund of ₹140 has been processed to your original payment mode (UPI/Bank) and should reflect in 5-15 minutes." to listOf("Check Bank UTR", "Download Invoice", "Need More Help")
            }
            lower.contains("agent") || lower.contains("human") -> {
                "Connecting you to Rahul from BlinkCart Premium Support... You are #1 in queue. Estimated wait time: 30 seconds." to listOf("Wait for Agent", "Leave a Callback")
            }
            else -> {
                "Thank you for contacting BlinkCart. Our dark store team is working 24x7 to ensure lightning fast grocery delivery. Can we assist with anything else?" to listOf("Track Order", "Offers & Coupons", "Talk to Live Agent")
            }
        }
    }

    suspend fun markAllNotificationsRead() {
        groceryDao.markAllAsRead()
    }

    private suspend fun seedInitialNotifications() {
        groceryDao.insertNotification(
            NotificationItem(
                id = "notif_1",
                title = "⚡ Flash Deal: 50% OFF Fresh Fruits",
                message = "Handpicked organic apples, bananas & oranges starting at ₹29!",
                type = "PROMO"
            )
        )
        groceryDao.insertNotification(
            NotificationItem(
                id = "notif_2",
                title = "🎉 150 Blink Coins Credited!",
                message = "Welcome to BlinkCart Gold Saver. Enjoy free 10-min delivery on orders above ₹149.",
                type = "LOYALTY"
            )
        )
    }

    private suspend fun seedInitialProducts() {
        val catalog = listOf(
            // Vegetables & Fruits
            Product(
                id = "veg_1",
                name = "Fresh Hybrid Tomatoes",
                hindiName = "ताज़ा टमाटर",
                category = "Vegetables & Fruits",
                price = 24.0,
                originalPrice = 35.0,
                discountPercent = 31,
                unit = "500 g",
                isVeg = true,
                stockQuantity = 45,
                rating = 4.8f,
                description = "Plump, vine-ripened tomatoes sourced directly from local polyhouses."
            ),
            Product(
                id = "veg_2",
                name = "Fresh Green Coriander (Dhaniya)",
                hindiName = "हरा धनिया",
                category = "Vegetables & Fruits",
                price = 9.0,
                originalPrice = 18.0,
                discountPercent = 50,
                unit = "100 g",
                isVeg = true,
                stockQuantity = 60,
                rating = 4.7f,
                description = "Aromatic, hydroponically grown fresh coriander leaves."
            ),
            Product(
                id = "veg_3",
                name = "Farm Fresh Onions (Pyaz)",
                hindiName = "ताज़ा प्याज़",
                category = "Vegetables & Fruits",
                price = 38.0,
                originalPrice = 52.0,
                discountPercent = 26,
                unit = "1 kg",
                isVeg = true,
                stockQuantity = 80,
                rating = 4.6f,
                description = "Grade-A Nashik red onions with robust flavor and crunch."
            ),
            Product(
                id = "veg_4",
                name = "Golden Potatoes (Aloo)",
                hindiName = "आलू",
                category = "Vegetables & Fruits",
                price = 28.0,
                originalPrice = 40.0,
                discountPercent = 30,
                unit = "1 kg",
                isVeg = true,
                stockQuantity = 75,
                rating = 4.7f,
                description = "Clean, unblemished all-purpose potatoes perfect for curries and fries."
            ),
            Product(
                id = "veg_5",
                name = "Fresh Cavendish Bananas",
                hindiName = "केला",
                category = "Vegetables & Fruits",
                price = 39.0,
                originalPrice = 55.0,
                discountPercent = 29,
                unit = "Pack of 6",
                isVeg = true,
                stockQuantity = 35,
                rating = 4.9f,
                description = "Sweet, naturally ripened bananas rich in potassium and quick energy."
            ),
            Product(
                id = "veg_6",
                name = "Royal Gala Apples",
                hindiName = "सेब",
                category = "Vegetables & Fruits",
                price = 129.0,
                originalPrice = 179.0,
                discountPercent = 27,
                unit = "Pack of 4",
                isVeg = true,
                stockQuantity = 30,
                rating = 4.8f,
                description = "Crisp, sweet imported apples with vibrant red blush."
            ),

            // Dairy & Bread
            Product(
                id = "dairy_1",
                name = "Amul Taaza Homogenised Toned Milk",
                hindiName = "अमूल ताज़ा दूध",
                category = "Dairy & Bread",
                price = 27.0,
                originalPrice = 28.0,
                discountPercent = 4,
                unit = "500 ml",
                isVeg = true,
                stockQuantity = 90,
                rating = 4.9f,
                description = "Pure, fresh pasteurized toned milk from India's most trusted dairy."
            ),
            Product(
                id = "dairy_2",
                name = "Mother Dairy Classic Malai Paneer",
                hindiName = "पनीर",
                category = "Dairy & Bread",
                price = 92.0,
                originalPrice = 100.0,
                discountPercent = 8,
                unit = "200 g",
                isVeg = true,
                stockQuantity = 40,
                rating = 4.9f,
                description = "Soft, melt-in-the-mouth cottage cheese packed with protein."
            ),
            Product(
                id = "dairy_3",
                name = "Harvest Gold 100% Atta Whole Wheat Bread",
                hindiName = "ब्राउन ब्रेड",
                category = "Dairy & Bread",
                price = 50.0,
                originalPrice = 55.0,
                discountPercent = 9,
                unit = "400 g",
                isVeg = true,
                stockQuantity = 35,
                rating = 4.8f,
                description = "Wholesome bread crafted with 100% whole wheat flour, zero maida."
            ),
            Product(
                id = "dairy_4",
                name = "Amul Salted Butter",
                hindiName = "अमूल मक्खन",
                category = "Dairy & Bread",
                price = 56.0,
                originalPrice = 58.0,
                discountPercent = 3,
                unit = "100 g",
                isVeg = true,
                stockQuantity = 50,
                rating = 5.0f,
                description = "The classic Utterly Butterly Delicious Indian breakfast butter."
            ),
            Product(
                id = "dairy_5",
                name = "Farm Fresh Brown Eggs",
                hindiName = "अंडे (ब्राउन)",
                category = "Dairy & Bread",
                price = 65.0,
                originalPrice = 80.0,
                discountPercent = 18,
                unit = "Pack of 6",
                isVeg = false,
                stockQuantity = 40,
                rating = 4.7f,
                description = "Free-range nutrient-rich antibiotic-free brown eggs."
            ),

            // Snacks & Munchies
            Product(
                id = "snack_1",
                name = "Lay's India's Magic Masala Chips",
                hindiName = "लेज़ मैजिक मसाला",
                category = "Snacks & Munchies",
                price = 20.0,
                originalPrice = 20.0,
                discountPercent = 0,
                unit = "48 g",
                isVeg = true,
                stockQuantity = 100,
                rating = 4.9f,
                description = "Crispy potato chips coated in tangy, spicy Indian masala blend."
            ),
            Product(
                id = "snack_2",
                name = "Haldiram's Nagpur Aloo Bhujia",
                hindiName = "हल्दीराम आलू भुजिया",
                category = "Snacks & Munchies",
                price = 58.0,
                originalPrice = 65.0,
                discountPercent = 10,
                unit = "200 g",
                isVeg = true,
                stockQuantity = 65,
                rating = 4.9f,
                description = "Spicy potato noodle snack spiced with mint and dry mango powder."
            ),
            Product(
                id = "snack_3",
                name = "Cadbury Dairy Milk Silk Chocolate",
                hindiName = "सिल्क चॉकलेट",
                category = "Snacks & Munchies",
                price = 78.0,
                originalPrice = 90.0,
                discountPercent = 13,
                unit = "60 g",
                isVeg = true,
                stockQuantity = 55,
                rating = 5.0f,
                description = "Velvety smooth milk chocolate that melts seamlessly on your tongue."
            ),
            Product(
                id = "snack_4",
                name = "Britannia Good Day Cashew Cookies",
                hindiName = "गुड डे कुकीज़",
                category = "Snacks & Munchies",
                price = 38.0,
                originalPrice = 45.0,
                discountPercent = 15,
                unit = "200 g",
                isVeg = true,
                stockQuantity = 60,
                rating = 4.8f,
                description = "Rich buttery cookies sprinkled with crunchy roasted cashews."
            ),

            // Instant Food
            Product(
                id = "inst_1",
                name = "Maggi 2-Minute Masala Noodles",
                hindiName = "मैगी 2 मिनट नूडल्स",
                category = "Instant Food",
                price = 56.0,
                originalPrice = 60.0,
                discountPercent = 7,
                unit = "Pack of 4 (280 g)",
                isVeg = true,
                stockQuantity = 120,
                rating = 4.9f,
                description = "India's favorite noodle with the signature aromatic spice mix."
            ),
            Product(
                id = "inst_2",
                name = "Tata Sampann High Protein Chana Dal",
                hindiName = "चना दाल",
                category = "Instant Food",
                price = 108.0,
                originalPrice = 130.0,
                discountPercent = 16,
                unit = "1 kg",
                isVeg = true,
                stockQuantity = 40,
                rating = 4.7f,
                description = "Unpolished chana dal retaining natural goodness and dietary fiber."
            ),
            Product(
                id = "inst_3",
                name = "Aashirvaad Superior Shudh Chakki Atta",
                hindiName = "आशीर्वाद शुद्ध चक्की आटा",
                category = "Instant Food",
                price = 245.0,
                originalPrice = 285.0,
                discountPercent = 14,
                unit = "5 kg",
                isVeg = true,
                stockQuantity = 50,
                rating = 4.9f,
                description = "100% whole wheat flour stone ground for soft, fluffy rotis."
            ),
            Product(
                id = "inst_4",
                name = "Fortune Sunlite Refined Sunflower Oil",
                hindiName = "फॉर्च्यून रिफाइंड तेल",
                category = "Instant Food",
                price = 142.0,
                originalPrice = 175.0,
                discountPercent = 18,
                unit = "1 L pouch",
                isVeg = true,
                stockQuantity = 45,
                rating = 4.8f,
                description = "Light and healthy cooking oil enriched with Vitamins A & D."
            ),

            // Beverages & Cold Drinks
            Product(
                id = "bev_1",
                name = "Coca-Cola Original Taste",
                hindiName = "कोका-कोला",
                category = "Beverages & Cold Drinks",
                price = 40.0,
                originalPrice = 40.0,
                discountPercent = 0,
                unit = "750 ml",
                isVeg = true,
                stockQuantity = 80,
                rating = 4.8f,
                description = "Crisp, effervescent classic cola beverage served chilled."
            ),
            Product(
                id = "bev_2",
                name = "Tata Tea Gold Rich Taste",
                hindiName = "टाटा टी गोल्ड चाय",
                category = "Beverages & Cold Drinks",
                price = 295.0,
                originalPrice = 345.0,
                discountPercent = 14,
                unit = "500 g",
                isVeg = true,
                stockQuantity = 35,
                rating = 4.9f,
                description = "Signature blend of Assam CTC teas with 15% gently rolled long leaves."
            ),
            Product(
                id = "bev_3",
                name = "Real Fruit Power Mixed Fruit Juice",
                hindiName = "मिक्स्ड फ्रूट जूस",
                category = "Beverages & Cold Drinks",
                price = 118.0,
                originalPrice = 135.0,
                discountPercent = 12,
                unit = "1 L",
                isVeg = true,
                stockQuantity = 40,
                rating = 4.7f,
                description = "Delicious blend of 9 handpicked fruits packed with Vitamin C."
            ),

            // Household Essentials
            Product(
                id = "house_1",
                name = "Surf Excel Matic Top Load Liquid Detergent",
                hindiName = "सर्फ एक्सेल लिक्विड",
                category = "Household Essentials",
                price = 215.0,
                originalPrice = 245.0,
                discountPercent = 12,
                unit = "1 L",
                isVeg = true,
                stockQuantity = 30,
                rating = 4.9f,
                description = "Superior stain removal in washing machines with fresh fragrance."
            ),
            Product(
                id = "house_2",
                name = "Dettol Original Germ Protection Handwash",
                hindiName = "डेटॉल हैंडवॉश रिफिल",
                category = "Household Essentials",
                price = 119.0,
                originalPrice = 149.0,
                discountPercent = 20,
                unit = "750 ml Refill",
                isVeg = true,
                stockQuantity = 50,
                rating = 4.9f,
                description = "100% better protection against illness-causing germs with pine fragrance."
            ),
            Product(
                id = "house_3",
                name = "Colgate MaxFresh Peppermint Ice Toothpaste",
                hindiName = "कोलगेट मैक्सफ्रेश",
                category = "Household Essentials",
                price = 98.0,
                originalPrice = 120.0,
                discountPercent = 18,
                unit = "150 g",
                isVeg = true,
                stockQuantity = 45,
                rating = 4.8f,
                description = "Infused with cooling crystals for intense freshness and cavity protection."
            )
        )

        groceryDao.insertProducts(catalog)
    }
}
