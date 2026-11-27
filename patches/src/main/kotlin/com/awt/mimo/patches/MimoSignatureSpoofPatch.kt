package com.awt.mimo.patches

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.patch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.awt.mimo.patches.Constants.COMPATIBILITY_MIMO

@Suppress("unused")
val mimoSignatureSpoofPatch = patch(
    name = "Signature spoof",
    description = "Redirects PackageManager.getPackageInfo(...) through the " +
        "universal SignatureSpoof helper so the original release signing " +
        "certificate is returned, bypassing license/signature checks.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_MIMO)

    execute {
        val method = SignatureCheckFingerprint.methodOrNull
            ?: error("Signature check method not found. Adjust SignatureCheckFingerprint.")

        replaceGetPackageInfoInvoke(method)
    }
}

/**
 * Replaces every
 *   invoke-virtual {pm, name, flags}, Landroid/content/pm/PackageManager;->getPackageInfo(...)
 * with
 *   invoke-static {pm, name, flags}, L…SignatureSpoof;->getPackageInfo(...)
 * preserving the original registers exactly (verifier-safe).
 */
private fun replaceGetPackageInfoInvoke(method: Method) {
    val impl = method.implementation as? MutableMethodImplementation
        ?: error("Method has no implementation: ${method.definingClass}->${method.name}")

    for (i in impl.instructions.indices.reversed()) {
        val insn = impl.instructions[i]
        if (insn.opcode != Opcode.INVOKE_VIRTUAL) continue

        val old = insn as? BuilderInstruction35c ?: continue
        val ref = old.reference

        if (ref.definingClass != "Landroid/content/pm/PackageManager;") continue
        if (ref.name != "getPackageInfo") continue

        impl.instructions[i] = BuilderInstruction35c(
            Opcode.INVOKE_STATIC,
            old.registerCount,
            old.registerC,
            old.registerD,
            old.registerE,
            old.registerF,
            old.registerG,
            spoofRef(ref),
        )
    }
}

/** Builds a MethodReference to SignatureSpoof.getPackageInfo(...) matching `original`. */
private fun spoofRef(original: MethodReference): MethodReference =
    object : MethodReference {
        override fun getDefiningClass() = "Lapp/morphe/extension/signature/SignatureSpoof;"
        override fun getName() = original.name
        override fun getReturnType() = original.returnType
        override fun getParameterTypes() = original.parameterTypes
    }