plugins {
    id("poptato.android.feature")
    id("poptato.android.compose")
}

android {
    namespace = "com.poptato.ui"
}

dependencies {
    implementation(projects.designSystem)
}