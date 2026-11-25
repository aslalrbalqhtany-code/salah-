package com.bashar.mawaqit

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private val names = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
    private val prefs by lazy { getSharedPreferences("prayer_times", Context.MODE_PRIVATE) }
    private lateinit var fields: List<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 32, 28, 24)
            setBackgroundColor(0xFFF5F2EA.toInt())
        }
        val title = TextView(this).apply {
            text = "مواقيت الصلاة"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(0xFF174B3A.toInt())
        }
        layout.addView(title, match())
        val subtitle = TextView(this).apply {
            text = "أدخل أوقات الصلاة حسب تقويم مدينتك (مثال 04:30)"
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 18)
        }
        layout.addView(subtitle, match())
        val edits = mutableListOf<EditText>()
        names.forEachIndexed { i, name ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val label = TextView(this).apply { text = name; textSize = 18f; width = 130; setTextColor(0xFF222222.toInt()) }
            val edit = EditText(this).apply {
                hint = "05:00"
                textSize = 18f
                inputType =  timeInputType()
                setText(prefs.getString("time$i", ""))
                gravity = Gravity.CENTER
                setSingleLine(true)
            }
            row.addView(label)
            row.addView(edit, LinearLayout.LayoutParams(0, -2, 1f))
            layout.addView(row)
            edits.add(edit)
        }
        fields = edits
        val save = Button(this).apply {
            text = "حفظ وتفعيل التنبيهات"
            setOnClickListener {
                var valid = true
                fields.forEachIndexed { i, e ->
                    val value = e.text.toString().trim()
                    if (value.isNotEmpty() && !value.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))) valid = false
                    prefs.edit().putString("time$i", value).apply()
                }
                if (!valid) {
                    Toast.makeText(this@MainActivity, "اكتب الوقت بصيغة 24 ساعة مثل 05:30", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                PrayerScheduler.scheduleAll(this@MainActivity)
                if (Build.VERSION.SDK_INT >= 31 && !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
                Toast.makeText(this@MainActivity, "تم حفظ الأوقات وجدولة التنبيهات", Toast.LENGTH_LONG).show()
            }
        }
        layout.addView(save, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 22 })
        val note = TextView(this).apply {
            text = "ملاحظة: هذه النسخة تعتمد على إدخال الأوقات يدوياً. لتشغيل الأذان بصوته، أضف ملفاتك الصوتية إلى مجلد res/raw بعد تسميتها adhan1.mp3 وadhan2.mp3، ثم أعد البناء. حالياً يصدر تنبيه النظام."
            textSize = 13f
            setPadding(0, 18, 0, 0)
        }
        layout.addView(note, match())
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun timeInputType() = android.text.InputType.TYPE_CLASS_DATETIME or android.text.InputType.TYPE_DATETIME_VARIATION_TIME
    private fun match() = LinearLayout.LayoutParams(-1, -2)
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        }
    }
}
