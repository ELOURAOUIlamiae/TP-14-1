package `Tp 14 1`
// Exercice 1 __________________________________________//

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class GestionCommandesRestaurant {

    //  Question 1: Fonction pour vérifier la disponibilité des ingrédients

    suspend fun verifierDisponibilite(commandeId: String): Boolean {
        println("[$commandeId] Vérification des ingrédients en cours...")
        delay(2000) // 2 secondes
        println("[$commandeId] Vérification des ingrédients terminée")
        return true
    }


    //  Question 2:  Fonction pour préparer la commande

    suspend fun preparerCommande(commandeId: String): String {
        println("[$commandeId] Préparation de la commande en cours...")
        delay(5000) // 5 secondes
        println("[$commandeId]  Préparation de la commande terminée")
        return "Commande $commandeId prête"
    }


    //  Question 3:  Fonction pour livrer le repas (avec Dispatchers.IO)


    suspend fun livrerRepas(commandeId: String, repas: String): String {
        withContext(Dispatchers.IO) {
            println("[$commandeId] Livraison du repas en cours... (sur le thread: ${Thread.currentThread().name})")
            delay(3000)
        }
        println("[$commandeId] Livraison du repas terminée")
        return "Repas $repas livré avec succès"
    }


    // 4. Fonction principale qui orchestre le processus complet

    suspend fun traiterCommande(commandeId: String) = coroutineScope {
        println("\n=== Début du traitement de la commande: $commandeId ===")
        try {
            val ingredientsDisponibles = verifierDisponibilite(commandeId)
            if (ingredientsDisponibles) {
                val commandePreparee = preparerCommande(commandeId)
                val resultatLivraison = livrerRepas(commandeId, commandePreparee)
                println("[$commandeId]  $resultatLivraison")
                println("[$commandeId]  Commande traitée avec succès!")
            } else {
                println("[$commandeId]  Ingrédients non disponibles, commande annulée")
            }
        } catch (e: Exception) {
            println("[$commandeId]  Erreur lors du traitement: ${e.message}")
        }
    }
    suspend fun traiterCommandeParallele(commandeId: String) = coroutineScope {
        println("\n=== Début du traitement PARALLÈLE de la commande: $commandeId ===")
        try {
            val ingredientsDisponibles = verifierDisponibilite(commandeId)
            if (ingredientsDisponibles) {
                val preparation = async { preparerCommande(commandeId) }
                val livraison = async {
                    val repas = preparation.await()
                    livrerRepas(commandeId, repas)
                }
                val resultat = livraison.await()
                println("[$commandeId]  $resultat")
                println("[$commandeId]  Commande traitée en parallèle avec succès!")
            } else {
                println("[$commandeId]  Ingrédients non disponibles, commande annulée")
            }
        } catch (e: Exception) {
            println("[$commandeId]  Erreur lors du traitement: ${e.message}")
        }
    }
}


fun main() = runBlocking {
    val gestionnaire = GestionCommandesRestaurant()
    println("=== TEST AVEC EXÉCUTION SÉQUENTIELLE ===")
    val tempsSequential = measureTimeMillis {
        gestionnaire.traiterCommande("CMD-001")
    }
    println(" Temps d'exécution séquentiel: ${tempsSequential / 1000.0} secondes")
    println("\n" + "=".repeat(50) + "\n")
    println("=== TEST AVEC EXÉCUTION PARALLÈLE ===")
    val tempsParallele = measureTimeMillis {
        gestionnaire.traiterCommandeParallele("CMD-002")
    }
    println(" Temps d'exécution parallèle: ${tempsParallele / 1000.0} secondes")
    println("\n" + "=".repeat(50) + "\n")
    println("=== TEST AVEC MULTIPLES COMMANDES ===")
    val tempsMultiples = measureTimeMillis {
        val commandes = listOf("CMD-003", "CMD-004", "CMD-005")
        commandes.map { commandeId ->
            launch {
                gestionnaire.traiterCommande(commandeId)
            }
        }.forEach { it.join() }
    }
    println(" Temps pour ${3} commandes: ${tempsMultiples / 1000.0} secondes")
}