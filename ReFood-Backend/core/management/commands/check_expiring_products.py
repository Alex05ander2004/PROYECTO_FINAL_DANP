from datetime import date

from django.core.management.base import BaseCommand
from firebase_admin import messaging

from products.models import Product
from users.models import User

# (dias_restantes_maximo, descuento_minimo). El descuento nunca baja uno que
# un administrador ya haya puesto mas alto a mano.
THRESHOLDS = [
    (2, 60),
    (7, 40),
    (30, 20),
]


class Command(BaseCommand):
    help = (
        'Sube el descuento y envia una notificacion push a los productos '
        'activos que cruzan un umbral de dias para vencer.'
    )

    def handle(self, *args, **options):
        today = date.today()
        products = Product.objects.filter(is_active=True, expiration_date__gte=today)

        updated = 0
        notified = 0

        for product in products:
            days_left = (product.expiration_date - today).days
            threshold = self._matching_threshold(days_left)
            if threshold is None:
                continue

            threshold_days, min_discount = threshold
            if product.last_discount_threshold == threshold_days:
                continue  # ya se aplico y notifico este umbral

            if min_discount > (product.discount_percentage or 0):
                product.discount_percentage = min_discount
                updated += 1

            product.last_discount_threshold = threshold_days
            product.save(update_fields=['discount_percentage', 'last_discount_threshold'])

            if self._notify(product, days_left):
                notified += 1

        self.stdout.write(self.style.SUCCESS(
            f'Productos actualizados: {updated}. Notificaciones enviadas: {notified}.'
        ))

    def _matching_threshold(self, days_left):
        for threshold_days, discount in THRESHOLDS:
            if days_left <= threshold_days:
                return threshold_days, discount
        return None

    def _notify(self, product, days_left):
        tokens = list(
            User.objects.exclude(fcm_token__isnull=True)
            .exclude(fcm_token='')
            .values_list('fcm_token', flat=True)
        )
        if not tokens:
            return False

        vencimiento_texto = 'hoy' if days_left <= 0 else f'en {days_left} día{"s" if days_left != 1 else ""}'
        body = f'{product.name} ahora tiene {product.discount_percentage}% de descuento — ¡vence {vencimiento_texto}!'

        messages = [
            messaging.Message(
                notification=messaging.Notification(title='🔥 Oferta urgente en ReFood', body=body),
                token=token,
            )
            for token in tokens
        ]
        try:
            response = messaging.send_each(messages)
            return response.success_count > 0
        except Exception as exc:
            self.stderr.write(self.style.WARNING(f'No se pudo enviar la notificación: {exc}'))
            return False
