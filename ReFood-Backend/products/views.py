from django.views.generic import ListView
from django.shortcuts import render
from rest_framework import viewsets, permissions
from .models import Category, Product
from .serializers import CategorySerializer, ProductSerializer
from core.permissions import IsAdmin

# Create your views here.
class CategoryViewSet(viewsets.ModelViewSet):
    queryset = Category.objects.all()
    serializer_class = CategorySerializer

    def get_permissions(self):
        if self.action in ['create', 'update', 'partial_update', 'destroy']:
            return [IsAdmin()]
        return [permissions.IsAuthenticated()]


class ProductViewSet(viewsets.ModelViewSet):
    serializer_class = ProductSerializer

    def get_permissions(self):
        if self.action in ['create', 'update', 'partial_update', 'destroy']:
            return [IsAdmin()]
        return [permissions.IsAuthenticated()]

    def get_queryset(self):
        queryset = Product.objects.all()
        
        if self.request.user.role != 'ADMIN':
            queryset = queryset.filter(is_active=True)

        featured = self.request.query_params.get('is_featured_offer')
        category = self.request.query_params.get('category')

        if featured is not None:
            queryset = queryset.filter(is_featured_offer=featured.lower() == 'true')
        if category:
            queryset = queryset.filter(category_id=category)

        return queryset

    def perform_create(self, serializer):
        serializer.save(created_by=self.request.user)

# Vista genérica de lista de productos para la página principal
class ProductIndexView(ListView):
    model = Product
    template_name = 'products/index.html'
    context_object_name = 'products'

    def get_queryset(self):
        return Product.objects.filter(is_active=True).select_related('category')