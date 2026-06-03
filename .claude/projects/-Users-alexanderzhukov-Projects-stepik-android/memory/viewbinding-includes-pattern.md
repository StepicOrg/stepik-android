---
name: viewbinding-includes-pattern
description: How to handle ViewBinding with included layouts — views from included layouts are NOT in parent binding
metadata:
  type: reference
---

# ViewBinding + Included Layouts Pattern

When a layout (A) includes another layout (B) without an `android:id` on the `<include>` tag, the generated ViewBinding class for A does **NOT** contain fields for views from B.

**Verified by examining generated binding classes (2026-06-03):**
- `FragmentCatalogBinding` only has views from `fragment_catalog.xml`, not from `view_catalog_search_toolbar.xml`
- `ActivityCertificatesBinding` only has views from `activity_certificates.xml`, not from included layouts

**Solution:** Use `view.findViewById()` for views from included layouts:
```kotlin
// In Fragment.onViewCreated():
searchViewToolbar = view.findViewById(R.id.searchViewToolbar)
backIcon = view.findViewById(R.id.backIcon)

// In Activity.onCreate():
tryAgainView = findViewById(R.id.tryAgain)
```

**Also:** Even after removing `tools:viewBindingIgnore` from both the parent and included layouts, the views from included layouts still won't appear in the parent binding. The `viewBindingIgnore` removal is still needed so the binding class is generated at all.

**Related:** [[viewbinding-migration-includes]]
