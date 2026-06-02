# ViewBinding Migration Patterns

## Important: Single Import for All Component Types

**The `viewBinding()` delegate uses the SAME import for Activity, Fragment, DialogFragment, BottomSheetDialogFragment, ViewHolder, and ViewGroup.** The library defines separate extension functions on each receiver type, but Kotlin resolves the correct one at the call site based on the class where the property is declared. From the consumer's perspective, it's one function name with one import.

```kotlin
// This ONE import works for ALL component types:
import by.kirich1409.viewbindingdelegate.viewBinding
```

**The syntax is also identical** for all lifecycle-aware components (Activity, Fragment, DialogFragment, BottomSheetDialogFragment):

```kotlin
private val binding: XxxBinding by viewBinding(XxxBinding::bind)
```

The library's `Fragment.viewBinding()` extension internally detects `DialogFragment` and dispatches to a dialog-lifecycle-aware delegate that binds/unbinds with the dialog view.

**The only exception** is ViewHolder/ViewGroup, which uses the lambda form because there is no standard lifecycle to bind to:

```kotlin
// ViewHolder uses lambda form:
private val viewBinding: ItemXxxBinding by viewBinding { ItemXxxBinding.bind(root) }
```

A deprecated `dialogViewBinding()` function exists in v1.4.7 (separate import: `by.kirich1409.viewbindingdelegate.dialogViewBinding`) but simply delegates to `viewBinding()`. **Never use `dialogViewBinding()`** — always use `viewBinding()` for all types.

What differs between component types is only the **surrounding lifecycle methods** (how the layout gets inflated), not the delegate API. The patterns below show these lifecycle differences.

---

## Binding Class Name Convention

The generated binding class name is derived from the layout XML filename by converting to PascalCase and appending "Binding":

| Layout XML | Binding Class |
|------------|--------------|
| `activity_auth_social.xml` | `ActivityAuthSocialBinding` |
| `fragment_debug.xml` | `FragmentDebugBinding` |
| `bottom_sheet_dialog_course_purchase.xml` | `BottomSheetDialogCoursePurchaseBinding` |
| `item_course_news.xml` | `ItemCourseNewsBinding` |
| `dialog_custom.xml` | `DialogCustomBinding` |
| `layout_notification_settings.xml` | `LayoutNotificationSettingsBinding` |

All binding classes live in the `org.stepic.droid.databinding` package.

---

## Activity Migration Pattern

### Before (Kotlin Synthetic)

```kotlin
package org.stepik.android.view.auth.ui.activity

import kotlinx.android.synthetic.main.activity_auth_social.*
// ... other imports

class SocialAuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_social)

        dismissButton.setOnClickListener { onBackPressed() }
        dismissButton.isVisible = true
        launchSignUpButton.setOnClickListener { /* ... */ }
        signInWithEmail.setOnClickListener { /* ... */ }
        signInText.text = spannableSignIn
        socialListRecyclerView.layoutManager = GridLayoutManager(this, 3)
        root_view.snackbar(messageRes = R.string.connectionProblems)
    }
}
```

### After (ViewBinding)

```kotlin
package org.stepik.android.view.auth.ui.activity

import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.databinding.ActivityAuthSocialBinding
// ... other imports (no kotlinx.android.synthetic)

class SocialAuthActivity : AppCompatActivity() {

    private val binding: ActivityAuthSocialBinding by viewBinding(ActivityAuthSocialBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_social)

        binding.dismissButton.setOnClickListener { onBackPressed() }
        binding.dismissButton.isVisible = true
        binding.launchSignUpButton.setOnClickListener { /* ... */ }
        binding.signInWithEmail.setOnClickListener { /* ... */ }
        binding.signInText.text = spannableSignIn
        binding.socialListRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.rootView.snackbar(messageRes = R.string.connectionProblems)
    }
}
```

### Key Points

- **Keep `setContentView(R.layout.xxx)`** — the delegate auto-binds to the content view
- **View ID conversion**: snake_case → camelCase (`root_view` → `rootView`, `sign_in_button` → `signInButton`)
- **Binding naming convention**: Use a descriptive prefix + "Binding" (e.g., `rubricatorBinding`, `courseNewsBinding`, `socialAuthBinding`)
- For Activities with toolbar access, nested bindings remain the same pattern: `binding.appBar.toolbarBinding.centeredToolbar`

