from rest_framework import serializers
from .models import Category, Product

class CategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = Category
        fields = ['id', 'name']

class ProductSerializer(serializers.ModelSerializer):
    category = serializers.SlugRelatedField(
        slug_field='name',
        queryset=Category.objects.all()
    )

    class Meta:
        model = Product
        fields = [
            'id', 'name', 'description', 'category', 'price', 'discount_percentage',
            'discount_price', 'image', 'unit', 'stock', 'expiration_date',
            'is_featured_offer', 'is_active',
        ]