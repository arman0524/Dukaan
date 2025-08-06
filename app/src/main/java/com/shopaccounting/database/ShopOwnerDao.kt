package com.shopaccounting.database

import androidx.room.*
import com.shopaccounting.models.ShopOwner

@Dao
interface ShopOwnerDao {
    @Query("SELECT * FROM shop_owner WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): ShopOwner?

    @Query("SELECT * FROM shop_owner LIMIT 1")
    suspend fun getShopOwner(): ShopOwner?

    @Insert
    suspend fun insertShopOwner(shopOwner: ShopOwner): Long

    @Update
    suspend fun updateShopOwner(shopOwner: ShopOwner)

    @Query("SELECT COUNT(*) FROM shop_owner")
    suspend fun getShopOwnerCount(): Int
}
