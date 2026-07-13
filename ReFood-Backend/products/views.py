from django.shortcuts import render
from rest_framework import viewsets, permissions
from .models import Category, Product
from .serializers import CategorySerializer, ProductSerializer

# Create your views here.
class CategoryViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Category.objects.all()
    serializer_class = CategorySerializer
    permission_classes = [permissions.IsAuthenticated]

class ProductViewSet(viewsets.ReadOnlyModelViewSet):
    serializer_class = ProductSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        queryset = Product.objects.filter(is_active=True)
        featured = self.request.query_params.get('is_featured_offer')
        category = self.request.query_params.get('category')

        if featured is not None:
            queryset = queryset.filter(is_featured_offer=featured.lower() == 'true')
        if category:
            queryset = queryset.filter(category_id=category)

        return queryset