package com.example.tastify.models

enum class CookingRole(val displayName: String, val requiredLevel: Int = 0) {
    BEGINNER("Beginner"),
    CHEF("Chef"),
    HOME_COOK("Home Cook"),
    BAKER("Baker"),
    GRILL_MASTER("Grill Master"),
    PASTRY_CHEF("Pastry Chef"),
    VEGETARIAN_COOK("Vegetarian Cook"),
    VEGAN_COOK("Vegan Cook"),
    GLUTEN_FREE_COOK("Gluten-Free Cook"),
    DAIRY_FREE_COOK("Dairy-Free Cook"),
    NUT_FREE_COOK("Nut-Free Cook"),
    PEANUT_FREE_COOK("Peanut-Free Cook"),
    SOY_FREE_COOK("Soy-Free Cook"),
    EGG_FREE_COOK("Egg-Free Cook"),
    SHELLFISH_FREE_COOK("Shellfish-Free Cook"),
    FOOD_LOVER("Food Lover"),
    CONTENT_CREATOR("Content Creator"),
    PROFESSIONAL_CHEF("Professional Chef"),

    KITCHEN_NOVICE("Kitchen Novice", 1),
    FLAVOR_EXPLORER("Flavor Explorer", 10),
    RECIPE_CRAFTER("Recipe Crafter", 20),
    SEASONED_COOK("Seasoned Cook", 30),
    CULINARY_ARTISAN("Culinary Artisan", 40),
    SOUS_CHEF("Sous Chef", 50),
    EXECUTIVE_CHEF("Executive Chef", 60),
    GOURMET_MASTER("Gourmet Master", 70),
    TASTE_VISIONARY("Taste Visionary", 85),
    TASTIFY_LEGEND("Tastify Legend", 100);

    companion object {
        fun getAvailableRoles(userLevel: Int): List<CookingRole> {
            return entries.filter { userLevel >= it.requiredLevel }
        }
    }
}