package com.example

import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.localization.AppLanguage
import com.example.localization.LanguageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BlinkCartUnitTest {

    @Test
    fun testMultilingualSupport() {
        val hindiStrings = LanguageManager.getStrings(AppLanguage.HINDI)
        assertEquals("ब्लिंककार्ट", hindiStrings.appTitle)
        assertEquals("10 मिनट में डिलीवरी", hindiStrings.deliveryInMinutes)

        val gujaratiStrings = LanguageManager.getStrings(AppLanguage.GUJARATI)
        assertEquals("બ્લિંકકાર્ટ", gujaratiStrings.appTitle)

        val englishStrings = LanguageManager.getStrings(AppLanguage.ENGLISH)
        assertEquals("BlinkCart", englishStrings.appTitle)
    }

    @Test
    fun testProductDiscountsAndPricing() {
        val product = Product(
            id = "test_1",
            name = "Test Mango",
            hindiName = "आम",
            category = "Vegetables & Fruits",
            price = 80.0,
            originalPrice = 100.0,
            discountPercent = 20,
            unit = "1 kg"
        )

        assertTrue(product.originalPrice > product.price)
        assertEquals(20, product.discountPercent)
        assertTrue(product.isVeg)
    }

    @Test
    fun testOrderStatusEnumOrder() {
        assertTrue(OrderStatus.PLACED.ordinal < OrderStatus.PACKED.ordinal)
        assertTrue(OrderStatus.PACKED.ordinal < OrderStatus.ASSIGNED.ordinal)
        assertTrue(OrderStatus.ASSIGNED.ordinal < OrderStatus.OUT_FOR_DELIVERY.ordinal)
        assertTrue(OrderStatus.OUT_FOR_DELIVERY.ordinal < OrderStatus.DELIVERED.ordinal)
    }
}
