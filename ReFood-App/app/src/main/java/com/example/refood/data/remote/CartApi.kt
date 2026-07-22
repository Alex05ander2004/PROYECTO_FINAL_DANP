package com.example.refood.data.remote

import com.example.refood.data.remote.dto.AddCartItemRequest
import com.example.refood.data.remote.dto.CartItemDto
import com.example.refood.data.remote.dto.UpdateCartItemRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CartApi {
    @GET("orders/cart/")
    suspend fun getCart(): List<CartItemDto>

    @POST("orders/cart/")
    suspend fun addItem(@Body body: AddCartItemRequest): CartItemDto

    @PATCH("orders/cart/{id}/")
    suspend fun updateQuantity(@Path("id") id: Long, @Body body: UpdateCartItemRequest): CartItemDto

    @DELETE("orders/cart/{id}/")
    suspend fun removeItem(@Path("id") id: Long)
}
