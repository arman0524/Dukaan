package com.shopaccounting.utils

import android.content.Context
import android.content.SharedPreferences
import com.shopaccounting.models.ShopOwner

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shop_prefs", Context.MODE_PRIVATE)

    fun saveLoginState(isLoggedIn: Boolean, ownerId: Long) {
        prefs.edit()
            .putBoolean("is_logged_in", isLoggedIn)
            .putLong("owner_id", ownerId)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun getOwnerId(): Long {
        return prefs.getLong("owner_id", 0)
    }

    fun saveShopOwnerDetails(shopOwner: ShopOwner) {
        prefs.edit()
            .putString("owner_name", shopOwner.ownerName)
            .putString("shop_name", shopOwner.shopName)
            .putString("owner_email", shopOwner.email)
            .putString("owner_contact", shopOwner.contactNumber)
            .putString("upi_id", shopOwner.upiId)
            .apply()
    }

    fun getOwnerName(): String {
        return prefs.getString("owner_name", "") ?: ""
    }

    fun getShopName(): String {
        return prefs.getString("shop_name", "") ?: ""
    }

    fun getOwnerEmail(): String {
        return prefs.getString("owner_email", "") ?: ""
    }

    fun getOwnerContact(): String {
        return prefs.getString("owner_contact", "") ?: ""
    }

    fun getUpiId(): String {
        return prefs.getString("upi_id", "") ?: ""
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
