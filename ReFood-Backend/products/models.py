from django.db import models
from users.models import User
from django.core.validators import MaxValueValidator

# Create your models here.
class Category(models.Model):
    name = models.CharField(max_length=100, unique=True)

    class Meta:
        verbose_name = 'Categoría'
        verbose_name_plural = 'Categorías'

    def __str__(self):
        return self.name

class Product(models.Model):
    name = models.CharField(max_length=150)
    description = models.TextField(blank=True)
    category = models.ForeignKey(Category, on_delete=models.PROTECT, related_name='products')
    price = models.DecimalField(max_digits=8, decimal_places=2)
    discount_percentage = models.PositiveIntegerField(
        null=True, blank=True,
        validators=[MaxValueValidator(100)]
    )
    image = models.ImageField(upload_to='products/')
    unit = models.CharField(max_length=30)
    stock = models.PositiveIntegerField(default=0)
    expiration_date = models.DateField()
    is_featured_offer = models.BooleanField(default=False)
    is_active = models.BooleanField(default=True)
    created_by = models.ForeignKey(User, on_delete=models.SET_NULL, null=True, related_name='products_created')
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'Producto'
        verbose_name_plural = 'Productos'
    
    def __str__(self):
        return self.name
    
    @property
    def discount_price(self):
        if self.discount_percentage:
            return round(float(self.price) * (1 - self.discount_percentage / 100), 2)
        return None