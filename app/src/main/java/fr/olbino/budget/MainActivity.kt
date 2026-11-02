package fr.olbino.budget

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var store: Store
    private lateinit var currentKey: String       // "yyyy-MM"
    private lateinit var data: MonthData

    private val monthNames = arrayOf(
        "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre"
    )

    // Vues
    private lateinit var monthLabel: TextView
    private lateinit var salaryValue: TextView
    private lateinit var availableValue: TextView
    private lateinit var balanceValue: TextView
    private lateinit var fixedTotal: TextView
    private lateinit var punctualTotal: TextView
    private lateinit var fixedContainer: LinearLayout
    private lateinit var punctualContainer: LinearLayout
    private lateinit var fixedEmpty: TextView
    private lateinit var punctualEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        store = Store(this)

        monthLabel = findViewById(R.id.monthLabel)
        salaryValue = findViewById(R.id.salaryValue)
        availableValue = findViewById(R.id.availableValue)
        balanceValue = findViewById(R.id.balanceValue)
        fixedTotal = findViewById(R.id.fixedTotal)
        punctualTotal = findViewById(R.id.punctualTotal)
        fixedContainer = findViewById(R.id.fixedContainer)
        punctualContainer = findViewById(R.id.punctualContainer)
        fixedEmpty = findViewById(R.id.fixedEmpty)
        punctualEmpty = findViewById(R.id.punctualEmpty)

        findViewById<Button>(R.id.prevMonth).setOnClickListener { shiftMonth(-1) }
        findViewById<Button>(R.id.nextMonth).setOnClickListener { shiftMonth(1) }
        findViewById<View>(R.id.salaryRow).setOnClickListener { editSalary() }
        findViewById<Button>(R.id.addFixed).setOnClickListener { addFixed() }
        findViewById<Button>(R.id.addPunctual).setOnClickListener { addPunctual() }

        val now = Calendar.getInstance()
        currentKey = keyOf(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        data = loadOrCarry(currentKey)
        render()
    }

    // ---------- Mois ----------

    private fun keyOf(year: Int, month0: Int): String =
        String.format(Locale.US, "%04d-%02d", year, month0 + 1)

    private fun yearOf(key: String) = key.substring(0, 4).toInt()
    private fun month0Of(key: String) = key.substring(5, 7).toInt() - 1

    private fun shiftMonth(delta: Int) {
        val c = Calendar.getInstance()
        c.set(yearOf(currentKey), month0Of(currentKey), 1)
        c.add(Calendar.MONTH, delta)
        currentKey = keyOf(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
        data = loadOrCarry(currentKey)
        render()
    }

    /** Charge le mois, sinon le crée en reprenant les charges fixes du mois le plus proche. */
    private fun loadOrCarry(key: String): MonthData {
        store.load(key)?.let { return it }
        val keys = store.existingKeys()
        val src = keys.filter { it < key }.maxOrNull() ?: keys.filter { it > key }.minOrNull()
        val base = src?.let { store.load(it) }
        val d = MonthData()
        if (base != null) {
            d.salary = base.salary
            for (f in base.fixed) {
                d.fixed.add(FixedCharge(label = f.label, amount = f.amount, paid = false))
            }
        }
        return d
    }

    private fun persist() {
        store.save(currentKey, data)
    }

    // ---------- Affichage ----------

    private fun render() {
        monthLabel.text = "${monthNames[month0Of(currentKey)].replaceFirstChar { it.uppercase() }} ${yearOf(currentKey)}"
        salaryValue.text = money(data.salary)
        availableValue.text = money(data.available)
        balanceValue.text = money(data.balance)
        balanceValue.setTextColor(getColor(if (data.balance < 0) R.color.negative else R.color.positive))
        fixedTotal.text = money(data.totalFixed)
        punctualTotal.text = money(data.totalPunctual)

        renderFixed()
        renderPunctual()
    }

    private fun renderFixed() {
        fixedContainer.removeAllViews()
        fixedEmpty.visibility = if (data.fixed.isEmpty()) View.VISIBLE else View.GONE
        val inflater = LayoutInflater.from(this)
        for (f in data.fixed) {
            val row = inflater.inflate(R.layout.row_fixed, fixedContainer, false)
            val check = row.findViewById<CheckBox>(R.id.paidCheck)
            val labelText = row.findViewById<TextView>(R.id.labelText)
            val amountText = row.findViewById<TextView>(R.id.amountText)
            labelText.text = f.label
            amountText.text = money(f.amount)
            check.setOnCheckedChangeListener(null)
            check.isChecked = f.paid
            applyPaidStyle(labelText, amountText, f.paid)
            check.setOnCheckedChangeListener { _, isChecked ->
                f.paid = isChecked
                applyPaidStyle(labelText, amountText, isChecked)
                persist()
            }
            row.findViewById<View>(R.id.rowBody).setOnClickListener { editFixed(f) }
            fixedContainer.addView(row)
        }
    }

    private fun applyPaidStyle(label: TextView, amount: TextView, paid: Boolean) {
        val color = getColor(if (paid) R.color.text_muted else R.color.text)
        label.setTextColor(color)
        amount.setTextColor(color)
        val flags = if (paid)
            label.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        else
            label.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        label.paintFlags = flags
    }

    private fun renderPunctual() {
        punctualContainer.removeAllViews()
        punctualEmpty.visibility = if (data.punctual.isEmpty()) View.VISIBLE else View.GONE
        val inflater = LayoutInflater.from(this)
        val sorted = data.punctual.sortedBy { it.date }
        for (p in sorted) {
            val row = inflater.inflate(R.layout.row_punctual, punctualContainer, false)
            row.findViewById<TextView>(R.id.dateText).text = shortDate(p.date)
            row.findViewById<TextView>(R.id.labelText).text = p.label
            row.findViewById<TextView>(R.id.amountText).text = "-" + money(p.amount)
            row.findViewById<View>(R.id.rowBody).setOnClickListener { editPunctual(p) }
            punctualContainer.addView(row)
        }
    }

    // ---------- Salaire ----------

    private fun editSalary() {
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(if (data.salary == 0.0) "" else trimNumber(data.salary))
        input.hint = getString(R.string.amount_hint)
        val pad = dp(24)
        val wrap = LinearLayout(this)
        wrap.setPadding(pad, dp(8), pad, 0)
        wrap.addView(input)
        AlertDialog.Builder(this)
            .setTitle(R.string.dlg_salary)
            .setView(wrap)
            .setPositiveButton(R.string.save) { _, _ ->
                val v = parseAmount(input.text.toString())
                if (v == null) {
                    toast(R.string.err_amount)
                } else {
                    data.salary = v
                    persist()
                    render()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ---------- Charges fixes ----------

    private fun addFixed() {
        showItemDialog(
            titleRes = R.string.dlg_new_fixed,
            label = "", amount = null, withDate = false, dateKey = null,
            onSave = { label, amount, _ ->
                data.fixed.add(FixedCharge(label = label, amount = amount))
                persist(); render()
            },
            onDelete = null
        )
    }

    private fun editFixed(f: FixedCharge) {
        showItemDialog(
            titleRes = R.string.dlg_edit_fixed,
            label = f.label, amount = f.amount, withDate = false, dateKey = null,
            onSave = { label, amount, _ ->
                f.label = label; f.amount = amount
                persist(); render()
            },
            onDelete = {
                data.fixed.remove(f)
                persist(); render()
            }
        )
    }

    // ---------- Dépenses ponctuelles ----------

    private fun addPunctual() {
        showItemDialog(
            titleRes = R.string.dlg_new_punctual,
            label = "", amount = null, withDate = true, dateKey = defaultDateForMonth(),
            onSave = { label, amount, date ->
                data.punctual.add(Punctual(label = label, amount = amount, date = date ?: defaultDateForMonth()))
                persist(); render()
            },
            onDelete = null
        )
    }

    private fun editPunctual(p: Punctual) {
        showItemDialog(
            titleRes = R.string.dlg_edit_punctual,
            label = p.label, amount = p.amount, withDate = true, dateKey = p.date,
            onSave = { label, amount, date ->
                p.label = label; p.amount = amount; if (date != null) p.date = date
                persist(); render()
            },
            onDelete = {
                data.punctual.remove(p)
                persist(); render()
            }
        )
    }

    // ---------- Boîte de dialogue générique ----------

    private fun showItemDialog(
        titleRes: Int,
        label: String,
        amount: Double?,
        withDate: Boolean,
        dateKey: String?,
        onSave: (String, Double, String?) -> Unit,
        onDelete: (() -> Unit)?
    ) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_item, null)
        val labelInput = view.findViewById<EditText>(R.id.labelInput)
        val amountInput = view.findViewById<EditText>(R.id.amountInput)
        val dateRow = view.findViewById<View>(R.id.dateRow)
        val dateButton = view.findViewById<Button>(R.id.dateButton)

        labelInput.setText(label)
        if (amount != null) amountInput.setText(trimNumber(amount))

        val chosen = Calendar.getInstance()
        if (withDate) {
            dateRow.visibility = View.VISIBLE
            val d = dateKey ?: defaultDateForMonth()
            chosen.set(d.substring(0, 4).toInt(), d.substring(5, 7).toInt() - 1, d.substring(8, 10).toInt())
            dateButton.text = longDate(chosen)
            dateButton.setOnClickListener {
                DatePickerDialog(
                    this,
                    { _, y, m, day ->
                        chosen.set(y, m, day)
                        dateButton.text = longDate(chosen)
                    },
                    chosen.get(Calendar.YEAR), chosen.get(Calendar.MONTH), chosen.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        val builder = AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setView(view)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
        if (onDelete != null) {
            builder.setNeutralButton(R.string.delete, null)
        }
        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val lbl = labelInput.text.toString().trim()
            val amt = parseAmount(amountInput.text.toString())
            if (lbl.isEmpty()) { toast(R.string.err_label); return@setOnClickListener }
            if (amt == null) { toast(R.string.err_amount); return@setOnClickListener }
            val dateStr = if (withDate) keyDate(chosen) else null
            onSave(lbl, amt, dateStr)
            dialog.dismiss()
        }
        if (onDelete != null) {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle(R.string.delete_confirm_title)
                    .setMessage(R.string.delete_confirm_msg)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        onDelete()
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }

    // ---------- Dates ----------

    private fun defaultDateForMonth(): String {
        val now = Calendar.getInstance()
        val thisKey = keyOf(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        return if (thisKey == currentKey) {
            keyOf(now.get(Calendar.YEAR), now.get(Calendar.MONTH)) + String.format(Locale.US, "-%02d", now.get(Calendar.DAY_OF_MONTH))
        } else {
            "$currentKey-01"
        }
    }

    private fun keyDate(c: Calendar): String =
        String.format(Locale.US, "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))

    private fun shortDate(key: String): String =
        if (key.length >= 10) "${key.substring(8, 10)}/${key.substring(5, 7)}" else ""

    private fun longDate(c: Calendar): String =
        String.format(Locale.FRANCE, "%02d/%02d/%04d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR))

    // ---------- Montants ----------

    private fun parseAmount(s: String): Double? {
        val cleaned = s.trim().replace(" ", "").replace(",", ".")
        if (cleaned.isEmpty()) return null
        val v = cleaned.toDoubleOrNull() ?: return null
        return if (v < 0) null else Math.round(v * 100) / 100.0
    }

    private fun trimNumber(v: Double): String =
        if (v == Math.floor(v)) v.toLong().toString() else String.format(Locale.US, "%.2f", v)

    private val euroFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.FRANCE)

    private fun money(v: Double): String {
        val rounded = Math.round(v * 100) / 100.0
        euroFormat.minimumFractionDigits = if (rounded == Math.floor(rounded)) 0 else 2
        euroFormat.maximumFractionDigits = 2
        return "${euroFormat.format(rounded)} €"
    }

    // ---------- Utilitaires ----------

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun toast(res: Int) = Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
}
