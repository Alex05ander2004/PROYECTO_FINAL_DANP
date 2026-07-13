package com.example.refood.data.local

import com.example.refood.data.local.entity.ProductEntity

/** Catálogo de ejemplo para poblar la base de datos local mientras no exista un backend compartido. */
object DatabaseSeeder {

    fun sampleProducts(): List<ProductEntity> = listOf(
        ProductEntity(
            name = "Pan integral artesanal",
            description = "Bolsa de pan integral horneado ayer, ideal para tostadas y sándwiches.",
            category = "Panadería",
            price = 8.50,
            discountPrice = 4.50,
            imageUrl = "https://picsum.photos/seed/refood-pan-integral/600/450",
            unit = "bolsa x 6 uds",
            stock = 14,
            expirationDate = "2026-07-14",
            isFeaturedOffer = true
        ),
        ProductEntity(
            name = "Croissants de mantequilla",
            description = "Croissants recién horneados, próximos a vencer al cierre del día.",
            category = "Panadería",
            price = 12.00,
            discountPrice = 6.00,
            imageUrl = "https://picsum.photos/seed/refood-croissant/600/450",
            unit = "caja x 4 uds",
            stock = 9,
            expirationDate = "2026-07-13",
            isFeaturedOffer = true
        ),
        ProductEntity(
            name = "Yogurt natural",
            description = "Yogurt natural sin azúcar añadida, envase de 1 litro.",
            category = "Lácteos",
            price = 9.90,
            discountPrice = 6.90,
            imageUrl = "https://picsum.photos/seed/refood-yogurt/600/450",
            unit = "1 L",
            stock = 20,
            expirationDate = "2026-07-16",
            isFeaturedOffer = true
        ),
        ProductEntity(
            name = "Queso fresco paria",
            description = "Queso fresco tipo paria, corte artesanal.",
            category = "Lácteos",
            price = 15.00,
            discountPrice = null,
            imageUrl = "https://picsum.photos/seed/refood-queso/600/450",
            unit = "500 g",
            stock = 11,
            expirationDate = "2026-07-20",
            isFeaturedOffer = false
        ),
        ProductEntity(
            name = "Leche fresca entera",
            description = "Leche fresca pasteurizada, próxima a vencer.",
            category = "Lácteos",
            price = 6.50,
            discountPrice = 3.90,
            imageUrl = "https://picsum.photos/seed/refood-leche/600/450",
            unit = "1 L",
            stock = 16,
            expirationDate = "2026-07-13",
            isFeaturedOffer = true
        ),
        ProductEntity(
            name = "Mix de frutas de temporada",
            description = "Selección de frutas maduras: plátano, manzana y papaya.",
            category = "Frutas y Verduras",
            price = 18.00,
            discountPrice = 10.00,
            imageUrl = "https://picsum.photos/seed/refood-frutas/600/450",
            unit = "malla 2 kg",
            stock = 12,
            expirationDate = "2026-07-14",
            isFeaturedOffer = true
        ),
        ProductEntity(
            name = "Verduras surtidas",
            description = "Zanahoria, brócoli y zapallo, ligeramente golpeados por transporte.",
            category = "Frutas y Verduras",
            price = 14.00,
            discountPrice = 7.50,
            imageUrl = "https://picsum.photos/seed/refood-verduras/600/450",
            unit = "malla 2 kg",
            stock = 10,
            expirationDate = "2026-07-15",
            isFeaturedOffer = true
        ),
        ProductEntity(
            name = "Palta fuerte",
            description = "Palta lista para consumir, punto óptimo de maduración.",
            category = "Frutas y Verduras",
            price = 10.00,
            discountPrice = null,
            imageUrl = "https://picsum.photos/seed/refood-palta/600/450",
            unit = "kg",
            stock = 25,
            expirationDate = "2026-07-17",
            isFeaturedOffer = false
        ),
        ProductEntity(
            name = "Arroz extra",
            description = "Arroz extra en bolsa sellada, empaque con etiqueta ligeramente dañada.",
            category = "Abarrotes",
            price = 7.00,
            discountPrice = 5.00,
            imageUrl = "https://picsum.photos/seed/refood-arroz/600/450",
            unit = "1 kg",
            stock = 30,
            expirationDate = "2026-11-01",
            isFeaturedOffer = false
        ),
        ProductEntity(
            name = "Fideos spaghetti",
            description = "Paquete de fideos, próximo al vencimiento del lote.",
            category = "Abarrotes",
            price = 4.50,
            discountPrice = 2.50,
            imageUrl = "https://picsum.photos/seed/refood-fideos/600/450",
            unit = "500 g",
            stock = 40,
            expirationDate = "2026-08-05",
            isFeaturedOffer = true
        ),
        ProductEntity(
            name = "Galletas integrales",
            description = "Paquete de galletas integrales, próximas a vencer.",
            category = "Abarrotes",
            price = 5.50,
            discountPrice = 3.00,
            imageUrl = "https://picsum.photos/seed/refood-galletas/600/450",
            unit = "paquete 200 g",
            stock = 22,
            expirationDate = "2026-07-18",
            isFeaturedOffer = true
        ),
        ProductEntity(
            name = "Jugo de naranja natural",
            description = "Botella de jugo de naranja recién exprimido.",
            category = "Bebidas",
            price = 9.00,
            discountPrice = 5.50,
            imageUrl = "https://picsum.photos/seed/refood-jugo/600/450",
            unit = "1 L",
            stock = 15,
            expirationDate = "2026-07-14",
            isFeaturedOffer = true
        ),
        ProductEntity(
            name = "Agua mineral sin gas",
            description = "Botella de agua mineral, pack de 6 unidades.",
            category = "Bebidas",
            price = 12.00,
            discountPrice = null,
            imageUrl = "https://picsum.photos/seed/refood-agua/600/450",
            unit = "pack x 6",
            stock = 50,
            expirationDate = "2027-01-01",
            isFeaturedOffer = false
        ),
        ProductEntity(
            name = "Torta de chocolate (porciones)",
            description = "Porciones individuales de torta de chocolate del día anterior.",
            category = "Panadería",
            price = 20.00,
            discountPrice = 11.00,
            imageUrl = "https://picsum.photos/seed/refood-torta/600/450",
            unit = "caja x 6 porciones",
            stock = 7,
            expirationDate = "2026-07-13",
            isFeaturedOffer = true
        ),
        ProductEntity(
            name = "Miel de abeja pura",
            description = "Frasco de miel 100% natural, envase con leve abolladura.",
            category = "Abarrotes",
            price = 25.00,
            discountPrice = 18.00,
            imageUrl = "https://picsum.photos/seed/refood-miel/600/450",
            unit = "500 g",
            stock = 13,
            expirationDate = "2027-03-01",
            isFeaturedOffer = false
        )
    )
}
