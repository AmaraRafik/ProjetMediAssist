package com.example.projetmediassist.models

data class SymptomSeed(
    val noms: List<String> // Le premier est le "nom principal", les autres sont des synonymes
)

data class MaladieSeed(
    val nom: String,
    val description: String,
    val symptomes: List<SymptomSeed>,
    val medicaments: List<MedicamentSeed>
)

data class MedicamentSeed(
    val nom: String,
    val description: String?,
    val contreIndications: String?,
    val interactions: String?,
    val posologie: String? // <<--- AJOUT
)

val maladiesSeed = listOf(
    MaladieSeed(
        "Bronchite",
        "Inflammation des bronches",
        listOf(
            SymptomSeed(listOf("toux", "tousser")),
            SymptomSeed(listOf("fièvre", "température", "fièvre légère")),
            SymptomSeed(listOf("essoufflement", "difficulté à respirer"))
        ),
        medicaments = listOf(
            MedicamentSeed(
                nom = "Ibuprofène",
                description = "Anti-inflammatoire non stéroïdien utilisé pour soulager la douleur et la fièvre",
                contreIndications = "asthme,ulcere,allergie_ibuprofene",
                interactions = "aspirine",
                posologie = "1 comprimé 3 fois par jour pendant 5 jours, après les repas"
            )
        )
    ),
    MaladieSeed(
        "Grippe",
        "Infection virale aiguë",
        listOf(
            SymptomSeed(listOf("fièvre", "température élevée")),
            SymptomSeed(listOf("maux de tête", "céphalée")),
            SymptomSeed(listOf("courbatures", "douleurs musculaires")),
            SymptomSeed(listOf("toux"))
        ),
        medicaments = listOf(
            MedicamentSeed(
                nom = "Paracétamol",
                description = "Antalgique et antipyrétique utilisé pour réduire la fièvre et soulager la douleur",
                contreIndications = "allergie_paracetamol",
                interactions = "amoxicilline",
                posologie = "1 comprimé toutes les 6 heures, pendant 3 jours, avant ou après repas"
            )
        )
    ),
    MaladieSeed(
        "Rhume",
        "Infection virale bénigne",
        listOf(
            SymptomSeed(listOf("nez qui coule", "rhinorrhée")),
            SymptomSeed(listOf("éternuements", "atchoum")),
            SymptomSeed(listOf("toux")),
            SymptomSeed(listOf("gorge irritée", "mal de gorge"))
        ),
        medicaments = listOf(
            MedicamentSeed(
                nom = "Doliprane",
                description = "Analgésique léger pour soulager les symptômes du rhume",
                contreIndications = "allergie_paracetamol",
                interactions = "aucune",
                posologie = "1 comprimé toutes les 8 heures, pendant 3 jours, indifférent repas"
            )
        )
    ),
    MaladieSeed(
        "Covid-19",
        "Maladie virale respiratoire",
        listOf(
            SymptomSeed(listOf("fièvre", "température")),
            SymptomSeed(listOf("toux")),
            SymptomSeed(listOf("perte d'odorat", "anosmie")),
            SymptomSeed(listOf("fatigue")),
            SymptomSeed(listOf("essoufflement"))
        ),
        medicaments = listOf(
            MedicamentSeed(
                nom = "Paracétamol",
                description = "Utilisé pour réduire la fièvre et soulager la douleur",
                contreIndications = "allergie_paracetamol",
                interactions = "aucune",
                posologie = "1 comprimé toutes les 6 heures, pendant 5 jours, après les repas"
            )
        )
    ),
    MaladieSeed(
        "Migraine",
        "Maux de tête récurrents",
        listOf(
            SymptomSeed(listOf("maux de tête", "céphalée")),
            SymptomSeed(listOf("nausée")),
            SymptomSeed(listOf("sensibilité à la lumière", "photophobie"))
        ),
        medicaments = listOf(
            MedicamentSeed(
                nom = "Ibuprofène",
                description = "Anti-inflammatoire pour soulager la douleur",
                contreIndications = "asthme,ulcere,allergie_ibuprofene",
                interactions = "aspirine",
                posologie = "1 comprimé 2 fois par jour pendant 3 jours, après les repas"
            )
        )
    ),
    MaladieSeed(
        "Sinusite",
        "Inflammation des sinus",
        listOf(
            SymptomSeed(listOf("douleur faciale")),
            SymptomSeed(listOf("nez bouché", "congestion nasale")),
            SymptomSeed(listOf("toux"))
        ),
        medicaments = listOf(
            MedicamentSeed(
                nom = "Amoxicilline",
                description = "Antibiotique utilisé en cas de sinusite bactérienne",
                contreIndications = "allergie_penicilline",
                interactions = "paracétamol",
                posologie = "1 comprimé 3 fois par jour pendant 7 jours, avant les repas"
            )
        )
    ),
    MaladieSeed(
        "Otite",
        "Inflammation de l'oreille",
        listOf(
            SymptomSeed(listOf("douleur à l'oreille", "otalgie")),
            SymptomSeed(listOf("fièvre"))
        ),
        medicaments = listOf(
            MedicamentSeed(
                nom = "Amoxicilline",
                description = "Antibiotique pour traiter l’otite",
                contreIndications = "allergie_penicilline",
                interactions = "paracétamol",
                posologie = "1 comprimé 3 fois par jour pendant 8 jours, indifférent repas"
            )
        )
    ),
    MaladieSeed(
        "Angine",
        "Inflammation de la gorge",
        listOf(
            SymptomSeed(listOf("gorge irritée", "mal de gorge")),
            SymptomSeed(listOf("fièvre")),
            SymptomSeed(listOf("difficulté à avaler"))
        ),
        medicaments = listOf(
            MedicamentSeed(
                nom = "Amoxicilline",
                description = "Antibiotique couramment prescrit pour l’angine bactérienne",
                contreIndications = "allergie_penicilline",
                interactions = "paracétamol",
                posologie = "1 comprimé 3 fois par jour pendant 6 jours, avant repas"
            )
        )
    ),
    MaladieSeed(
        "Gastro-entérite",
        "Infection digestive",
        listOf(
            SymptomSeed(listOf("diarrhée")),
            SymptomSeed(listOf("vomissements")),
            SymptomSeed(listOf("fièvre")),
            SymptomSeed(listOf("douleur abdominale"))
        ),
        medicaments = listOf(
            MedicamentSeed(
                nom = "Smecta",
                description = "Pansement digestif pour soulager la diarrhée",
                contreIndications = "occlusion_intestinale",
                interactions = "aucune",
                posologie = "1 sachet 3 fois par jour pendant 5 jours, après repas"
            )
        )
    ),
    MaladieSeed(
        "Allergie saisonnière",
        "Réaction allergique aux pollens",
        listOf(
            SymptomSeed(listOf("éternuements")),
            SymptomSeed(listOf("yeux qui piquent")),
            SymptomSeed(listOf("nez qui coule"))
        ),
        medicaments = listOf(
            MedicamentSeed(
                nom = "Loratadine",
                description = "Antihistaminique pour réduire les symptômes d’allergie",
                contreIndications = "allergie_loratadine",
                interactions = "aucune",
                posologie = "1 comprimé par jour pendant 10 jours, indifférent repas"
            )
        )
    )
)

