package com.poptato.plugin

import com.poptato.plugin.configureAndroidCommonPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        plugins.apply("com.android.library")
        configureAndroidCommonPlugin()
    }
}