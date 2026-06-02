# Testing Guide for ViewBinding Migration

## Compilation Verification (Per File)

After migrating each file, verify that the module compiles:

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -50
```

This catches:
- Missing binding class imports
- Incorrect view property names (wrong camelCase conversion)
- Type mismatches between synthetic and binding
- Unresolved references from removed imports

### Faster variant-specific check

```bash
# Debug variant (default)
./gradlew :app:compileDebugKotlin

# Release variant
./gradlew :app:compileReleaseKotlin
```

---

## Per-File Verification Checklist

After completing each migration, verify these items:

- [ ] No `kotlinx.android.synthetic` imports remain in the file
- [ ] `tools:viewBindingIgnore="true"` removed from the layout XML root element
- [ ] Binding delegate declared with correct type: `private val xxxBinding: XxxBinding by viewBinding(XxxBinding::bind)`
- [ ] Correct import for `by.kirich1409.viewbindingdelegate.viewBinding`
- [ ] Correct import for the generated binding class (`org.stepic.droid.databinding.XxxBinding`)
- [ ] All synthetic view references replaced with `binding.viewId`
- [ ] Snake_case IDs converted to camelCase properties
- [ ] Layout inflation preserved (`setContentView` for Activities, `onCreateView` return for Fragments)
- [ ] No references to bare view IDs (e.g., `textView.text` without `binding.` prefix)

---

## Common Errors and Fixes

### "Unresolved reference: XxxBinding"

**Cause:** The binding class does not exist or is not in the expected package.

**Fix:**
1. Verify the layout XML file exists: check for `app/src/main/res/layout/xxx.xml`
2. **Check for `tools:viewBindingIgnore="true"` on the layout's root element** — this is the most common cause in this project. Remove it if present (see step 2.3a in the main skill).
3. Verify viewBinding is enabled: check `buildFeatures { viewBinding true }` in `app/build.gradle`
4. Check the binding class package: run `./gradlew :app:compileDebugKotlin` and check generated sources in `app/build/generated/data_binding_base_class_source_out/`
5. For layouts in different modules, the binding class package may differ

### "Cannot access 'viewId' on 'XxxBinding'"

**Cause:** The view ID does not exist in the layout XML file.

**Fix:**
1. Open the layout XML and verify the view ID exists
2. The synthetic may have referenced a view from an `<include>`d layout — check if the include has a `<binding>` tag
3. The view may be defined in a different layout file — check other synthetic imports

### "Property delegate must have a 'getValue' method"

**Cause:** The `viewBinding` delegate import is missing.

**Fix:** Add `import by.kirich1409.viewbindingdelegate.viewBinding`

### "Type mismatch" errors after migration

**Cause:** Synthetic properties infer types from `findViewById`, while binding classes use the exact XML `class` attribute.

**Fix:**
1. Check the XML `class` attribute matches the expected Kotlin type
2. Cast explicitly if types differ: `binding.view as SpecificType`
3. Update the XML to use the correct `class` attribute

### "Overload resolution ambiguity"

**Cause:** Multiple methods match after removing the synthetic import that disambiguated the call.

**Fix:** Add explicit type parameters or qualify the method call.

### "Val cannot be reassigned"

**Cause:** Synthetic references could be directly reassigned in rare cases. Binding properties are read-only.

**Fix:** Use the binding's setter methods instead of reassignment.

---

## Running Tests (Batch Verification)

After migrating a batch of files, run the project's test suite:

```bash
# Unit tests
./gradlew :app:testDebugUnitTest

# Android instrumented tests (requires emulator or device)
./gradlew :app:connectedDebugAndroidTest
```

If individual test classes relate to migrated screens, prioritize running those:

```bash
./gradlew :app:testDebugUnitTest --tests "org.stepik.android.view.auth.*"
```

---

## Verification: No Remaining Synthetic Imports

After completing all migrations, verify the entire project is clean:

```bash
# Should return 0 results
grep -rn "kotlinx.android.synthetic" --include="*.kt" app/src/

# Count remaining files (should be 0)
grep -rn "kotlinx.android.synthetic" --include="*.kt" -l app/src/ | wc -l
```

Also verify the Kotlin Android Extensions plugin can be removed (check `build.gradle`):

```bash
# Check for kotlin-android-extensions plugin
grep -rn "android.extensions\|kotlin-android-extensions" --include="*.gradle" .
```

If the plugin is still declared and no synthetic imports remain, it can be safely removed. If the project uses `@Parcelize`, switch to the `kotlin-parcelize` plugin first.

---

## Manual Verification (Critical Screens)

For high-traffic or critical screens, manual testing is recommended:

1. Build and install the debug variant: `./gradlew :app:assembleDebug`
2. Navigate to each migrated screen
3. Verify all UI elements render correctly
4. Test all interactions (button clicks, text input, scrolling, selections)
5. Verify no `ClassCastException` or `NullPointerException` in logcat
6. Test configuration changes (rotation) if the screen preserves state
7. Test back navigation and fragment transactions

### Screens to prioritize for manual testing

- Login/registration screens (critical user flow)
- Payment screens (financial impact)
- Main feed and navigation (core UX)
- Any screens with complex view hierarchies or animations
