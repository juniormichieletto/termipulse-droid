# Agent Instructions & Engineering Standards

## Engineering Standards

### Test-Driven Development (TDD)
You MUST follow TDD for most implementation tasks:
1. **Red**: Write a failing test case that reproduces the issue or defines the new feature.
2. **Verify Failure**: Run the test to confirm it fails as expected.
3. **Green**: Implement the minimal code changes required to make the test pass.
4. **Refactor**: Clean up and refactor as necessary while keeping the test suite green.

### Verification
- Always execute the full test suite (`gradle :app:testDebugUnitTest` or `compile_applet`) after making changes to ensure the project remains functional and no regressions were introduced.

### Version Bumping
- Whenever you bump the project version (e.g., `versionCode` or `versionName` in `build.gradle.kts`), you MUST also update all version references in `README.md` to ensure documentation remains synchronized with the release.

### Source Control
- **NEVER** commit changes automatically. Always wait for explicit user confirmation or a "commit" directive after presenting changes for review.

---

## Recommended Project Guidelines

### Modern Android & Architecture
- **Language & Framework**: Use Kotlin and Jetpack Compose for all UI components.
- **State Management**: Keep UI state reactive using `ViewModel` and `StateFlow` / `collectAsStateWithLifecycle`.
- **Database & Storage**: Utilize Room for local persistence and `EncryptedSharedPreferences` for sensitive credentials.
- **Edge-to-Edge & Accessibility**: Ensure edge-to-edge support with proper window insets handling, custom adaptive icons, and minimum touch target sizes (48dp).

### Code Quality & Formatting
- Keep source files modular and under 500 lines where practical.
- Use explicit types for domain structures and modern Material Design 3 guidelines.
- Always verify app compilation using `compile_applet` after modifying source code.
