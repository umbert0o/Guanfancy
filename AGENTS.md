# Guanfancy - Android Medication Tracking App

An Android app for tracking Guanfacine medication intake with food zone timing indicators.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Run lint checks
./gradlew lint

# Run unit tests (all)
./gradlew test

# Run unit tests for specific test class
./gradlew test --tests "com.guanfancy.app.ExampleTest"

# Run single test method
./gradlew test --tests "com.guanfancy.app.ExampleTest.testMethodName"

# Run Android instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidDebugTest

# Run specific instrumentation test
./gradlew connectedAndroidDebugTest -Pandroid.testInstrumentationRunnerArguments.class=com.guanfancy.app.ExampleInstrumentedTest
```

## Project Structure

```
app/src/main/java/com/guanfancy/app/
├── data/                    # Data layer
│   ├── local/              # Room database, DAOs, entities
│   │   ├── dao/
│   │   └── entity/
│   ├── notifications/      # AlarmManager, notification helpers
│   ├── preferences/        # DataStore preferences
│   └── repository/         # Repository implementations
├── di/                      # Hilt DI modules
├── domain/                  # Business logic layer
│   ├── model/              # Domain models, constants
│   └── repository/         # Repository interfaces
├── ui/                      # Presentation layer
│   ├── components/         # Reusable Compose components
│   ├── navigation/         # Navigation graph, screens
│   ├── screens/            # Screen composables + ViewModels
│   └── theme/              # Material3 theme, colors, typography
├── GuanfancyApplication.kt # Hilt application class
└── MainActivity.kt         # Single activity
```

## Architecture

- **Clean Architecture**: Separation into data/domain/ui layers
- **MVVM**: ViewModels expose StateFlow to Compose UI
- **Repository Pattern**: Domain interfaces, data implementations
- **Dependency Injection**: Hilt with @HiltViewModel, @Inject, @Singleton
- **Single Activity**: Navigation Compose for screen navigation

## Code Style Guidelines

### Imports
- Order: Android/Jetpack → Kotlin → Third-party → Local packages
- No unused imports
- No wildcard imports

### Formatting
- 4-space indentation
- Maximum line length: 120 characters
- Blank line between sections (imports, class members)
- Trailing lambda for Composable function parameters

### Types & Nullability
- Use `data class` for models
- Prefer `val` over `var`
- Use nullable types (`?`) explicitly where null is valid
- Use `!!` only when absolutely certain of non-null
- Prefer `?:` elvis operator for null defaults

### Naming Conventions
- **Classes**: PascalCase (`DashboardViewModel`)
- **Functions**: camelCase (`markIntakeTaken`)
- **Properties**: camelCase (`nextIntake`)
- **Composables**: PascalCase, suffixed with `Screen` or descriptive name (`DashboardScreen`, `NextIntakeCard`)
- **State classes**: Screen name + `State` (`DashboardState`)
- **Constants**: SCREAMING_SNAKE_CASE in companion objects or objects
- **Private backing properties**: Prefix with underscore (`_state`)

### Compose Guidelines
- Use `StateFlow` + `collectAsStateWithLifecycle()` for state in ViewModels
- Mark composables with `@Composable` annotation on separate line
- Use `@OptIn(ExperimentalMaterial3Api::class)` for experimental APIs
- Private composables at bottom of file
- Pass `modifier: Modifier = Modifier` as last parameter to public composables
- Use `Modifier` chain pattern (one modifier per line for complex chains)

### State Management
```kotlin
// ViewModel pattern
private val _state = MutableStateFlow(State())
val state: StateFlow<State> = _state.asStateFlow()

// Usage in Composable
val state by viewModel.state.collectAsStateWithLifecycle()
```

### Repository Pattern
- Domain: `interface MedicationRepository` in `domain/repository/`
- Data: `class MedicationRepositoryImpl` in `data/repository/`
- Bind via Hilt `@Module` with `@Binds`

### Entity/Domain Mapping
- Entity classes in `data/local/entity/`
- Domain models in `domain/model/`
- Extension functions `toDomain()` and `toEntity()` in entity file

### Error Handling
- Use nullable returns for expected absence (`getIntakeById(): MedicationIntake?`)
- Use `Result<T>` for operations that can fail
- Log errors, don't silently catch exceptions

### Comments
- **Do NOT add comments** unless specifically asked
- Code should be self-documenting through clear naming

## Tech Stack

- **Kotlin**: 2.1.0
- **Compose BOM**: 2025.08.00
- **Material3**: For UI components
- **Navigation Compose**: 2.9.1
- **Hilt**: 2.57.1 for DI
- **Room**: 2.7.2 for local database
- **DataStore**: 1.1.7 for preferences
- **kotlinx-datetime**: 0.6.2 for date/time handling
- **WorkManager**: 2.10.2 for background tasks

## Rules

1) Ask questions if you have to and something is unclear!
2) Always use Context7 MCP when I need library/API documentation, code generation, setup or configuration steps without me having to explicitly ask.

## Good Software Architecture (Frontend + Backend)

- **Modularity**  
  Split into small, independent parts.

- **Separation of Concerns**  
  Keep different responsibilities (UI, logic, data) clearly apart.

- **Basic Layering**  
  Use layers: presentation/UI → services/business logic → data access.

- **Never let UI/frontend directly access the database**  
  Always go through services or APIs instead.

- **Scalability**  
  Design to handle growth easily.

- **Security**  
  Protect from the beginning.

- **Performance**  
  Keep it fast and efficient.

- **Maintainability**  
  Write clean, understandable, changeable code.
