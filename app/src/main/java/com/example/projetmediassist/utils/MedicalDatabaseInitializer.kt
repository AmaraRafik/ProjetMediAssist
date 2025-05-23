package com.example.projetmediassist.utils

import android.content.Context
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.models.*
import kotlinx.coroutines.*
import java.util.Locale

object MedicalDatabaseInitializer {
    fun peuplerBaseMedicaleSiVide(context: Context, onFinished: () -> Unit) {
        val db = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val nbMaladies = db.maladieDao().countMaladies()
            if (nbMaladies == 0) {
                val symptomeIdMap = mutableMapOf<String, Int>()

                // Insère les symptômes uniques
                maladiesSeed
                    .flatMap { it.symptomes }
                    .distinctBy { it.noms[0] }
                    .forEach { symptome ->
                        val nomPrincipal = symptome.noms[0]
                        val synonymes = symptome.noms.drop(1).joinToString(",")
                        val id = db.symptomeDao().insert(
                            Symptome(
                                nom = nomPrincipal,
                                synonymes = synonymes
                            )
                        ).toInt()
                        symptomeIdMap[nomPrincipal] = id
                    }

                // Insère les maladies et les associations symptôme-maladie
                maladiesSeed.forEach { maladieSeed ->
                    val maladieId = db.maladieDao().insert(
                        Maladie(
                            nom = maladieSeed.nom,
                            description = maladieSeed.description
                        )
                    ).toInt()
                    maladieSeed.symptomes.forEach { symptome ->
                        val symptomeId = symptomeIdMap[symptome.noms[0]]!!
                        db.associationSymptomeMaladieDao().insert(
                            AssociationSymptomeMaladie(
                                symptomeId = symptomeId,
                                maladieId = maladieId
                            )
                        )
                    }
                }

                // Insertion des médicaments UNIQUES dans la table Medicament
                val medicamentsUniques = maladiesSeed
                    .flatMap { it.medicaments }
                    .distinctBy { it.nom.lowercase(Locale.ROOT) }

                medicamentsUniques.forEach { medicamentSeed ->
                    val dejaDansLaBase = db.medicamentDao().getByNoms(listOf(medicamentSeed.nom)).isNotEmpty()
                    if (!dejaDansLaBase) {
                        db.medicamentDao().insertAll(
                            Medicament(
                                nom = medicamentSeed.nom,
                                description = medicamentSeed.description,
                                contreIndications = medicamentSeed.contreIndications,
                                interactions = medicamentSeed.interactions,
                                posologie = medicamentSeed.posologie // <-- AJOUT POSOLOGIE
                            )
                        )
                    }
                }

                // Insertion des associations maladie <-> médicament
                maladiesSeed.forEach { maladieSeed ->
                    maladieSeed.medicaments.forEach { medicamentSeed ->
                        db.associationMaladieMedicamentDao().insert(
                            AssociationMaladieMedicament(
                                maladieNom = maladieSeed.nom,
                                medicamentNom = medicamentSeed.nom
                            )
                        )
                    }
                }
            }
            withContext(Dispatchers.Main) { onFinished() }
        }
    }
}
