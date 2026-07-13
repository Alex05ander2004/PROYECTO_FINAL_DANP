from rest_framework.routers import DefaultRouter
from django.urls import path, include
from .views import CartItemViewSet, OrderViewSet, CreateOrderView

router = DefaultRouter()
router.register('cart', CartItemViewSet, basename='cart-item')
router.register('', OrderViewSet, basename='order')

urlpatterns = [
    path('checkout/', CreateOrderView.as_view()),
    path('', include(router.urls)),
]