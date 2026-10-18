package com.springshop.springshop.config;

import com.springshop.springshop.entity.Category;
import com.springshop.springshop.entity.Product;
import com.springshop.springshop.repository.CategoryRepository;
import com.springshop.springshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CatalogInitalizer implements CommandLineRunner {
    private record SeedProduct(String name, String description, String price, int stock) {}

    private record SeedCategory(String name, List<SeedProduct> products) {}

    private static final List<SeedCategory> CATALOG = List.of(
            new SeedCategory("Livres", List.of(
                    new SeedProduct("Clean Code",
                            "Les bonnes pratiques pour écrire un code lisible, testable et facile à maintenir.",
                            "35.90", 25),
                    new SeedProduct("Design Patterns",
                            "Les grands patrons de conception de la programmation orientée objet, avec des exemples.",
                            "42.00", 12),
                    new SeedProduct("Le Petit Prince",
                            "Le conte poétique d'Antoine de Saint-Exupéry, un classique à lire à tout âge.",
                            "8.50", 40))),

            new SeedCategory("Informatique", List.of(
                    new SeedProduct("Clavier mécanique",
                            "Clavier filaire à switchs linéaires, rétroéclairé, disposition AZERTY.",
                            "79.90", 15),
                    new SeedProduct("Souris sans fil",
                            "Souris ergonomique rechargeable, connexion Bluetooth ou récepteur USB.",
                            "24.90", 30),
                    new SeedProduct("Écran 27 pouces QHD",
                            "Dalle IPS 2560 x 1440, 75 Hz, entrées HDMI et DisplayPort.",
                            "249.00", 4))),

            new SeedCategory("Maison", List.of(
                    new SeedProduct("Lampe de bureau LED",
                            "Lampe à bras articulé, trois températures de couleur et variateur d'intensité.",
                            "29.90", 18),
                    new SeedProduct("Cafetière filtre programmable",
                            "Capacité de 12 tasses, minuteur et arrêt automatique.",
                            "39.90", 10),
                    new SeedProduct("Plaid en coton",
                            "Plaid doux et respirant de 130 x 170 cm, lavable en machine.",
                            "34.00", 0))),

            new SeedCategory("Sport", List.of(
                    new SeedProduct("Tapis de yoga",
                            "Tapis antidérapant de 6 mm d'épaisseur, livré avec une sangle de transport.",
                            "19.90", 35),
                    new SeedProduct("Haltères réglables 20 kg",
                            "Paire d'haltères à disques interchangeables pour la musculation à domicile.",
                            "69.00", 8),
                    new SeedProduct("Gourde isotherme 750 ml",
                            "En acier inoxydable : garde le froid 24 h et le chaud 12 h.",
                            "22.50", 50))),

            new SeedCategory("Jeux", List.of(
                    new SeedProduct("Jeu de société de stratégie",
                            "De 2 à 5 joueurs, parties de 45 minutes, à partir de 10 ans.",
                            "34.90", 14),
                    new SeedProduct("Puzzle 1000 pièces",
                            "Paysage de montagne, pièces en carton épais, format fini de 68 x 48 cm.",
                            "15.90", 22),
                    new SeedProduct("Jeu de cartes classique",
                            "Jeu de 54 cartes plastifiées pour tous les jeux traditionnels.",
                            "5.90", 60))));

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0 || productRepository.count() > 0) {
            log.info("catalogue nnon vide : aucune données de démo ajoutée");
            return;
        }

        int cpt = 0;
        for (SeedCategory seed : CATALOG) {
            Category category = categoryRepository.save(Category.builder().name(seed.name()).build());

            List<Product> product = seed.products().stream()
                    .map(p -> Product.builder()
                            .name(p.name())
                            .description(p.description())
                            .price(new BigDecimal(p.price()))
                            .stock(p.stock())
                            .category(category)
                            .build())
                    .toList();
            productRepository.saveAll(product);
            cpt += product.size();
        }
        log.info("catalogue de démo créé : {} catégories, {} produits", CATALOG.size(), cpt);
    }

}
