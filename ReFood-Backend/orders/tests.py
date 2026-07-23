from datetime import date, timedelta

from django.core.cache import cache
from rest_framework import status
from rest_framework.test import APITestCase

from products.models import Category, Product
from users.models import User
from .models import CartItem, Order

TEST_PASSWORD = 'TestPassword123!'


class CartAndCheckoutTests(APITestCase):
    def setUp(self):
        cache.clear()
        self.admin = User.objects.create_user(
            email='admin@test.com', name='Admin', password=TEST_PASSWORD, role=User.Role.ADMIN
        )
        self.user = User.objects.create_user(email='client@test.com', name='Client', password=TEST_PASSWORD)
        self.other_user = User.objects.create_user(email='other@test.com', name='Other', password=TEST_PASSWORD)
        category = Category.objects.create(name='Panaderia')
        self.product = Product.objects.create(
            name='Pan', description='...', category=category, price=10, unit='bolsa',
            stock=5, expiration_date=date.today() + timedelta(days=5), is_active=True,
        )
        self.client.force_authenticate(user=self.user)

    def test_add_to_cart(self):
        response = self.client.post('/api/orders/cart/', {'product': self.product.id, 'quantity': 2})
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(CartItem.objects.get(user=self.user, product=self.product).quantity, 2)

    def test_add_to_cart_merges_existing_quantity(self):
        self.client.post('/api/orders/cart/', {'product': self.product.id, 'quantity': 2})
        self.client.post('/api/orders/cart/', {'product': self.product.id, 'quantity': 3})
        self.assertEqual(CartItem.objects.get(user=self.user, product=self.product).quantity, 5)

    def test_cart_is_scoped_to_user(self):
        CartItem.objects.create(user=self.other_user, product=self.product, quantity=1)
        response = self.client.get('/api/orders/cart/')
        self.assertEqual(len(response.data), 0)

    def test_update_cart_quantity(self):
        add_response = self.client.post('/api/orders/cart/', {'product': self.product.id, 'quantity': 2})
        item_id = add_response.data['id']
        response = self.client.patch(f'/api/orders/cart/{item_id}/', {'quantity': 4})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(CartItem.objects.get(id=item_id).quantity, 4)

    def test_remove_cart_item(self):
        add_response = self.client.post('/api/orders/cart/', {'product': self.product.id, 'quantity': 2})
        item_id = add_response.data['id']
        response = self.client.delete(f'/api/orders/cart/{item_id}/')
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertFalse(CartItem.objects.filter(id=item_id).exists())

    def test_checkout_creates_order_and_clears_cart(self):
        self.client.post('/api/orders/cart/', {'product': self.product.id, 'quantity': 2})
        response = self.client.post('/api/orders/checkout/', {
            'delivery_address': 'Av. Test 123', 'payment_method': 'TARJETA', 'notes': '',
        })
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(float(response.data['total']), 20.0)
        self.product.refresh_from_db()
        self.assertEqual(self.product.stock, 3)
        self.assertEqual(CartItem.objects.filter(user=self.user).count(), 0)

    def test_checkout_stores_payment_reference(self):
        self.client.post('/api/orders/cart/', {'product': self.product.id, 'quantity': 1})
        response = self.client.post('/api/orders/checkout/', {
            'delivery_address': 'Av. Test 123', 'payment_method': 'YAPE', 'payment_reference': '123456',
            'notes': '',
        })
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        order = Order.objects.get(id=response.data['id'])
        self.assertEqual(order.payment_reference, '123456')

    def test_checkout_fails_with_empty_cart(self):
        response = self.client.post('/api/orders/checkout/', {
            'delivery_address': 'Av. Test 123', 'payment_method': 'TARJETA', 'notes': '',
        })
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_checkout_fails_with_insufficient_stock(self):
        self.client.post('/api/orders/cart/', {'product': self.product.id, 'quantity': 999})
        response = self.client.post('/api/orders/checkout/', {
            'delivery_address': 'Av. Test 123', 'payment_method': 'TARJETA', 'notes': '',
        })
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.product.refresh_from_db()
        self.assertEqual(self.product.stock, 5)

    def test_orders_scoped_to_own_user(self):
        Order.objects.create(user=self.other_user, delivery_address='x', payment_method='TARJETA', total=10)
        response = self.client.get('/api/orders/')
        self.assertEqual(len(response.data), 0)

    def test_admin_sees_all_orders(self):
        Order.objects.create(user=self.other_user, delivery_address='x', payment_method='TARJETA', total=10)
        self.client.force_authenticate(user=self.admin)
        response = self.client.get('/api/orders/')
        self.assertEqual(len(response.data), 1)

    def test_client_cannot_update_order_status(self):
        order = Order.objects.create(user=self.user, delivery_address='x', payment_method='TARJETA', total=10)
        response = self.client.patch(f'/api/orders/{order.id}/', {'status': 'ENTREGADO'})
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    def test_admin_can_update_order_status(self):
        order = Order.objects.create(user=self.user, delivery_address='x', payment_method='TARJETA', total=10)
        self.client.force_authenticate(user=self.admin)
        response = self.client.patch(f'/api/orders/{order.id}/', {'status': 'ENTREGADO'})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        order.refresh_from_db()
        self.assertEqual(order.status, 'ENTREGADO')
