import io
from datetime import date, timedelta

from django.core.cache import cache
from django.core.files.uploadedfile import SimpleUploadedFile
from PIL import Image
from rest_framework import status
from rest_framework.test import APITestCase

from users.models import User
from .models import Category, Product

TEST_PASSWORD = 'TestPassword123!'


def _fake_image_file():
    buffer = io.BytesIO()
    Image.new('RGB', (10, 10), color='green').save(buffer, format='JPEG')
    buffer.seek(0)
    return SimpleUploadedFile('test.jpg', buffer.read(), content_type='image/jpeg')


class ProductTests(APITestCase):
    def setUp(self):
        cache.clear()
        self.admin = User.objects.create_user(
            email='admin@test.com', name='Admin', password=TEST_PASSWORD, role=User.Role.ADMIN
        )
        self.client_user = User.objects.create_user(
            email='client@test.com', name='Client', password=TEST_PASSWORD
        )
        self.category = Category.objects.create(name='Panaderia')
        self.active_product = Product.objects.create(
            name='Pan', description='Pan de prueba', category=self.category, price=10, unit='bolsa',
            stock=5, expiration_date=date.today() + timedelta(days=5), is_active=True,
        )
        self.inactive_product = Product.objects.create(
            name='Discontinuado', description='...', category=self.category, price=5, unit='unidad',
            stock=0, expiration_date=date.today() + timedelta(days=5), is_active=False,
        )

    def test_list_requires_authentication(self):
        response = self.client.get('/api/products/')
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_client_only_sees_active_products(self):
        self.client.force_authenticate(user=self.client_user)
        response = self.client.get('/api/products/')
        names = [p['name'] for p in response.data]
        self.assertIn('Pan', names)
        self.assertNotIn('Discontinuado', names)

    def test_admin_sees_all_products(self):
        self.client.force_authenticate(user=self.admin)
        response = self.client.get('/api/products/')
        self.assertEqual(len(response.data), 2)

    def test_client_cannot_create_product(self):
        self.client.force_authenticate(user=self.client_user)
        response = self.client.post('/api/products/', {
            'name': 'Nuevo', 'category': self.category.name, 'price': 5, 'unit': 'kg',
            'stock': 1, 'expiration_date': str(date.today() + timedelta(days=10)),
        })
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    def test_admin_can_create_product(self):
        self.client.force_authenticate(user=self.admin)
        image = _fake_image_file()
        response = self.client.post('/api/products/', {
            'name': 'Nuevo', 'category': self.category.name, 'price': 5, 'unit': 'kg',
            'stock': 1, 'expiration_date': str(date.today() + timedelta(days=10)), 'image': image,
        }, format='multipart')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertTrue(Product.objects.filter(name='Nuevo').exists())

    def test_admin_can_delete_product(self):
        self.client.force_authenticate(user=self.admin)
        response = self.client.delete(f'/api/products/{self.active_product.id}/')
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertFalse(Product.objects.filter(id=self.active_product.id).exists())

    def test_discount_price_calculation(self):
        self.active_product.discount_percentage = 50
        self.active_product.save()
        self.assertEqual(self.active_product.discount_price, 5.0)

    def test_no_discount_price_when_no_discount(self):
        self.assertIsNone(self.active_product.discount_price)

    def test_featured_offer_filter(self):
        self.active_product.is_featured_offer = True
        self.active_product.save()
        self.client.force_authenticate(user=self.client_user)
        response = self.client.get('/api/products/?is_featured_offer=true')
        names = [p['name'] for p in response.data]
        self.assertEqual(names, ['Pan'])

    def test_category_filter(self):
        other_category = Category.objects.create(name='Bebidas')
        Product.objects.create(
            name='Agua', description='...', category=other_category, price=3, unit='botella',
            stock=10, expiration_date=date.today() + timedelta(days=30), is_active=True,
        )
        self.client.force_authenticate(user=self.client_user)
        response = self.client.get(f'/api/products/?category={self.category.id}')
        names = [p['name'] for p in response.data]
        self.assertEqual(names, ['Pan'])
