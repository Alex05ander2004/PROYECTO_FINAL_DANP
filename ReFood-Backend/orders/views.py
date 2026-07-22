from django.shortcuts import render
from rest_framework import generics, viewsets, permissions, status
from rest_framework.response import Response
from .models import CartItem, Order
from .serializers import CartItemSerializer, AddCartItemSerializer, OrderSerializer, CreateOrderSerializer, UpdateOrderStatusSerializer
from core.permissions import IsAdmin

# Create your views here.
class CartItemViewSet(viewsets.ModelViewSet):
    serializer_class = CartItemSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return CartItem.objects.filter(user=self.request.user).select_related('product')

    def create(self, request, *args, **kwargs):
        serializer = AddCartItemSerializer(data=request.data, context={'request': request})
        serializer.is_valid(raise_exception=True)
        item = serializer.save()
        return Response(CartItemSerializer(item, context={'request': request}).data, status=status.HTTP_201_CREATED)

class OrderViewSet(viewsets.ReadOnlyModelViewSet):
    permission_classes = [permissions.IsAuthenticated]

    def get_serializer_class(self):
        if self.action == 'partial_update':
            return UpdateOrderStatusSerializer
        return OrderSerializer

    def get_permissions(self):
        if self.action == 'partial_update':
            return [IsAdmin()]
        return [permissions.IsAuthenticated()]

    def get_queryset(self):
        base = Order.objects.select_related('user').prefetch_related('items')
        if self.request.user.role == 'ADMIN':
            return base
        return base.filter(user=self.request.user)

    def partial_update(self, request, *args, **kwargs):
        order = self.get_object()
        serializer = UpdateOrderStatusSerializer(order, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(OrderSerializer(order).data)

class CreateOrderView(generics.CreateAPIView):
    serializer_class = CreateOrderSerializer
    permission_classes = [permissions.IsAuthenticated]

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data, context={'request': request})
        serializer.is_valid(raise_exception=True)
        order = serializer.save()
        return Response(OrderSerializer(order).data, status=status.HTTP_201_CREATED)