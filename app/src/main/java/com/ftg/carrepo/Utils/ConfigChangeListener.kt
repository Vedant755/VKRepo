package com.ftg.carrepo.Utils

interface ConfigChangeListener {
    fun configChanged(input: String?, searchByRc: Boolean?, isTwoColumn: Boolean?, searchOffline: Boolean?)
}