from django.contrib import admin
from .models import Category, Product

# Register your models here.
@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    list_display = ['id', 'name']
    search_fields = ['name']

@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    list_display = ['name', 'category', 'price', 'discount_percentage', 'discount_price_display', 'stock', 'unit', 'expiration_date', 'is_featured_offer', 'is_active']
    list_filter = ['category', 'is_featured_offer', 'is_active']
    search_fields = ['name']

    @admin.display(description='Precio con descuento')
    def discount_price_display(self, obj):
        return obj.discount_price or '-'