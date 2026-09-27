package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.AuthOtpDialog
import com.example.ui.components.CartBottomBar
import com.example.ui.components.TopAppBarHeader
import com.example.ui.screens.AdminInventoryScreen
import com.example.ui.screens.CartCheckoutScreen
import com.example.ui.screens.CustomerSupportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoyaltyScreen
import com.example.ui.screens.NotificationSettingsScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.SearchFilterScreen
import com.example.ui.theme.BlinkCartTheme
import com.example.ui.viewmodel.GroceryViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GroceryViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            BlinkCartTheme(darkTheme = isDarkMode) {
                BlinkCartApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BlinkCartApp(viewModel: GroceryViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val strings by viewModel.strings.collectAsStateWithLifecycle()

    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartDetailed by viewModel.cartDetailed.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartTotalCount.collectAsStateWithLifecycle()
    val cartTotal by viewModel.cartItemTotal.collectAsStateWithLifecycle()

    val deliveryFee by viewModel.deliveryFee.collectAsStateWithLifecycle()
    val platformFee by viewModel.platformFee.collectAsStateWithLifecycle()
    val couponDiscount by viewModel.couponDiscount.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val selectedTip by viewModel.selectedTip.collectAsStateWithLifecycle()
    val grandTotal by viewModel.grandTotal.collectAsStateWithLifecycle()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()

    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val activeOrder by viewModel.activeOrder.collectAsStateWithLifecycle()
    val loyaltyProfile by viewModel.loyaltyProfile.collectAsStateWithLifecycle()
    val lastScratchedWon by viewModel.lastScratchedAmount.collectAsStateWithLifecycle()

    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val notifFrequency by viewModel.notificationFrequency.collectAsStateWithLifecycle()
    val pushAlerts by viewModel.pushAlertsEnabled.collectAsStateWithLifecycle()
    val orderAlerts by viewModel.orderAlertsEnabled.collectAsStateWithLifecycle()
    val promoAlerts by viewModel.promoAlertsEnabled.collectAsStateWithLifecycle()
    val vibration by viewModel.vibrationEnabled.collectAsStateWithLifecycle()

    val supportMessages by viewModel.supportMessages.collectAsStateWithLifecycle()
    val chatInput by viewModel.currentChatInput.collectAsStateWithLifecycle()

    val showOtpDialog by viewModel.showOtpDialog.collectAsStateWithLifecycle()
    val enteredOtp by viewModel.enteredOtp.collectAsStateWithLifecycle()
    val isAuthenticating by viewModel.isAuthenticating.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val vegOnly by viewModel.vegOnlyFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val address by viewModel.currentDeliveryAddress.collectAsStateWithLifecycle()

    // Android back button handling
    BackHandler(enabled = currentScreen != Screen.HOME) {
        if (!viewModel.navigateBack()) {
            viewModel.navigateTo(Screen.HOME)
        }
    }

    // Top Header visible on non-full-screen modes
    val showTopHeader = currentScreen in listOf(
        Screen.HOME,
        Screen.SEARCH_FILTER,
        Screen.LOYALTY_CLUB,
        Screen.ORDER_TRACKING
    )

    // Bottom Navigation visible on standard tabs
    val showBottomNav = currentScreen in listOf(
        Screen.HOME,
        Screen.SEARCH_FILTER,
        Screen.LOYALTY_CLUB,
        Screen.ORDER_TRACKING,
        Screen.SUPPORT_CHAT
    )

    Scaffold(
        topBar = {
            if (showTopHeader) {
                TopAppBarHeader(
                    strings = strings,
                    address = address,
                    unreadNotifCount = notifications.count { !it.isRead },
                    isDarkMode = isDarkMode,
                    currentLanguage = currentLanguage,
                    onLanguageChange = { viewModel.selectedLanguage.value = it },
                    onToggleDarkMode = { viewModel.isDarkMode.value = !isDarkMode },
                    onNotificationClick = { viewModel.navigateTo(Screen.NOTIFICATION_SETTINGS) },
                    onAdminClick = { viewModel.navigateTo(Screen.ADMIN_INVENTORY) }
                )
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Floating Cart Bar if on Home or Search and cart is not empty
                if (currentScreen in listOf(Screen.HOME, Screen.SEARCH_FILTER) && cartCount > 0) {
                    CartBottomBar(
                        totalItems = cartCount,
                        itemTotal = cartTotal,
                        strings = strings,
                        onViewCartClick = { viewModel.navigateTo(Screen.CART_CHECKOUT) }
                    )
                }

                if (showBottomNav) {
                    AppBottomNavigation(
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> {
                    HomeScreen(
                        products = allProducts,
                        cartItems = cartItems,
                        strings = strings,
                        currentLanguage = currentLanguage,
                        onProductAdd = { viewModel.addToCart(it.id) },
                        onProductDecrement = { viewModel.decrementCart(it.id) },
                        onCategorySelect = { cat ->
                            viewModel.selectedCategory.value = cat
                        },
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }

                Screen.SEARCH_FILTER -> {
                    SearchFilterScreen(
                        products = filteredProducts,
                        cartItems = cartItems,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.searchQuery.value = it },
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.selectedCategory.value = it },
                        isVegOnly = vegOnly,
                        onVegOnlyToggle = { viewModel.vegOnlyFilter.value = it },
                        currentSort = sortOption,
                        onSortChange = { viewModel.sortOption.value = it },
                        strings = strings,
                        currentLanguage = currentLanguage,
                        onProductAdd = { viewModel.addToCart(it.id) },
                        onProductDecrement = { viewModel.decrementCart(it.id) }
                    )
                }

                Screen.CART_CHECKOUT -> {
                    CartCheckoutScreen(
                        cartItems = cartDetailed,
                        itemTotal = cartTotal,
                        deliveryFee = deliveryFee,
                        platformFee = platformFee,
                        couponDiscount = couponDiscount,
                        appliedCoupon = appliedCoupon,
                        selectedTip = selectedTip,
                        grandTotal = grandTotal,
                        deliveryAddress = address,
                        selectedPaymentMethod = selectedPaymentMethod,
                        strings = strings,
                        onAddToCart = { viewModel.addToCart(it.id) },
                        onDecrement = { viewModel.decrementCart(it.id) },
                        onClearCart = { viewModel.clearCart() },
                        onTipSelect = { viewModel.selectedTip.value = it },
                        onApplyCoupon = { viewModel.applyCoupon(it) },
                        onRemoveCoupon = { viewModel.removeCoupon() },
                        onPaymentMethodSelect = { viewModel.selectedPaymentMethod.value = it },
                        onProceedToPay = { viewModel.initiateCheckout() },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.ORDER_TRACKING -> {
                    OrderTrackingScreen(
                        order = activeOrder,
                        allOrders = allOrders,
                        strings = strings,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }

                Screen.LOYALTY_CLUB -> {
                    LoyaltyScreen(
                        profile = loyaltyProfile,
                        lastScratchedWon = lastScratchedWon,
                        strings = strings,
                        onScratchCard = { viewModel.scratchLoyaltyCard() }
                    )
                }

                Screen.ADMIN_INVENTORY -> {
                    AdminInventoryScreen(
                        products = allProducts,
                        orders = allOrders,
                        strings = strings,
                        onStockDelta = { id, delta -> viewModel.adminUpdateStock(id, delta) },
                        onToggleStock = { id -> viewModel.adminToggleStock(id) },
                        onUpdateOrderStatus = { id, status -> viewModel.adminChangeOrderStatus(id, status) },
                        onBackToStore = { viewModel.navigateTo(Screen.HOME) }
                    )
                }

                Screen.SUPPORT_CHAT -> {
                    CustomerSupportScreen(
                        messages = supportMessages,
                        inputText = chatInput,
                        onInputChange = { viewModel.currentChatInput.value = it },
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        strings = strings,
                        onBack = { viewModel.navigateTo(Screen.HOME) }
                    )
                }

                Screen.NOTIFICATION_SETTINGS -> {
                    NotificationSettingsScreen(
                        notifications = notifications,
                        pushEnabled = pushAlerts,
                        onPushToggle = { viewModel.pushAlertsEnabled.value = it },
                        frequency = notifFrequency,
                        onFrequencySelect = { viewModel.notificationFrequency.value = it },
                        orderAlerts = orderAlerts,
                        onOrderAlertsToggle = { viewModel.orderAlertsEnabled.value = it },
                        promoAlerts = promoAlerts,
                        onPromoAlertsToggle = { viewModel.promoAlertsEnabled.value = it },
                        vibration = vibration,
                        onVibrationToggle = { viewModel.vibrationEnabled.value = it },
                        strings = strings,
                        onMarkAllRead = { viewModel.markNotificationsRead() },
                        onBack = { viewModel.navigateBack() }
                    )
                }
            }
        }

        // Multi-Factor Secure OTP Auth Dialog
        AuthOtpDialog(
            isOpen = showOtpDialog,
            otpValue = enteredOtp,
            onOtpChange = { viewModel.enteredOtp.value = it },
            isLoading = isAuthenticating,
            strings = strings,
            amountToPay = grandTotal,
            paymentMethod = selectedPaymentMethod,
            onDismiss = { viewModel.showOtpDialog.value = false },
            onConfirm = { viewModel.confirmPaymentAndPlaceOrder() }
        )
    }
}
