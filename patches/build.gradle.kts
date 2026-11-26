group = "com.awt.regain"

patches {
    about {
        name = "Regain Patches"
        description = "Morphe patches for Regain (ai.regainapp)"
        source = "https://github.com/your-username/regain-patches"
        author = "AWT"
        contact = "https://github.com/your-username/regain-patches/issues"
        website = "https://github.com/your-username/regain-patches"
        license = "GNU General Public License v3.0"
    }
}

dependencies {
    // Required due to smali, or build fails.
    implementation(libs.guava)

    implementation(libs.morphe.patches.library)
    testImplementation(kotlin("test"))
}
