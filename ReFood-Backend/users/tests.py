from django.core.cache import cache
from rest_framework import status
from rest_framework.test import APITestCase

from .models import User

TEST_PASSWORD = 'TestPassword123!'


class AuthTests(APITestCase):
    def setUp(self):
        cache.clear()

    def test_register_creates_client_user(self):
        response = self.client.post('/api/auth/register/', {
            'name': 'Test User',
            'email': 'newuser@test.com',
            'password': TEST_PASSWORD,
            'phone': '999999999',
            'address': 'Av. Test 123',
        })
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        user = User.objects.get(email='newuser@test.com')
        self.assertEqual(user.role, User.Role.CLIENT)
        self.assertTrue(user.check_password(TEST_PASSWORD))

    def test_register_rejects_duplicate_email(self):
        User.objects.create_user(email='dup@test.com', name='Dup', password=TEST_PASSWORD)
        response = self.client.post('/api/auth/register/', {
            'name': 'Otro', 'email': 'dup@test.com', 'password': TEST_PASSWORD,
            'phone': '1', 'address': 'x',
        })
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_register_rejects_weak_password(self):
        response = self.client.post('/api/auth/register/', {
            'name': 'Debil', 'email': 'debil@test.com', 'password': '12345678',
            'phone': '1', 'address': 'x',
        })
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_login_returns_tokens(self):
        User.objects.create_user(email='login@test.com', name='Login', password=TEST_PASSWORD)
        response = self.client.post('/api/auth/login/', {'email': 'login@test.com', 'password': TEST_PASSWORD})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('access', response.data)
        self.assertIn('refresh', response.data)

    def test_login_wrong_password_fails(self):
        User.objects.create_user(email='login2@test.com', name='Login', password=TEST_PASSWORD)
        response = self.client.post('/api/auth/login/', {'email': 'login2@test.com', 'password': 'incorrecta'})
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_login_throttling_blocks_after_limit(self):
        for _ in range(10):
            self.client.post('/api/auth/login/', {'email': 'x@test.com', 'password': 'wrong'})
        response = self.client.post('/api/auth/login/', {'email': 'x@test.com', 'password': 'wrong'})
        self.assertEqual(response.status_code, status.HTTP_429_TOO_MANY_REQUESTS)

    def test_me_requires_authentication(self):
        response = self.client.get('/api/auth/me/')
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_me_returns_own_profile(self):
        user = User.objects.create_user(email='me@test.com', name='Me', password=TEST_PASSWORD)
        self.client.force_authenticate(user=user)
        response = self.client.get('/api/auth/me/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['email'], 'me@test.com')

    def test_me_cannot_change_own_role(self):
        user = User.objects.create_user(email='norole@test.com', name='NoRole', password=TEST_PASSWORD)
        self.client.force_authenticate(user=user)
        self.client.patch('/api/auth/me/', {'role': 'ADMIN'})
        user.refresh_from_db()
        self.assertEqual(user.role, User.Role.CLIENT)


class UserAdminTests(APITestCase):
    def setUp(self):
        cache.clear()
        self.admin = User.objects.create_user(
            email='admin@test.com', name='Admin', password=TEST_PASSWORD, role=User.Role.ADMIN
        )
        self.client_user = User.objects.create_user(
            email='client@test.com', name='Client', password=TEST_PASSWORD
        )

    def test_client_cannot_list_users(self):
        self.client.force_authenticate(user=self.client_user)
        response = self.client.get('/api/auth/users/')
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    def test_anonymous_cannot_list_users(self):
        response = self.client.get('/api/auth/users/')
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_admin_can_list_users(self):
        self.client.force_authenticate(user=self.admin)
        response = self.client.get('/api/auth/users/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)

    def test_admin_can_change_role(self):
        self.client.force_authenticate(user=self.admin)
        response = self.client.patch(f'/api/auth/users/{self.client_user.id}/', {'role': 'ADMIN'})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.client_user.refresh_from_db()
        self.assertEqual(self.client_user.role, 'ADMIN')

    def test_admin_can_deactivate_user(self):
        self.client.force_authenticate(user=self.admin)
        response = self.client.patch(f'/api/auth/users/{self.client_user.id}/', {'is_active': False})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.client_user.refresh_from_db()
        self.assertFalse(self.client_user.is_active)

    def test_admin_cannot_change_user_email(self):
        self.client.force_authenticate(user=self.admin)
        self.client.patch(f'/api/auth/users/{self.client_user.id}/', {'email': 'otro@test.com'})
        self.client_user.refresh_from_db()
        self.assertEqual(self.client_user.email, 'client@test.com')
