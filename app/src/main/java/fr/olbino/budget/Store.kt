package fr.olbino.budget

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Sauvegarde du budget, un enregistrement par mois (clé "yyyy-MM"). */
class Store(context: Context) {

    private val prefs = context.getSharedPreferences("budget", Context.MODE_PRIVATE)

    private fun root(): JSONObject {
        val s = prefs.getString("data", null) ?: return JSONObject().put("months", JSONObject())
        return try {
            val o = JSONObject(s)
            if (!o.has("months")) o.put("months", JSONObject())
            o
        } catch (e: Exception) {
            JSONObject().put("months", JSONObject())
        }
    }

    private fun months(root: JSONObject): JSONObject = root.getJSONObject("months")

    /** Clés des mois enregistrés, triées par ordre chronologique. */
    fun existingKeys(): List<String> {
        val m = months(root())
        return m.keys().asSequence().toList().sorted()
    }

    fun hasMonth(key: String): Boolean = months(root()).has(key)

    fun load(key: String): MonthData? {
        val obj = months(root()).optJSONObject(key) ?: return null
        return fromJson(obj)
    }

    fun save(key: String, data: MonthData) {
        val r = root()
        months(r).put(key, toJson(data))
        prefs.edit().putString("data", r.toString()).apply()
    }

    private fun toJson(d: MonthData): JSONObject {
        val o = JSONObject()
        o.put("salary", d.salary)
        val fa = JSONArray()
        for (f in d.fixed) {
            fa.put(
                JSONObject()
                    .put("id", f.id)
                    .put("label", f.label)
                    .put("amount", f.amount)
                    .put("paid", f.paid)
            )
        }
        o.put("fixed", fa)
        val pa = JSONArray()
        for (p in d.punctual) {
            pa.put(
                JSONObject()
                    .put("id", p.id)
                    .put("label", p.label)
                    .put("amount", p.amount)
                    .put("date", p.date)
            )
        }
        o.put("punctual", pa)
        return o
    }

    private fun fromJson(o: JSONObject): MonthData {
        val d = MonthData(o.optDouble("salary", 0.0))
        val fa = o.optJSONArray("fixed") ?: JSONArray()
        for (i in 0 until fa.length()) {
            val f = fa.getJSONObject(i)
            d.fixed.add(
                FixedCharge(
                    id = f.optString("id", UUID_fallback()),
                    label = f.optString("label", ""),
                    amount = f.optDouble("amount", 0.0),
                    paid = f.optBoolean("paid", false)
                )
            )
        }
        val pa = o.optJSONArray("punctual") ?: JSONArray()
        for (i in 0 until pa.length()) {
            val p = pa.getJSONObject(i)
            d.punctual.add(
                Punctual(
                    id = p.optString("id", UUID_fallback()),
                    label = p.optString("label", ""),
                    amount = p.optDouble("amount", 0.0),
                    date = p.optString("date", "")
                )
            )
        }
        return d
    }

    private fun UUID_fallback(): String = java.util.UUID.randomUUID().toString()
}
