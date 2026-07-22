package com.example.refood.data.remote

import com.example.refood.data.remote.dto.CreateOrderRequest
import com.example.refood.data.remote.dto.OrderDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {
    @GET("orders/")
    suspend fun getAll(): List<OrderDto>

    @GET("orders/{id}/")
    suspend fun getById(@Path("id") id: Long): OrderDto

    @POST("orders/checkout/")
    suspend fun checkout(@Body body: CreateOrderRequest): OrderDto
}
