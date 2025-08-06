package com.shopaccounting.database

import androidx.room.*
import com.shopaccounting.models.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getTransactionsByCustomer(customerId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Query("SELECT SUM(CASE WHEN type = 'BORROW' THEN amount ELSE -amount END) FROM transactions WHERE customerId = :customerId")
    suspend fun getCustomerBalance(customerId: Long): Double?
}
