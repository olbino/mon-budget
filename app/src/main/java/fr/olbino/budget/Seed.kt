package fr.olbino.budget

/** Données de départ : charges fixes pré-remplies d'après la liste fournie.
 *  Utilisées uniquement si l'app ne contient encore aucun mois enregistré. */
object Seed {

    /** Libellés proposés dans la liste déroulante des dépenses ponctuelles. */
    val expenseLabels: List<String> = listOf(
        "Essence",
        "Péage / Tunnel",
        "Courses",
        "Restaurant",
        "Camping",
        "Carte SIM",
        "Cartouches",
        "AliExpress",
        "Temu",
        "Amazon",
        "Droits de douane",
        "Nom de domaine",
        "OVH Cloud",
        "Inscription course",
        "Cartes de visite",
        "Recommandé",
        "Coiffeur",
        "Pharmacie / Santé",
        "Vêtements",
        "Cadeau",
        "Loisirs",
        "Divers"
    )

    private val fixedCharges: List<Pair<String, Double>> = listOf(
        "PayPal – Aliexpress" to 27.40,
        "PayPal – Aliexpress (2)" to 7.93,
        "PayPal – Temu" to 3.65,
        "PayPal – Aliexpress cartouches" to 16.34,
        "Cotisations bancaires" to 12.68,
        "Apple" to 10.0,
        "Free" to 2.0,
        "Freebox" to 40.0,
        "Vosegus" to 45.0,
        "Apple (2)" to 10.0,
        "Appartement" to 600.0,
        "BPCE" to 70.0,
        "Pompe à chaleur + panneaux solaires" to 250.0,
        "Amazon" to 7.0,
        "Simple" to 40.0,
        "Google One" to 2.0,
        "Allianz" to 59.0,
        "DGFIP" to 71.0,
        "Free mobile" to 38.0,
        "EDF" to 179.0,
        "ChatGPT" to 23.0,
        "Claude" to 22.0,
        "Supabase" to 23.0
    )

    /** Salaire de départ (modifiable dans l'app). */
    private const val DEFAULT_SALARY = 2000.0

    fun initialMonth(): MonthData {
        val d = MonthData(salary = DEFAULT_SALARY)
        for ((label, amount) in fixedCharges) {
            d.fixed.add(FixedCharge(label = label, amount = amount))
        }
        return d
    }
}
