package com.shopaccounting.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "shop_owner")
data class ShopOwner(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerName: String,
    val shopName: String,
    val email: String,
    val contactNumber: String,
    val upiId: String,
    val password: String,
    val createdAt: Date = Date()
)