---

## Fragment Migration Pattern

### Before (Kotlin Synthetic)

```kotlin
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout.setOnRefreshListener { /* ... */ }
        recyclerView.adapter = adapter
        emptyView.setOnClickListener { /* ... */ }
        view?.toolbar?.title = getString(R.string.title)
    }
}
```

### After (ViewBinding)

```kotlin
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val homeBinding: FragmentHomeBinding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeBinding.swipeRefreshLayout.setOnRefreshListener { /* ... */ }
        homeBinding.recyclerView.adapter = adapter
        homeBinding.emptyView.setOnClickListener { /* ... */ }
        homeBinding.toolbar.title = getString(R.string.title)
    }
}
```

### Key Points

- **Keep the Fragment constructor argument** `Fragment(R.layout.xxx)` if present
- The delegate binds via `Fragment.getView()` — no manual binding in `onViewCreated`
- If the Fragment uses `onCreateView` to inflate, keep that and the delegate auto-binds to the returned view
- No `onDestroyView` cleanup needed — the delegate handles lifecycle

---

## DialogFragment Migration Pattern

### Before (Kotlin Synthetic)

```kotlin
import kotlinx.android.synthetic.main.dialog_custom.*

class CustomDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_custom, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogTitle.text = "Title"
        positiveButton.setOnClickListener { dismiss() }
    }
}
```

### After (ViewBinding)

```kotlin
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.databinding.DialogCustomBinding

class CustomDialogFragment : DialogFragment() {

    private val dialogBinding: DialogCustomBinding by viewBinding(DialogCustomBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_custom, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogBinding.dialogTitle.text = "Title"
        dialogBinding.positiveButton.setOnClickListener { dismiss() }
    }
}
```

### Key Points

- **Same `viewBinding()` import and delegate pattern as Activity and Fragment** — no separate dialog API needed
- The library internally detects `DialogFragment` and uses a dialog-lifecycle-aware delegate that binds/unbinds with the dialog view
- Keep `onCreateView` returning the inflated view — the delegate binds to it
- Works identically for `AlertDialog` that use custom views inflated in `onCreateDialog`

---

## BottomSheetDialogFragment Migration Pattern

**Uses the exact same API as DialogFragment.** No separate guide needed — the patterns are identical. The project already has working examples:

- `CoursePurchaseBottomSheetDialogFragment` — uses `coursePurchaseBinding: BottomSheetDialogCoursePurchaseBinding by viewBinding(BottomSheetDialogCoursePurchaseBinding::bind)`
- `FilterSearchBottomSheetDialogFragment` — uses `filterRootBinding: BottomSheetDialogFilterSearchBinding by viewBinding(BottomSheetDialogFilterSearchBinding::bind)`

### Pattern

```kotlin
// Same import as for every other component type:
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.databinding.BottomSheetXxxBinding

class XxxBottomSheetDialogFragment : BottomSheetDialogFragment() {

    // Same delegate syntax as Fragment/DialogFragment/Activity:
    private val binding: BottomSheetXxxBinding by viewBinding(BottomSheetXxxBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_xxx, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use binding.xxxView instead of synthetic
    }
}
```

---

## Adapter / ViewHolder Migration Pattern

### Before (Kotlin Synthetic)

```kotlin
import kotlinx.android.synthetic.main.item_course_news.view.*

class CourseNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(data: CourseNews) {
        itemView.newsTitle.text = data.title
        itemView.newsDate.text = data.date
        itemView.newsBadges.isVisible = data.hasBadges
    }
}
```

### After (ViewBinding)

```kotlin
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.databinding.ItemCourseNewsBinding

class CourseNewsViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    private val viewBinding: ItemCourseNewsBinding by viewBinding { ItemCourseNewsBinding.bind(root) }

    fun bind(data: CourseNews) {
        viewBinding.newsTitle.text = data.title
        viewBinding.newsDate.text = data.date
        viewBinding.newsBadges.isVisible = data.hasBadges
    }
}
```

### Key Points

- Use the lambda form `viewBinding { XxxBinding.bind(root) }` for ViewHolders
- The parameter name changes from `itemView` to `root` (convention in the project)
- Remove the `itemView.` prefix — access views directly from the binding
- For adapter delegates extending `DelegateViewHolder`, follow the same pattern as `CourseNewsAdapterDelegate` in the project

