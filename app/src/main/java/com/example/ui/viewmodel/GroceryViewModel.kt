package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.LoyaltyProfile
import com.example.data.model.NotificationItem
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.SupportMessage
import com.example.data.repository.GroceryRepository
import com.example.localization.AppLanguage
import com.example.localization.LanguageManager
import com.example.localization.LocalizedStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    SEARCH_FILTER,
    CART_CHECKOUT,
    ORDER_TRACKING,
    LOYALTY_CLUB,
    ADMIN_INVENTORY,
    SUPPORT_CHAT,
    NOTIFICATION_SETTINGS
}

enum class SortOption {
    POPULARITY,
    PRICE_LOW_HIGH,
    PRICE_HIGH_LOW,
    DISCOUNT
}

class GroceryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = GroceryRepository(db.groceryDao(), viewModelScope)

    // Navigation & Theme
    val currentScreen = MutableStateFlow(Screen.HOME)
    val screenBackStack = mutableListOf<Screen>()

    val isDarkMode = MutableStateFlow(false)
    val selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val strings: StateFlow<LocalizedStrings> = combine(selectedLanguage) { (lang) ->
        LanguageManager.getStrings(lang)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LanguageManager.getStrings(AppLanguage.ENGLISH))

    // Products & Categories
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCategory = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")
    val vegOnlyFilter = MutableStateFlow(false)
    val sortOption = MutableStateFlow(SortOption.POPULARITY)

    // Filtered Products
    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        selectedCategory,
        searchQuery,
        vegOnlyFilter,
        sortOption
    ) { products, cat, query, vegOnly, sort ->
        products.filter { p ->
            val matchesCategory = cat == null || p.category.equals(cat, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.hindiName.contains(query, ignoreCase = true) ||
                    p.category.contains(query, ignoreCase = true)
            val matchesVeg = !vegOnly || p.isVeg
            matchesCategory && matchesQuery && matchesVeg
        }.let { list ->
            when (sort) {
                SortOption.POPULARITY -> list.sortedByDescending { it.rating }
                SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
                SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
                SortOption.DISCOUNT -> list.sortedByDescending { it.discountPercent }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Items & Computation
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartDetailed: StateFlow<List<Pair<Product, Int>>> = combine(
        allProducts,
        cartItems
    ) { products, items ->
        items.mapNotNull { item ->
            val product = products.find { it.id == item.productId }
            if (product != null) Pair(product, item.quantity) else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartTotalCount: StateFlow<Int> = combine(cartItems) { items ->
        items[0].sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartItemTotal: StateFlow<Double> = combine(cartDetailed) { detailed ->
        detailed[0].sumOf { it.first.price * it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Delivery & Fees
    val selectedTip = MutableStateFlow(20.0) // 0.0, 20.0, 30.0, 50.0
    val appliedCoupon = MutableStateFlow<String?>(null)
    val couponDiscount = MutableStateFlow(0.0)
    val platformFee = MutableStateFlow(5.0)

    val deliveryFee: StateFlow<Double> = combine(cartItemTotal) { (total) ->
        if (total >= 199.0 || total == 0.0) 0.0 else 25.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25.0)

    val grandTotal: StateFlow<Double> = combine(
        cartItemTotal,
        deliveryFee,
        platformFee,
        selectedTip,
        couponDiscount
    ) { itemTotal, delivery, platform, tip, discount ->
        if (itemTotal == 0.0) 0.0
        else (itemTotal + delivery + platform + tip - discount).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Orders & Tracking
    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeOrder = MutableStateFlow<Order?>(null)

    // Loyalty Program
    val loyaltyProfile: StateFlow<LoyaltyProfile> = repository.loyaltyProfile
    val lastScratchedAmount = MutableStateFlow<Int?>(null)

    // Notifications & Settings
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationFrequency = MutableStateFlow("Instant Alerts")
    val pushAlertsEnabled = MutableStateFlow(true)
    val orderAlertsEnabled = MutableStateFlow(true)
    val promoAlertsEnabled = MutableStateFlow(true)
    val vibrationEnabled = MutableStateFlow(true)

    // Customer Support Chat
    val supportMessages: StateFlow<List<SupportMessage>> = repository.supportMessages
    val currentChatInput = MutableStateFlow("")

    // Payment & Auth Modal
    val selectedPaymentMethod = MutableStateFlow("UPI") // "UPI", "CARD", "NETBANKING", "COD", "BLINKPAY"
    val showOtpDialog = MutableStateFlow(false)
    val enteredOtp = MutableStateFlow("")
    val isAuthenticating = MutableStateFlow(false)

    // Location / Address
    val currentDeliveryAddress = MutableStateFlow("Flat 402, Green Meadows, Sector 45 (10 mins away)")

    // Navigation Helper
    fun navigateTo(screen: Screen) {
        if (currentScreen.value != screen) {
            screenBackStack.add(currentScreen.value)
            currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (screenBackStack.isNotEmpty()) {
            val prev = screenBackStack.removeAt(screenBackStack.size - 1)
            currentScreen.value = prev
            true
        } else {
            false
        }
    }

    // Cart actions
    fun addToCart(productId: String) {
        viewModelScope.launch {
            repository.addToCart(productId)
        }
    }

    fun decrementCart(productId: String) {
        viewModelScope.launch {
            repository.decrementCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun applyCoupon(code: String): Boolean {
        return when (code.trim().uppercase()) {
            "WELCOME50" -> {
                appliedCoupon.value = "WELCOME50"
                couponDiscount.value = 50.0
                true
            }
            "BLINKIT100" -> {
                if (cartItemTotal.value >= 399) {
                    appliedCoupon.value = "BLINKIT100"
                    couponDiscount.value = 100.0
                    true
                } else {
                    false
                }
            }
            "FREEDEL" -> {
                appliedCoupon.value = "FREEDEL"
                couponDiscount.value = deliveryFee.value
                true
            }
            else -> false
        }
    }

    fun removeCoupon() {
        appliedCoupon.value = null
        couponDiscount.value = 0.0
    }

    // Checkout & Payment execution
    fun initiateCheckout() {
        showOtpDialog.value = true
        enteredOtp.value = "7492" // prefilled for seamless demo testing
    }

    fun confirmPaymentAndPlaceOrder() {
        viewModelScope.launch {
            isAuthenticating.value = true
            kotlinx.coroutines.delay(1200)
            isAuthenticating.value = false
            showOtpDialog.value = false

            val placed = repository.placeOrder(
                items = cartDetailed.value,
                itemTotal = cartItemTotal.value,
                deliveryFee = deliveryFee.value,
                platformFee = platformFee.value,
                discount = couponDiscount.value,
                tip = selectedTip.value,
                paymentMethod = selectedPaymentMethod.value,
                address = currentDeliveryAddress.value
            )
            activeOrder.value = placed
            appliedCoupon.value = null
            couponDiscount.value = 0.0
            navigateTo(Screen.ORDER_TRACKING)
        }
    }

    // Admin Controls
    fun adminUpdateStock(productId: String, delta: Int) {
        viewModelScope.launch {
            val prod = allProducts.value.find { it.id == productId } ?: return@launch
            val newStock = (prod.stockQuantity + delta).coerceAtLeast(0)
            repository.updateProductStock(productId, newStock)
        }
    }

    fun adminToggleStock(productId: String) {
        viewModelScope.launch {
            val prod = allProducts.value.find { it.id == productId } ?: return@launch
            val newStock = if (prod.stockQuantity > 0) 0 else 50
            repository.updateProductStock(productId, newStock)
        }
    }

    fun adminChangeOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            repository.updateAdminOrderStatus(orderId, status)
            if (activeOrder.value?.orderId == orderId) {
                activeOrder.value = activeOrder.value?.copy(status = status)
            }
        }
    }

    // Loyalty Scratch Card
    fun scratchLoyaltyCard(): Int {
        val won = listOf(25, 50, 75, 100, 150).random()
        repository.scratchCardWin(won)
        lastScratchedAmount.value = won
        return won
    }

    // Support Chat
    fun sendChatMessage(text: String) {
        if (text.isNotBlank()) {
            repository.sendSupportMessage(text)
            currentChatInput.value = ""
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }
}
