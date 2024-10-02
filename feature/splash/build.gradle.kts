plugins {
    id("poptato.android.feature")
    id("poptato.android.compose")
}

android {
    namespace = "com.poptato.splash"
}

dependencies {
    implementation(projects.core)
    implementation(projects.domain)
    implementation(projects.core.ui)
    implementation(projects.designSystem)
}