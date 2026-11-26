group = "com.awt.regain"

patches {
    about {
        name = "Regain Patches"
        description = "Morphe patches for Regain (ai.regainapp)"
        source = "https://github.com/AWTMODS/AWT-Morphe"
        author = "AWTMODS"
        contact = "https://github.com/AWTMODS/AWT-Morphe/issues"
        website = "https://github.com/AWTMODS/AWT-Morphe"
        license = "GNU General Public License v3.0"
    }
}

dependencies {
    // Required due to smali, or build fails.
    implementation(libs.guava)

    implementation(libs.morphe.patches.library)
    testImplementation(kotlin("test"))
}
