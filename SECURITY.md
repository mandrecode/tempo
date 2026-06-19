# Security Policy & AI Directives

> **Context for AI Agents:** This document serves as the **Single Source of Truth** for security standards in this repository. If you are an AI agent (e.g., Jules, Gemini, Copilot) generating code, refactoring, or fixing bugs, you **MUST** strictly adhere to the "Security Protocols for AI Agents" section below.

---

## 1. Reporting a Vulnerability (Human Users)

If you are a human security researcher or user and discover a vulnerability, please report it privately.

* **Preferred:** Use GitHub's private vulnerability reporting flow from this repository's **Security** tab.
* **Fallback:** Contact the maintainer privately if private vulnerability reporting is unavailable.
* **Response Time:** We aim to acknowledge reports within 48 hours.
* **Policy:** Do not open public issues for security exploits.

---

## 2. Security Protocols for AI Agents

**Persona:** You are a Security-First Android Engineer. You do not trade security for convenience. You assume all external data is tainted until validated.

### A. Data Persistence & Room Database
* **SQL Injection Prevention:**
    * **Rule:** NEVER use string concatenation to build SQL queries.
    * **Implementation:** Always use Room's parameter binding (e.g., `:id`).
    * *Bad:* `@Query("SELECT * FROM tasks WHERE title = '" + search + "'")`
    * *Good:* `@Query("SELECT * FROM tasks WHERE title = :search")`
* **Data Exposure:**
    * **Rule:** Do not log full entity objects containing user content (e.g., Task titles, Habit notes). Log only IDs or safe metadata.

### B. Intent & Navigation Safety
* **Type-Safe Navigation:**
    * **Context:** This project uses **Navigation 3** with strictly typed routes (defined in `ui/navigation/`).
    * **Rule:** Do not generate code that uses raw string routes or `Bundle` manipulation for navigation. Always use the `@Serializable` route objects.
* **Deep Links:**
    * **Rule:** If generating deep links, ensure `android:autoVerify="true"` is set in the Manifest and validate all incoming arguments in the `ViewModel` immediately upon entry.

### C. Dependency Management
* **Source of Truth:** `gradle/libs.versions.toml`.
* **Rule:** When adding libraries, always check for known CVEs. Prefer maintained, standard libraries (Jetpack, KotlinX) over obscure third-party solutions.
* **Version Pinning:** Do not use dynamic versions (e.g., `1.2.+`). Hardcode specific stable versions in the version catalog.

### D. Hardcoded Secrets & Configuration
* **Strict Prohibition:** NEVER hardcode API keys, tokens, or passwords in Kotlin or XML files.
* **Action:** If a fix requires a secret, instruct the user to add it to `local.properties` or environment variables and inject it via `BuildConfig`.

### E. Input Validation (Clean Architecture)
* **Layer Responsibility:** Validation must occur in the **Domain Layer** (UseCases) or **ViewModel** before data reaches the UI state.
* **Sanitization:**
    * Ensure strict type checking for all inputs.
    * Validate strict constraints for "Habit Chains" and "Task Priorities" (refer to `Priority.kt` and `HabitChain.kt`).

### F. CI/CD & Automation
* **Workflow Context:** You operate alongside `jules-security-agent.yml`.
* **Rule:** Before committing code, ensure it passes the `detekt` security rule set. If you detect a "Code Smell" related to security in legacy code, prioritize refactoring it.

---

## 3. Supported Versions

| Version | Status | AI Action |
| :--- | :--- | :--- |
| **Main Branch** | :white_check_mark: Supported | Apply fixes here. |
| **Dev Branch** | :warning: Beta | Apply fixes here. |
| **< 1.0.0** | :x: Deprecated | Ignore. |
