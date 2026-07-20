from rest_framework import serializers
from .models import Category, Product

class CategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = Category
        fields = ['id', 'name']

class ProductSerializer(serializers.ModelSerializer):
    category = serializers.SlugRelatedField(slug_field='name', read_only=True)
    discountPrice = serializers.FloatField(source='discount_price', read_only=True)

    class Meta:
        model = Product
        fields = [
            'id', 'name', 'description', 'category', 'price', 'discountPrice',
            'image', 'unit', 'stock', 'expiration_date', 'is_featured_offer',
        ]