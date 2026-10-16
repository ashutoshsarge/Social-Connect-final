# SocialConnect: Connect. Volunteer. Make an Impact.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin%20100%25-blue.svg)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-purple.svg)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-Supabase%20%7C%20PostgreSQL-3ECF8E.svg)](https://supabase.com/)
[![Database](https://img.shields.io/badge/Local%20DB-Room%20(SQLite)-orange.svg)](https://developer.android.com/training/data-storage/room)
[![AI Grounding](https://img.shields.io/badge/AI-Gemini%20Search%20Grounding-4285F4.svg)](https://ai.google.dev/)
[![Compliance](https://img.shields.io/badge/Privacy-DPDP%20Act%202023%20Compliant-teal.svg)](https://www.meity.gov.in/)

---

## 🌟 1. Overview & Vision

**SocialConnect** is an impact-driven mobile platform built to connect passionate volunteers with verified Non-Governmental Organizations (NGOs). Whether planting trees to revive urban green cover, tutoring underprivileged children in community shelters, distributing warm meals to daily-wage workers, or building winter dog shelters, SocialConnect empowers everyday citizens to transform empathy into measurable grassroots action.

### 🎯 Intentions, Motive & Purpose

1. **Eliminating the Volunteering Discovery Gap**: Thousands of citizens want to dedicate their weekends to noble causes but lack a single, reliable hub to find credible initiatives happening nearby. SocialConnect makes finding weekend drives as simple as browsing a social feed.
2. **Empowering NGOs with On-Demand Manpower**: Non-profits and grassroots organizations often struggle with fluctuating volunteer turnouts and chaotic registration tracking. SocialConnect provides NGOs with digital roster management, live attendee counts, and material pledge coordination.
3. **Accountability & Verifiable Impact**: Volunteers need tangible recognition for their efforts. SocialConnect tracks validated volunteer hours, awards achievement badges, and issues downloadable digital service certificates.
4. **Dual-Track Giving (Goods & Funds)**: Non-profits do not just need money—they frequently require urgent physical items like textbooks, meal boxes, gardening gloves, or pet blankets. SocialConnect pairs monetary donations (with 80G tax receipts) with itemized material pledge campaigns.
5. **Privacy First (DPDP Act 2023)**: Designed with strict compliance to India's *Digital Personal Data Protection Act, 2023*, giving users complete transparency, full personal data export rights, and permanent data erasure mechanisms.

---

## 🛠️ 2. Tech Stack

| Layer | Technologies & Libraries |
| :--- | :--- |
| **Language & Runtime** | **Kotlin 100%**, Coroutines (`Dispatchers.IO`, `Dispatchers.Main`), Kotlin Flow & `StateFlow` |
| **Target Platform** | **Android 15+** (`compileSdk 36`, `targetSdk 36`, `minSdk 24`) |
| **UI Framework** | **Jetpack Compose**, **Material 3 (M3)** design system, Material Icons Extended, edge-to-edge window insets |
| **Architecture** | **MVVM (Model-View-ViewModel)** with Clean Architecture principles |
| **Cloud Backend** | **Supabase** (PostgreSQL Relational DB, PostgREST direct REST endpoints, Row-Level Security policies, Supabase Auth) |
| **Local Storage & Cache** | **Android Room Database** (SQLite with KSP codegen), Encrypted **SharedPreferences** (`UserSessionManager`) |
| **Networking & HTTP** | **OkHttpClient**, **Retrofit**, Moshi JSON converter, Logging Interceptors |
| **Image Loading** | **Coil Compose** with crossfade animations, local category drawable fallbacks, and memory cache |
| **AI & Search Grounding** | **Google Gemini API** (`gemini-3.5-flash`) for real-time Google Search Grounding of causes and NGOs |
| **Session Security** | Rotating login session counter, cryptographic 6-digit email OTP dispatch, and automated 5-login security logout |
| **Testing** | **Robolectric** (Local JVM CUJ testing), **JUnit 4**, AndroidX Test, **Roborazzi** screenshot tests |

---

## 🚀 3. Core Features

### 🔐 1. Mandatory Auth Gate & 5-Login Session Security
- **Strict Initial Gate**: First-time visitors and logged-out users are strictly gated at the Auth screen. Access to community drives, rosters, and certificates requires authentication.
- **Credential Persistence**: Once signed in or registered, credentials and session parameters are safely saved in local storage, providing smooth reentry across daily launches without retyping passwords.
- **Automated Security Auto-Logout (5 Logins)**: To protect accounts on shared or family mobile devices, an active login counter tracks consecutive authenticated sessions. Upon the 5th login/launch cycle, the app automatically invalidates the session and returns to the Auth screen with an informative amber banner:
  > *"For your account security, your session has expired after 5 logins. Please enter your credentials to continue."*
- **Live Session Status Card**: Displayed in the Profile screen showing current session progress (e.g., *"Login 2 of 5 active"*), a visual progress bar, and a one-tap **Renew Session** button.
- **Multiple Sign-In Pathways**:
  - Direct Supabase Email & Password.
  - Passwordless Email OTP with real-time 6-digit code dispatch and rate-limited cooldown.
  - One-Tap Demo Quick-Logins for Volunteer (*Rahul Nile*) and NGO Leader (*Dr. Aarav Patel*).

### 🔍 2. Real-Time Drive Discovery & Interactive Map Modals
- **Category Filter Tabs**: Environment, Education, Food Relief, Healthcare, Animal Welfare, Community Development, Women Empowerment, and Elderly Care.
- **Smart Time Filters**: Quick filters for *Today*, *Tomorrow*, and *Weekend* drives.
- **Interactive Event Detail**: Complete briefing including *"What You'll Do"*, *"Skills Needed"*, *"What to Bring"*, host NGO badge, venue coordinates, and one-tap RSVP.
- **Directions & Navigation Modal**: Embedded venue directions, metro station milestones, and external navigation intents.

### 👥 3. Dual-Role Experience (Volunteer & NGO Leader)
- **Volunteer Persona**:
  - One-tap drive RSVP with instant roster registration.
  - Bookmarking drives for later review.
  - Dynamic volunteer hours tracking (+hours button with celebratory toast).
  - Downloadable digitally verifiable Volunteer Service Certificates.
- **NGO Leader Persona**:
  - Dedicated **NGO Dashboard** with drives held, volunteer mobilization metrics, and impact hours.
  - **Create Drive Modal**: Publish new community drives directly to the live Supabase cloud database with custom title, category, date, venue, capacity, and cover images.
  - **Volunteer Attendance Roster**: Check-in attendees, toggle attendance status (*Registered* vs. *Attended*), and verify volunteer service hours.

### 🎁 4. Dual-Track Donation Engine
- **Itemized Material Pledging**: NGOs list urgently required items (e.g., *100 tree saplings*, *50 school bags*, *200 warm meals*). Volunteers can pledge specific quantities with live progress bars.
- **Tax-Deductible Money Donations**: Direct financial contributions with Section 80G tax exemption receipt confirmation.

### 💬 5. Community Changemaker Wall
- Public community feed where changemakers share field photos, milestone recaps, and thoughts.
- Interactive comments and community upvotes/likes.

### 🛡️ 6. DPDP Act 2023 Compliance & Data Protection
- **Section 11 (Right to Access Summary)**: Generates a complete, structured JSON/text export of user data processed by the SocialConnect Foundation.
- **Section 12 (Right to Erasure)**: Irreversibly purges personal profile, credentials, and local records upon user request.
- **Consent Banner**: Granular consent controls for functional storage, analytics, and essential authentication tokens.

---

## 🌍 4. How SocialConnect Helps the Community

```
┌────────────────────────────────────────────────────────────────────────┐
│                        SOCIALCONNECT ECOSYSTEM                         │
├───────────────────────────────────┬────────────────────────────────────┤
│           FOR VOLUNTEERS          │              FOR NGOs              │
├───────────────────────────────────┼────────────────────────────────────┤
│ • Zero-friction drive discovery   │ • Immediate access to manpower     │
│ • Verified & safe organizations   │ • Automated attendance rosters     │
│ • Official service certificates   │ • Material item pledge lists       │
│ • Track personal impact hours     │ • Zero-overhead cloud publishing   │
│ • Network with fellow changemakers│ • Direct donor communication       │
└───────────────────────────────────┴────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         SOCIETAL IMPACT                                │
│  🌱 10,000+ Trees Planted   🍲 50,000+ Meals Served   📚 1,200+ Mentored│
└────────────────────────────────────────────────────────────────────────┘
```

1. **For Volunteers (Students, Professionals & Retirees)**:
   - Eliminates uncertainty around safety and legitimacy by only listing verified NGOs.
   - Converts weekend downtime into positive community contributions.
   - Provides official certification helpful for academic applications and corporate CSR hours.

2. **For Non-Governmental Organizations (NGOs)**:
   - Cuts marketing and volunteer acquisition costs to zero.
   - Solves material supply bottlenecks through transparent pledge wishlists.
   - Saves hours of manual paperwork with automated digital roster check-ins.

3. **For Local Communities & Environment**:
   - Accelerates municipal tree cover and green corridor restoration.
   - Diverts riverbank plastics and trash to registered recycling facilities.
   - Ensures nutritious hot meals reach daily-wage earners and homeless shelters nightly.

---

## 🗄️ 5. Supabase Database Schema

The database architecture is defined in `supabase_schema.sql` and powered by Supabase PostgreSQL with Row Level Security (RLS) enabled across all public tables:

- **`events`**: Stores drive title, category, date, time, venue, distances, max capacity, requirements, and cover image URLs.
- **`user_profiles`**: Stores user identity, role (`VOLUNTEER` / `NGO_LEADER`), organization, contact, verified badges, and volunteer hours.
- **`event_registrations`**: Relational join tracking user RSVPs per volunteer drive.
- **`donations`**: Logs both monetary and material item pledges.
- **`posts` & `comments`**: Powers the community changemaker social board.

---

## 📱 6. Getting Started & Building the App

### Prerequisites
- Android Studio Ladybug / Meerkat or AI Studio Build Environment
- JDK 17 or higher
- Android SDK 36 (minSdk 24)

### Building & Running
```bash
# Verify app compilation
gradle compileDebugSources

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Assemble Debug APK
gradle :app:assembleDebug
```

---

## 📄 7. License & Credits

Developed with ❤️ for changemakers and non-profits everywhere.  
Compliant with India's **Digital Personal Data Protection Act (DPDP Act 2023)**.  
All icons and components conform to Google Material Design 3 guidelines.
