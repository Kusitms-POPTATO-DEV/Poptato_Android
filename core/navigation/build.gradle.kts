plugins {
    id("poptato.android.feature")
    id("poptato.android.compose")
}

android {
    namespace = "com.poptato.navigation"
}

dependencies {
    implementation(projects.core)
    implementation(projects.domain)
    implementation(projects.feature.splash)
}