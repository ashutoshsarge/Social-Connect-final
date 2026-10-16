# Supabase Migration, Strict Auth Gate & Session Security Plan

Transition SocialConnect from Firebase Firestore to a native Supabase architecture, paired with strict initial authentication gating, session credential persistence, and an automated security logout policy after a configured number of logins.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following core behaviors and security policies are specified for this implementation:
> - **Initial Auth Gate**: Users cannot access the app dashboard or internal screens without first logging in or signing up. The initial landing screen is the Auth Screen.
> - **Credential Persistence**: Once authenticated, credentials and session tokens are securely saved so the user immediately enters the app without repeated prompts on regular app restarts.
> - **Automated Security Logout**: An active login counter tracks consecutive authenticated sessions. When the threshold is reached (e.g., 5 logins), the app automatically invalidates the session and logs the user out with a clear security notice requiring re-authentication.
> - **Supabase Data Layer**: Full removal of Firebase Firestore in favor of direct Supabase PostgREST endpoints and a complete SQL schema (`supabase_schema.sql`).

---

## 1. Overview & Core Concept

SocialConnect is a community volunteer and NGO impact platform. This update delivers two major pillars:
1. **Clean Supabase Architecture**: Completely removes Firebase and Firestore dependencies, switching all data storage, event listings, and user authentication to Supabase.
2. **Session Security & Access Control**: Enforces mandatory authentication prior to dashboard entry, saves credentials on successful login, and enforces a rotating security auto-logout after 5 logins to protect user accounts on shared devices.

---

## 2. User Experience & Visual Design

### 1. Landing & Mandatory Gate
- When opening the app for the first time or after logout, the user is presented with the **SocialConnect Auth Screen** featuring clean, non-intrusive tabs: **Sign In** and **Sign Up**.
- No access to community events, NGO dashboards, or volunteer hours is granted prior to authentication.

### 2. Successful Login & Direct Access
- When credentials match via Supabase Auth (or verified local offline store), the user immediately transitions to `MainAppScaffold` (Home, Explore, My Events, Community, Profile).
- The credentials/session state is persisted in local encrypted storage / DataStore / Room for seamless continuity across app launches.

### 3. Login Counter & Security Auto-Logout
- Every successful login or resumed session increments a persistent session counter.
- A badge/indicator on the Profile screen displays security status (e.g., *"Security session 3 of 5 active"*).
- When the login counter reaches the limit (5 logins), the app automatically triggers a secure logout, returns to the Auth screen, and displays an informative banner:
  > *"For your account security, your session has expired after 5 logins. Please enter your credentials to continue."*

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Threshold-Based Security Auto-Logout**
  - *Chosen Approach*: Track a persistent `loginCount` integer. Reset the counter upon deliberate manual password entry; increment upon each app opening/login; automatically trigger `logout()` when `loginCount >= 5`.
  - *Why*: Directly satisfies the user's security specification, preventing indefinitely open sessions on mobile devices while keeping the policy transparent.
  - *Alternatives Considered*: Pure time-based TTL (session expires after 7 days) — retained as an additional secondary check alongside the login counter.

- **Decision 2: Direct Supabase REST API & SQL Schema**
  - *Chosen Approach*: Use `OkHttpClient` to communicate with Supabase PostgREST (`/rest/v1/events`, `/rest/v1/user_profiles`) and Supabase Auth (`/auth/v1/token`, `/auth/v1/signup`), alongside a full `supabase_schema.sql` file.
  - *Why*: Eliminates all Firebase overhead, resolves KSP AWT headless warnings, and provides pure PostgreSQL table structures.

- **Decision 3: Complete Removal of Firestore Files**
  - *Chosen Approach*: Safely delete `FirestoreVolunteerEventRepository.kt`, `FirestoreUserRepository.kt`, `FirebaseInitHelper.kt`, `firestore.rules`, and `firebase-blueprint.json`, and remove Firebase dependencies from `build.gradle.kts`.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│   AuthScreen ──(Once Authenticated)──> MainAppScaffold (Dashboard)     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                             AppViewModel                               │
│  - currentUser: StateFlow<UserEntity?>                                 │
│  - sessionLoginCount: StateFlow<Int>                                   │
│  - maxAllowedLogins: Int = 5                                           │
│  - checkSecuritySessionExpiry() -> triggers auto-logout                │
└───────────────────┬───────────────────────────────┬────────────────────┘
                    │                               │
        ┌───────────▼───────────┐       ┌───────────▼───────────┐
        │  SupabaseAuthService  │       │  SocialConnectRepo    │
        │  (Auth tokens,        │       │  (Supabase PostgREST  │
        │   credential check)   │       │   events & donations) │
        └───────────┬───────────┘       └───────────┬───────────┘
                    │                               │
┌───────────────────▼───────────────────────────────▼────────────────────┐
│                    Cloud Supabase Backend (PostgreSQL)                 │
│  Tables: events, user_profiles, event_registrations, posts, donations   │
└────────────────────────────────────────────────────────────────────────┘
```

### Relational Schema Definition (`supabase_schema.sql`)

- **`user_profiles`**: Maps users to names, roles (`VOLUNTEER` or `NGO_LEADER`), organization, phone, and total volunteer hours.
- **`events`**: Stores volunteer drives with category, date, time, location, volunteer capacities, and requirements.
- **`event_registrations`**: Relational join tracking changemaker registrations per event.
- **`donations`**: Logs money and material item pledges.
- **`posts`**: Community board updates, comments, and likes.

### Implementation Checklist

1. **Phase 1: Supabase SQL Schema Creation**:
   - Write `supabase_schema.sql` at root containing table definitions, RLS policies, and 12 seeded volunteer events.
2. **Phase 2: Supabase Data Service**:
   - Create `SupabaseDataService.kt` for PostgREST CRUD operations (`/rest/v1/events`).
3. **Phase 3: Auth Gate, Credential Persistence & Login Counter**:
   - Update `AuthRepository.kt` and `AppViewModel.kt` to track `sessionLoginCount`.
   - Implement `checkSecuritySession()` to enforce auto-logout when `sessionLoginCount >= 5`.
   - Ensure `MainActivity.kt` enforces `if (currentUser == null) AuthScreen() else MainAppScaffold()`.
4. **Phase 4: Complete Firestore & Firebase Clean-up**:
   - Remove `FirestoreVolunteerEventRepository.kt`, `FirestoreUserRepository.kt`, `FirebaseInitHelper.kt`, `firestore.rules`, and `firebase-blueprint.json`.
   - Clean up `app/build.gradle.kts` and `build.gradle.kts` to remove Firebase plugins and dependencies.
5. **Phase 5: Verification**:
   - Run `compile_applet` and verify successful compilation with zero warnings.
