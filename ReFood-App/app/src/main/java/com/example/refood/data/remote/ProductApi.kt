package com.example.refood.data.remote

import com.example.refood.data.remote.dto.ProductDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductApi {
    @GET("products/")
    suspend fun getAll(): List<ProductDto>

    @GET("products/{id}/")
    suspend fun getById(@Path("id") id: Long): ProductDto
}
