# Guanfancy - Android Medication Tracking App

Android app for tracking Guanfacine medication intake with food zone timing indicators.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run lint checks
./gradlew lint

# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.guanfancy.app.ExampleTest"

# Run single test method
./gradlew test --tests "com.guanfancy.app.ExampleTest.testMethodName"

# Run instrumentation tests (requires device/emulator)
./gradlew connectedAndroidDebugTest

# Run specific instrumentation test
./gradlew connectedAndroidDebugTest -Pandroid.testInstrumentationRunnerArguments.class=com.guanfancy.app.ExampleInstrumentedTest
```

## Project Structure

```
app/src/main/java/com/guanfancy/app/
├── data/                    # Data layer
│   ├── local/              # Room database, DAOs, entities
│   ├── notifications/      # AlarmManager, notification helpers
│   ├── preferences/        # DataStore preferences
│   └── repository/         # Repository implementations
├── di/                      # Hilt DI modules
├── domain/                  # Business logic layer
│   ├── model/              # Domain models, constants, calculators
│   └── repository/         # Repository interfaces
├── ui/                      # Presentation layer
│   ├── components/         # Reusable Compose components
│   ├── navigation/         # Navigation graph, screen destinations
│   ├── screens/            # Screen composables + ViewModels
│   └── theme/              # Material3 theme, colors, typography
├── GuanfancyApplication.kt
└── MainActivity.kt
```

## Architecture

- **Clean Architecture**: data/domain/ui layer separation
- **MVVM**: ViewModels expose StateFlow to Compose UI
- **Repository Pattern**: Domain interfaces, data implementations
- **Dependency Injection**: Hilt with @HiltViewModel, @Inject, @Singleton
- **Single Activity**: Navigation Compose with type-safe destinations

## Code Style

### Imports
- Order: Android/Jetpack → Kotlin → Third-party → Local packages
- No unused imports, no wildcard imports

### Formatting
- 4-space indentation, 120 char max line length
- Blank line between sections (imports, class members)
- Trailing lambda for Composable function parameters

### Types & Nullability
- Use `data class` for models
- Prefer `val` over `var`
- Use nullable types (`?`) explicitly where null is valid
- Prefer `?:` elvis operator for null defaults

### Naming Conventions
- **Classes**: PascalCase (`DashboardViewModel`)
- **Functions**: camelCase (`markIntakeTaken`)
- **Properties**: camelCase (`nextIntake`)
- **Composables**: PascalCase with `Screen` suffix or descriptive name (`DashboardScreen`, `NextIntakeCard`)
- **State classes**: Screen name + `State` (`DashboardState`) - defined at file top, outside ViewModel
- **Private backing properties**: Underscore prefix (`_state`)
- **Constants**: SCREAMING_SNAKE_CASE in companion objects
- **Default values**: `val DEFAULT = ...` in companion object

### State Management
```kotlin
// State class at top of file (not nested)
data class DashboardState(
    val nextIntake: MedicationIntake? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()
    
    // Prefer update{} for partial updates
    fun setLoading() = _state.update { it.copy(isLoading = true) }
}

// In Composable
val state by viewModel.state.collectAsStateWithLifecycle()
```

### Compose Guidelines
- `@OptIn(ExperimentalMaterial3Api::class)` on same line as `@Composable`
- `modifier: Modifier = Modifier` as last parameter
- Private composables at bottom of file
- Navigation via callback lambdas, never pass NavHostController to screens

### Repository Pattern
- Interface in `domain/repository/`
- Implementation in `data/repository/` with `@Singleton`
- Map entity flows to domain at repository boundary:
```kotlin
override fun getAllIntakes(): Flow<List<MedicationIntake>> =
    dao.getAllIntakes().map { entities -> entities.map { it.toDomain() } }
```

### Entity/Domain Mapping
- Entity classes in `data/local/entity/`
- Domain models in `domain/model/`
- Mappers in separate file `[Entity]Mappers.kt`:
```kotlin
fun IntakeEntity.toDomain(): MedicationIntake = MedicationIntake(...)
fun MedicationIntake.toEntity(): IntakeEntity = IntakeEntity(...)
```

### Hilt DI Modules
- Repository bindings: abstract class with `@Binds`
- Database/DAO provisioning: object with `@Provides`
- Both use `@InstallIn(SingletonComponent::class)`

### Navigation
- Type-safe with Kotlin serialization:
```kotlin
sealed interface Screen {
    @Serializable data object Dashboard : Screen
    @Serializable data class Feedback(val intakeId: Long) : Screen
}
```

### Error Handling
- Nullable returns for expected absence (`getIntakeById(): MedicationIntake?`)
- `Result<T>` for operations that can fail
- Log errors, don't silently catch exceptions

### Comments
- **Do NOT add comments** unless specifically asked
- Code should be self-documenting through clear naming

## Tech Stack

| Library | Version |
|---------|---------|
| Kotlin | 2.1.0 |
| Compose BOM | 2025.08.00 |
| Material3 | (from BOM) |
| Navigation Compose | 2.9.1 |
| Hilt | 2.57.1 |
| Room | 2.7.2 |
| DataStore | 1.1.7 |
| kotlinx-datetime | 0.6.2 |
| WorkManager | 2.10.2 |
| kotlinx-serialization | 1.8.0 |

## Rules

1. Ask questions if something is unclear
2. Use Context7 MCP for library/API documentation without being asked
