# ViewBindingPropertyDelegate Library Migration Guide

> **⚠️ WARNING: Do NOT execute this library upgrade as part of the synthetic-to-ViewBinding migration.**
> The v2.x library requires Kotlin 2.1.10+ (project currently uses 1.7.10).
> This guide is provided for future reference only. See SKILL.md Phase 3 for context.

## Current Setup

| Property | Value |
|----------|-------|
| Library | viewbindingpropertydelegate-noreflection |
| Version | 1.4.7 |
| Maven coordinates | `com.github.kirich1409:viewbindingpropertydelegate-noreflection:1.4.7` |
| Package | `by.kirich1409.viewbindingdelegate` |
| Import statement | `import by.kirich1409.viewbindingdelegate.viewBinding` |
| Declared in | `dependencies.gradle` line 51 (version) and line 177 (coordinates) |
| Kotlin version in project | 1.7.10 |
| AGP version in project | 8.6.1 |

## New Version (v2.x)

| Property | Value |
|----------|-------|
| Library | vbpd (no-reflection variant) |
| Latest version | 2.0.4 |
| Maven coordinates | `dev.androidbroadcast.vbpd:vbpd:2.0.4` |
| Package | `dev.androidbroadcast.vbpd` |
| Import statement | `import dev.androidbroadcast.vbpd.viewBinding` |
| Minimum Kotlin | 2.1.10 |
| Minimum AGP | 8.8.0 |
| JVM target | 11 |

Reflection variant also available: `dev.androidbroadcast.vbpd:vbpd-reflection:2.0.4`

---

## Compatibility Assessment

**⚠️ NOT COMPATIBLE with the current project configuration.**

| Requirement | Current | Required | Status |
|-------------|---------|----------|--------|
| Kotlin | 1.7.10 | 2.1.10+ | ❌ Major upgrade needed |
| AGP | 8.6.1 | 8.8.0+ | ❌ Minor upgrade needed |
| JVM target | 8 (likely) | 11 | ❌ Configuration change needed |
| Gradle | Unknown | Compatible with AGP 8.8 | ❌ May need upgrade |

### Impact of Kotlin 1.7.10 → 2.1.10 upgrade

This is a major version jump spanning multiple breaking changes:

- Kotlin 1.8: stabilized language features, default gradual mode changes
- Kotlin 1.9: new K2 compiler preview, data object, enum entries
- Kotlin 2.0: K2 compiler becomes default, significant compilation changes
- Kotlin 2.1: further K2 optimizations and deprecations

The Kotlin upgrade is a separate, substantial effort that should NOT be bundled with the ViewBinding migration.

---

## Migration Steps (After Kotlin Upgrade)

Perform these steps ONLY after upgrading Kotlin to 2.1.10+ and AGP to 8.8.0+.

### Step 1: Update dependency in `dependencies.gradle`

```gradle
ext.versions = [
    // ...
    viewBindingDelegate: '2.0.4',  // was '1.4.7'
]

ext.libraries = [
    // ...
    // REMOVE:
    // viewBindingDelegate: "com.github.kirich1409:viewbindingpropertydelegate-noreflection:$versions.viewBindingDelegate",
    // ADD:
    viewBindingDelegate: "dev.androidbroadcast.vbpd:vbpd:$versions.viewBindingDelegate",
]
```

### Step 2: Replace all imports across the project

Find all files using the old import:

```bash
grep -rn "by.kirich1409.viewbindingdelegate" --include="*.kt" -l
```

In each file, replace:

```kotlin
// Old:
import by.kirich1409.viewbindingdelegate.viewBinding

// New:
import dev.androidbroadcast.vbpd.viewBinding
```

No other code changes needed for the basic usage pattern (`viewBinding(XxxBinding::bind)`).

### Step 3: Address breaking API changes

#### 3a. `onViewDestroyed` callback removed

If any binding delegate uses:

```kotlin
// v1.x pattern (now removed):
private val binding: XxxBinding by viewBinding(XxxBinding::bind) {
    onViewDestroyed { vb ->
        // cleanup code
    }
}
```

Move the cleanup code to the appropriate lifecycle method:

```kotlin
// v2.x — cleanup in lifecycle callbacks:
override fun onDestroyView() {
    // cleanup code here
    super.onDestroyView()
}
```

#### 3b. `ViewBindingPropertyDelegate.strictMode` removed

Remove any references to strict mode:

```kotlin
// REMOVE:
ViewBindingPropertyDelegate.strictMode = true
```

#### 3c. Lifecycle dependency removed

The v2.x library no longer depends on `androidx.lifecycle`. It uses:
- `FragmentManager.FragmentLifecycleCallbacks` for fragments
- `Application.ActivityLifecycleCallbacks` for activities

No code changes needed unless the project manually depended on the library's transitive lifecycle dependency.

### Step 4: Three Delegate Approaches in v2.x

The new version offers multiple approaches per target type. The project's current pattern maps directly to **Approach A**:

#### Approach A — Bind function reference (recommended, same as current)

```kotlin
import dev.androidbroadcast.vbpd.viewBinding

// Fragment
private val binding: FragmentXxxBinding by viewBinding(FragmentXxxBinding::bind)

// Activity
private val binding: ActivityXxxBinding by viewBinding(ActivityXxxBinding::bind)

// ViewHolder
private val viewBinding: ItemXxxBinding by viewBinding { ItemXxxBinding.bind(root) }
```

#### Approach B — Bind function + custom view provider

```kotlin
private val binding: ActivityMainBinding by viewBinding(
    vbFactory = ActivityMainBinding::bind,
    viewProvider = { activity -> activity.findViewById(R.id.coordinator) }
)
```

#### Approach C — Bind function + root view ID

```kotlin
private val binding: ActivityMainBinding by viewBinding(
    vbFactory = ActivityMainBinding::bind,
    viewBindingRootId = R.id.coordinator
)
```

### Step 5: Reflection variant (alternative)

If the project prefers using reflection (less explicit code), the `vbpd-reflection` artifact supports:

```kotlin
import dev.androidbroadcast.vbpd.viewBinding

// Reified type — no ::bind needed
private val binding: FragmentXxxBinding by viewBinding()
```

This is not recommended for the initial migration since the no-reflection variant provides compile-time safety.

---

## Summary

| Action | When | Scope |
|--------|------|-------|
| Synthetic → ViewBinding migration | Now | ~201 files |
| Kotlin upgrade | Separate effort | Entire project |
| Library v1.x → v2.x | After Kotlin upgrade | ~22 files with existing viewBinding |
| Import replacement | After library upgrade | Batch find-and-replace |

**Recommendation:** Complete the synthetic migration first using v1.4.7. The library upgrade is a separate, lower-priority task that depends on a Kotlin version upgrade.
