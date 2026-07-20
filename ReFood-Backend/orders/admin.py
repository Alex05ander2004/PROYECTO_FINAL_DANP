from django.contrib import admin
from .models import Order, OrderItem, CartItem

# Register your models here.
class OrderItemInline(admin.TabularInline):
    model = OrderItem
    extra = 0
    readonly_fields = ['product_name', 'unit_price', 'quantity']

@admin.register(Order)
class OrderAdmin(admin.ModelAdmin):
    list_display = ['id', 'user', 'status', 'payment_method', 'total', 'created_at']
    list_filter = ['status', 'payment_method']
    search_fields = ['user__email']
    inlines = [OrderItemInline]

@admin.register(CartItem)
class CartItemAdmin(admin.ModelAdmin):
    list_display = ['user', 'product', 'quantity']
    search_fields = ['user__email', 'product__name']