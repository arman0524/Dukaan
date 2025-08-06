package com.shopaccounting.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val address: String? = null,
    val totalBorrowedAmount: Double = 0.0,
    val createdAt: Date = Date()
)
