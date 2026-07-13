from rest_framework import serializers
from django.db import transaction
from .models import CartItem, Order, OrderItem
from products.models import Product

class CartItemSerializer(serializers.ModelSerializer):
    product_name = serializers.CharField(source='product.name', read_only=True)
    product_price = serializers.FloatField(source='product.price', read_only=True)
    product_discount_price = serializers.FloatField(source='product.discount_price', read_only=True)
    product_image = serializers.ImageField(source='product.image', read_only=True)

    class Meta:
        model = CartItem
        fields = ['id', 'product', 'product_name', 'product_price', 'product_discount_price', 'product_image', 'quantity']

class AddCartItemSerializer(serializers.Serializer):
    product = serializers.PrimaryKeyRelatedField(queryset=Product.objects.filter(is_active=True))
    quantity = serializers.IntegerField(min_value=1)

    def save(self, **kwargs):
        user = self.context['request'].user
        product = self.validated_data['product']
        quantity = self.validated_data['quantity']

        item, created = CartItem.objects.get_or_create(
            user=user, product=product,
            defaults={'quantity': quantity}
        )
        if not created:
            item.quantity += quantity
            item.save()
        return item

class OrderItemSerializer(serializers.ModelSerializer):
    class Meta:
        model = OrderItem
        fields = ['id', 'product', 'product_name', 'unit_price', 'quantity']

class OrderSerializer(serializers.ModelSerializer):
    items = OrderItemSerializer(many=True, read_only=True)

    class Meta:
        model = Order
        fields = ['id', 'status', 'delivery_address', 'payment_method', 'notes', 'total', 'created_at', 'items']
        read_only_fields = ['status', 'total']

class CreateOrderSerializer(serializers.Serializer):
    delivery_address = serializers.CharField(max_length=255)
    payment_method = serializers.ChoiceField(choices=Order.PaymentMethod.choices)
    notes = serializers.CharField(required=False, allow_blank=True)

    def create(self, validated_data):
        user = self.context['request'].user
        cart_items = CartItem.objects.filter(user=user).select_related('product')

        if not cart_items.exists():
            raise serializers.ValidationError('El carrito está vacío.')

        with transaction.atomic():
            for item in cart_items:
                if item.quantity > item.product.stock:
                    raise serializers.ValidationError(
                        f'Stock insuficiente para "{item.product.name}". Disponible: {item.product.stock}'
                    )

            total = sum(
                (item.product.discount_price or float(item.product.price)) * item.quantity
                for item in cart_items
            )

            order = Order.objects.create(
                user=user, total=total, **validated_data
            )

            for item in cart_items:
                unit_price = item.product.discount_price or item.product.price
                OrderItem.objects.create(
                    order=order,
                    product=item.product,
                    product_name=item.product.name,
                    unit_price=unit_price,
                    quantity=item.quantity,
                )
                item.product.stock -= item.quantity
                item.product.save()

            cart_items.delete()

        return order