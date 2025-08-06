package com.shopaccounting.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val type: TransactionType,
    val paymentMethod: PaymentMethod,
    val description: String? = null,
    val upiTransactionId: String? = null,
    val createdAt: Date = Date()
)

enum class TransactionType {
    BORROW, PAYMENT
}

enum class PaymentMethod {
    CASH, UPI
}
