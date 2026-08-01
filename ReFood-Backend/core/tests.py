from datetime import date, timedelta
from unittest.mock import MagicMock, patch

from django.core.management import call_command
from django.test import RequestFactory, TestCase

from products.models import Category, Product
from users.models import User

from .permissions import IsAdmin, IsClient

TEST_PASSWORD = 'TestPassword123!'


class PermissionsTests(TestCase):
    def setUp(self):
        self.factory = RequestFactory()
        self.admin = User(role=User.Role.ADMIN)
        self.client_user = User(role=User.Role.CLIENT)

    def test_is_admin_permission(self):
        request = self.factory.get('/')
        request.user = self.admin
        self.assertTrue(IsAdmin().has_permission(request, None))
        request.user = self.client_user
        self.assertFalse(IsAdmin().has_permission(request, None))

    def test_is_client_permission(self):
        request = self.factory.get('/')
        request.user = self.client_user
        self.assertTrue(IsClient().has_permission(request, None))
        request.user = self.admin
        self.assertFalse(IsClient().has_permission(request, None))


def _fake_send_each_response(success_count=1):
    response = MagicMock()
    response.success_count = success_count
    return response


class CheckExpiringProductsTests(TestCase):
    def setUp(self):
        self.category = Category.objects.create(name='Panaderia')

    def _make_product(self, days_left, discount_percentage=None, is_active=True, last_discount_threshold=None):
        return Product.objects.create(
            name=f'Producto {days_left}d',
            description='...',
            category=self.category,
            price=10,
            discount_percentage=discount_percentage,
            unit='unidad',
            stock=5,
            expiration_date=date.today() + timedelta(days=days_left),
            is_active=is_active,
            last_discount_threshold=last_discount_threshold,
        )

    @patch('core.management.commands.check_expiring_products.messaging.send_each')
    def test_applies_20_percent_within_30_days(self, mock_send):
        mock_send.return_value = _fake_send_each_response()
        product = self._make_product(days_left=25)
        call_command('check_expiring_products')
        product.refresh_from_db()
        self.assertEqual(product.discount_percentage, 20)
        self.assertEqual(product.last_discount_threshold, 30)

    @patch('core.management.commands.check_expiring_products.messaging.send_each')
    def test_applies_40_percent_within_7_days(self, mock_send):
        mock_send.return_value = _fake_send_each_response()
        product = self._make_product(days_left=5)
        call_command('check_expiring_products')
        product.refresh_from_db()
        self.assertEqual(product.discount_percentage, 40)
        self.assertEqual(product.last_discount_threshold, 7)

    @patch('core.management.commands.check_expiring_products.messaging.send_each')
    def test_applies_60_percent_within_2_days(self, mock_send):
        mock_send.return_value = _fake_send_each_response()
        product = self._make_product(days_left=1)
        call_command('check_expiring_products')
        product.refresh_from_db()
        self.assertEqual(product.discount_percentage, 60)
        self.assertEqual(product.last_discount_threshold, 2)

    @patch('core.management.commands.check_expiring_products.messaging.send_each')
    def test_never_lowers_an_existing_higher_discount(self, mock_send):
        mock_send.return_value = _fake_send_each_response()
        product = self._make_product(days_left=25, discount_percentage=80)
        call_command('check_expiring_products')
        product.refresh_from_db()
        self.assertEqual(product.discount_percentage, 80)

    @patch('core.management.commands.check_expiring_products.messaging.send_each')
    def test_does_not_reprocess_same_threshold(self, mock_send):
        mock_send.return_value = _fake_send_each_response()
        product = self._make_product(days_left=25, discount_percentage=20, last_discount_threshold=30)
        call_command('check_expiring_products')
        self.assertEqual(mock_send.call_count, 0)

    @patch('core.management.commands.check_expiring_products.messaging.send_each')
    def test_ignores_inactive_products(self, mock_send):
        mock_send.return_value = _fake_send_each_response()
        product = self._make_product(days_left=1, is_active=False)
        call_command('check_expiring_products')
        product.refresh_from_db()
        self.assertIsNone(product.discount_percentage)

    @patch('core.management.commands.check_expiring_products.messaging.send_each')
    def test_ignores_already_expired_products(self, mock_send):
        mock_send.return_value = _fake_send_each_response()
        product = self._make_product(days_left=-1)
        call_command('check_expiring_products')
        product.refresh_from_db()
        self.assertIsNone(product.discount_percentage)

    @patch('core.management.commands.check_expiring_products.messaging.send_each')
    def test_deactivates_expired_products(self, mock_send):
        mock_send.return_value = _fake_send_each_response()
        product = self._make_product(days_left=-1)
        call_command('check_expiring_products')
        product.refresh_from_db()
        self.assertFalse(product.is_active)

    @patch('core.management.commands.check_expiring_products.messaging.send_each')
    def test_does_not_deactivate_product_expiring_today(self, mock_send):
        mock_send.return_value = _fake_send_each_response()
        product = self._make_product(days_left=0)
        call_command('check_expiring_products')
        product.refresh_from_db()
        self.assertTrue(product.is_active)

    @patch('core.management.commands.check_expiring_products.messaging.send_each')
    def test_sends_notification_to_registered_tokens(self, mock_send):
        mock_send.return_value = _fake_send_each_response(success_count=1)
        User.objects.create_user(
            email='withtoken@test.com', name='Con token', password=TEST_PASSWORD, fcm_token='fake-token'
        )
        self._make_product(days_left=1)
        call_command('check_expiring_products')
        self.assertEqual(mock_send.call_count, 1)

    @patch('core.management.commands.check_expiring_products.messaging.send_each')
    def test_skips_notification_when_no_tokens_registered(self, mock_send):
        self._make_product(days_left=1)
        call_command('check_expiring_products')
        mock_send.assert_not_called()