---

## Adapter (non-ViewHolder) Migration Pattern

Some adapters may use synthetic imports directly on the adapter class. These typically need conversion to use ViewBinding inside `onBindViewHolder`:

### Before

```kotlin
import kotlinx.android.synthetic.main.item_social.view.*

class SocialAuthAdapter : RecyclerView.Adapter<SocialAuthAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.socialIcon.setImageResource(icons[position])
        holder.itemView.socialName.setText(names[position])
    }
}
```

### After

```kotlin
import org.stepic.droid.databinding.ItemSocialBinding

class SocialAuthAdapter : RecyclerView.Adapter<SocialAuthAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = ItemSocialBinding.bind(holder.itemView)
        binding.socialIcon.setImageResource(icons[position])
        binding.socialName.setText(names[position])
    }
}
```

Or preferably, store the binding in the ViewHolder using the delegate pattern above.

---

## Utility / Helper Class Migration

Some utility classes import synthetic for view type helpers, extension functions, or type resolution. These typically do NOT need a binding delegate.

### Options

1. **Remove unused synthetic imports** — if the import was only for type access
2. **Pass View as parameter** — instead of accessing via synthetic, accept the View and use `XxxBinding.bind(view)` or `view.findViewById()`
3. **Skip** — if the file is a helper that doesn't directly reference views

### Example: ToolbarHelper

```kotlin
// Before
import kotlinx.android.synthetic.main.activity_main.*

fun Toolbar.setupWithNavController() { /* ... */ }

// After — synthetic import removed, function unchanged
fun Toolbar.setupWithNavController() { /* ... */ }
```

---

## Common Pitfalls

### 1. Multiple synthetic import files

Some classes import from multiple layouts:

```kotlin
import kotlinx.android.synthetic.main.fragment_lesson.*
import kotlinx.android.synthetic.main.layout_video_controls.*
```

Each layout gets its own binding class. Create separate bindings if needed, or check if the second layout is `<include>`d in the first (in which case one binding covers both).

### 2. View references in lambdas

```kotlin
// Before
adapter.setOnItemClickListener { position ->
    recyclerView.scrollToPosition(position)
    titleView.text = items[position].name
}

// After
adapter.setOnItemClickListener { position ->
    binding.recyclerView.scrollToPosition(position)
    binding.titleView.text = items[position].name
}
```

### 3. Synthetic references in companion object

Static synthetic references are not supported. Move to instance methods:

```kotlin
// Before
companion object {
    fun newInstance(title: String): Fragment {
        // Cannot use synthetic here, but some code might reference them
    }
}

// After — same, synthetic was never valid here
```

### 4. Extension functions on views

Extension functions defined on view types still work with binding:

```kotlin
// Before
fun TextView.setStyledText(text: String) { /* ... */ }
textView.setStyledText("hello")

// After
binding.textView.setStyledText("hello")
```

### 5. View IDs with underscores

XML IDs use snake_case, binding properties use camelCase:

| XML ID | Binding Property |
|--------|-----------------|
| `root_view` | `rootView` |
| `sign_in_button` | `signInButton` |
| `recycler_view` | `recyclerView` |
| `text_view_title` | `textViewTitle` |

### 6. Included layouts

Views from `<include layout="@layout/xxx">` are accessible via the parent binding:

```kotlin
// If activity_main.xml includes layout_toolbar.xml
// binding.toolbarTitle is accessible directly (no separate binding needed)
binding.toolbarTitle.text = "Hello"
```

If the included layout has its own binding with `<binding>` tag, access via the nested binding property:

```kotlin
binding.includedLayout.toolbarTitle.text = "Hello"
```

**IMPORTANT — `viewBindingIgnore` on included layouts:** Each included layout may also have `tools:viewBindingIgnore="true"`. Check and remove it from every included layout that the migrated code references. See step 2.3b in the main skill for details.

**IMPORTANT — Passing included layouts to delegates:** When a delegate/helper class takes a `View` parameter representing an included layout's root, use `.root` on the included binding:

```kotlin
// Synthetic: root.achievementTile was a View
val delegate = SomeDelegate(root.achievementTile, resolver)

// ViewBinding: binding.achievementTile is the included layout's binding class
// Use .root to get the View (equivalent to the synthetic behavior)
val delegate = SomeDelegate(binding.achievementTile.root, resolver)
```
