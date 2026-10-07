package com.saymon.pdf2cbz.data

import android.content.Context

/** Настройки: пока только тема. 0 — система, 1 — светлая, 2 — тёмная. */
class SettingsStore(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("p2c", Context.MODE_PRIVATE)

    var theme: Int
        get() = prefs.getInt("theme", 0)
        set(v) { prefs.edit().putInt("theme", v).apply() }
}
