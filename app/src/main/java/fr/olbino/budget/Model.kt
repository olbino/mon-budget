package fr.olbino.budget

import java.util.UUID

data class FixedCharge(
    val id: String = UUID.randomUUID().toString(),
    var label: String,
    var amount: Double,
    var paid: Boolean = false
)

data class Punctual(
    val id: String = UUID.randomUUID().toString(),
    var label: String,
    var amount: Double,
    var date: String // format "yyyy-MM-dd"
)

class MonthData(
    var salary: Double = 0.0,
    val fixed: MutableList<FixedCharge> = mutableListOf(),
    val punctual: MutableList<Punctual> = mutableListOf()
) {
    val totalFixed: Double get() = fixed.sumOf { it.amount }
    val totalPunctual: Double get() = punctual.sumOf { it.amount }
    val available: Double get() = salary - totalFixed
    val balance: Double get() = available - totalPunctual

    fun isEmpty(): Boolean = salary == 0.0 && fixed.isEmpty() && punctual.isEmpty()
}
