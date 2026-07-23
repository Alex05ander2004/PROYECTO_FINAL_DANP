import json
import logging
import os

from django.apps import AppConfig

logger = logging.getLogger(__name__)


class CoreConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'core'

    def ready(self):
        import firebase_admin
        from django.conf import settings

        if firebase_admin._apps:
            return

        # En Render no existe el archivo de credenciales (esta en .gitignore
        # por seguridad), asi que se puede pasar el JSON completo por variable
        # de entorno en su lugar. Si no hay ninguna de las dos, las
        # notificaciones push quedan deshabilitadas en vez de tumbar el
        # arranque de Django (afecta solo a check_expiring_products).
        cred_json = getattr(settings, 'FIREBASE_CREDENTIALS_JSON', '')
        if cred_json:
            cred = firebase_admin.credentials.Certificate(json.loads(cred_json))
            firebase_admin.initialize_app(cred)
        elif os.path.exists(settings.FIREBASE_CREDENTIALS_PATH):
            cred = firebase_admin.credentials.Certificate(settings.FIREBASE_CREDENTIALS_PATH)
            firebase_admin.initialize_app(cred)
        else:
            logger.warning(
                'No se encontraron credenciales de Firebase: las notificaciones push quedan deshabilitadas.'
            )
