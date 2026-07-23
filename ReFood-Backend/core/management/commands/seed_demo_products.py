from datetime import date, timedelta
from pathlib import Path

from django.core.files import File
from django.core.management.base import BaseCommand

from products.models import Category, Product

# Catalogo de ejemplo (mismo usado antes localmente en la app Android,
# data/local/DatabaseSeeder.kt) para poder probar Productos/Carrito/Pedidos
# reales sin depender de carga manual de datos.
DEMO_PRODUCTS = [
    dict(name="Pan integral artesanal", category="Panaderia", price=8.50, discount_percentage=47,
         unit="bolsa x 6 uds", stock=14, days_to_expire=-7, is_featured_offer=True,
         description="Bolsa de pan integral horneado ayer, ideal para tostadas y sandwiches."),
    dict(name="Croissants de mantequilla", category="Panaderia", price=12.00, discount_percentage=50,
         unit="caja x 4 uds", stock=9, days_to_expire=-8, is_featured_offer=True,
         description="Croissants recien horneados, proximos a vencer al cierre del dia."),
    dict(name="Yogurt natural", category="Lacteos", price=9.90, discount_percentage=30,
         unit="1 L", stock=20, days_to_expire=-5, is_featured_offer=True,
         description="Yogurt natural sin azucar anadida, envase de 1 litro."),
    dict(name="Queso fresco paria", category="Lacteos", price=15.00, discount_percentage=None,
         unit="500 g", stock=11, days_to_expire=-1, is_featured_offer=False,
         description="Queso fresco tipo paria, corte artesanal."),
    dict(name="Leche fresca entera", category="Lacteos", price=6.50, discount_percentage=40,
         unit="1 L", stock=16, days_to_expire=-8, is_featured_offer=True,
         description="Leche fresca pasteurizada, proxima a vencer."),
    dict(name="Mix de frutas de temporada", category="Frutas y Verduras", price=18.00, discount_percentage=44,
         unit="malla 2 kg", stock=12, days_to_expire=-7, is_featured_offer=True,
         description="Seleccion de frutas maduras: platano, manzana y papaya."),
    dict(name="Verduras surtidas", category="Frutas y Verduras", price=14.00, discount_percentage=46,
         unit="malla 2 kg", stock=10, days_to_expire=-6, is_featured_offer=True,
         description="Zanahoria, brocoli y zapallo, ligeramente golpeados por transporte."),
    dict(name="Palta fuerte", category="Frutas y Verduras", price=10.00, discount_percentage=None,
         unit="kg", stock=25, days_to_expire=-4, is_featured_offer=False,
         description="Palta lista para consumir, punto optimo de maduracion."),
    dict(name="Arroz extra", category="Abarrotes", price=7.00, discount_percentage=28,
         unit="1 kg", stock=30, days_to_expire=103, is_featured_offer=False,
         description="Arroz extra en bolsa sellada, empaque con etiqueta ligeramente danada."),
    dict(name="Fideos spaghetti", category="Abarrotes", price=4.50, discount_percentage=44,
         unit="500 g", stock=40, days_to_expire=15, is_featured_offer=True,
         description="Paquete de fideos, proximo al vencimiento del lote."),
    dict(name="Galletas integrales", category="Abarrotes", price=5.50, discount_percentage=45,
         unit="paquete 200 g", stock=22, days_to_expire=-3, is_featured_offer=True,
         description="Paquete de galletas integrales, proximas a vencer."),
    dict(name="Jugo de naranja natural", category="Bebidas", price=9.00, discount_percentage=39,
         unit="1 L", stock=15, days_to_expire=-7, is_featured_offer=True,
         description="Botella de jugo de naranja recien exprimido."),
    dict(name="Agua mineral sin gas", category="Bebidas", price=12.00, discount_percentage=None,
         unit="pack x 6", stock=50, days_to_expire=160, is_featured_offer=False,
         description="Botella de agua mineral, pack de 6 unidades."),
    dict(name="Torta de chocolate (porciones)", category="Panaderia", price=20.00, discount_percentage=45,
         unit="caja x 6 porciones", stock=7, days_to_expire=-8, is_featured_offer=True,
         description="Porciones individuales de torta de chocolate del dia anterior."),
    dict(name="Miel de abeja pura", category="Abarrotes", price=25.00, discount_percentage=28,
         unit="500 g", stock=13, days_to_expire=220, is_featured_offer=False,
         description="Frasco de miel 100% natural, envase con leve abolladura."),
]

PLACEHOLDER_IMAGES = ["Test_1_dia.jpg", "Test_5_dias.jpg", "Test_25_dias.jpg"]


class Command(BaseCommand):
    help = "Crea categorias y productos de ejemplo para probar la app con datos reales."

    def handle(self, *args, **options):
        media_dir = Path(__file__).resolve().parents[3] / "media" / "images" / "products"
        created_count = 0

        for index, data in enumerate(DEMO_PRODUCTS):
            if Product.objects.filter(name=data["name"]).exists():
                continue

            category, _ = Category.objects.get_or_create(name=data["category"])
            expiration_date = date.today() + timedelta(days=data["days_to_expire"])

            product = Product(
                name=data["name"],
                description=data["description"],
                category=category,
                price=data["price"],
                discount_percentage=data["discount_percentage"],
                unit=data["unit"],
                stock=data["stock"],
                expiration_date=expiration_date,
                is_featured_offer=data["is_featured_offer"],
                is_active=True,
            )

            placeholder = media_dir / PLACEHOLDER_IMAGES[index % len(PLACEHOLDER_IMAGES)]
            if not placeholder.exists():
                # Si la imagen no existe, se genera con Pillow
                placeholder.parent.mkdir(parents=True, exist_ok=True)
                from PIL import Image, ImageDraw
                img = Image.new('RGB', (800, 800), color=(200, 200, 200))
                d = ImageDraw.Draw(img)
                d.text((350, 400), placeholder.name, fill=(0, 0, 0))
                img.save(placeholder)

            with open(placeholder, "rb") as f:
                product.image.save(placeholder.name, File(f), save=False)

            product.save()
            created_count += 1

        self.stdout.write(self.style.SUCCESS(f"{created_count} productos de ejemplo creados."))
