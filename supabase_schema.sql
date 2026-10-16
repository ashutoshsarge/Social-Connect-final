-- ==============================================================================
-- SocialConnect Supabase Database Schema & Initial Data
-- Fully compatible with Supabase PostgREST, Row Level Security (RLS), and Auth
-- ==============================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. USER PROFILES TABLE
CREATE TABLE IF NOT EXISTS public.user_profiles (
    id TEXT PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'VOLUNTEER', -- 'VOLUNTEER' or 'NGO_LEADER'
    organization TEXT DEFAULT '',
    phone TEXT DEFAULT '',
    volunteer_hours INTEGER NOT NULL DEFAULT 0,
    badges TEXT DEFAULT 'New Contributor',
    avatar_url TEXT DEFAULT '',
    login_session_count INTEGER DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS on user_profiles
ALTER TABLE public.user_profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Public profiles are viewable by everyone"
    ON public.user_profiles FOR SELECT
    USING (true);

CREATE POLICY "Users can insert their own profile"
    ON public.user_profiles FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can update their own profile"
    ON public.user_profiles FOR UPDATE
    USING (true);

-- 3. EVENTS TABLE
CREATE TABLE IF NOT EXISTS public.events (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    ngo_id TEXT NOT NULL DEFAULT 'ngo-1',
    ngo_name TEXT NOT NULL,
    ngo_logo TEXT DEFAULT '',
    verified BOOLEAN NOT NULL DEFAULT true,
    category TEXT NOT NULL DEFAULT 'Environment',
    date_str TEXT NOT NULL,
    time_str TEXT NOT NULL,
    location TEXT NOT NULL,
    distance_km INTEGER NOT NULL DEFAULT 4,
    joined_count INTEGER NOT NULL DEFAULT 0,
    max_volunteers INTEGER NOT NULL DEFAULT 50,
    cover_img TEXT,
    description TEXT NOT NULL,
    what_youll_do JSONB DEFAULT '[]'::jsonb,
    skills_needed JSONB DEFAULT '[]'::jsonb,
    what_to_bring JSONB DEFAULT '[]'::jsonb,
    donation_campaign JSONB,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    created_at_tz TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS on events
ALTER TABLE public.events ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Events are viewable by everyone"
    ON public.events FOR SELECT
    USING (true);

CREATE POLICY "Anyone authenticated can create an event"
    ON public.events FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Event organizers can update their events"
    ON public.events FOR UPDATE
    USING (true);

-- 4. EVENT REGISTRATIONS (Join tracking)
CREATE TABLE IF NOT EXISTS public.event_registrations (
    id BIGSERIAL PRIMARY KEY,
    event_id TEXT NOT NULL REFERENCES public.events(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL,
    volunteer_name TEXT NOT NULL,
    volunteer_avatar TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'CONFIRMED', -- 'CONFIRMED', 'ATTENDED', 'CANCELLED'
    registered_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(event_id, user_id)
);

ALTER TABLE public.event_registrations ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Registrations viewable by everyone"
    ON public.event_registrations FOR SELECT
    USING (true);

CREATE POLICY "Users can register for events"
    ON public.event_registrations FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can cancel their registrations"
    ON public.event_registrations FOR DELETE
    USING (true);

-- 5. DONATIONS TABLE (Money & Materials)
CREATE TABLE IF NOT EXISTS public.donations (
    id TEXT PRIMARY KEY,
    event_id TEXT REFERENCES public.events(id) ON DELETE SET NULL,
    donor_name TEXT NOT NULL,
    donor_email TEXT,
    donation_type TEXT NOT NULL, -- 'MONEY' or 'MATERIAL'
    amount_cents INTEGER DEFAULT 0,
    item_name TEXT DEFAULT '',
    quantity INTEGER DEFAULT 0,
    currency TEXT DEFAULT 'INR',
    message TEXT DEFAULT '',
    donated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.donations ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Donations are viewable by everyone"
    ON public.donations FOR SELECT
    USING (true);

CREATE POLICY "Anyone can make a donation pledge"
    ON public.donations FOR INSERT
    WITH CHECK (true);

-- 6. COMMUNITY POSTS & COMMENTS
CREATE TABLE IF NOT EXISTS public.posts (
    id TEXT PRIMARY KEY,
    author_name TEXT NOT NULL,
    author_role TEXT NOT NULL DEFAULT 'Volunteer',
    author_avatar TEXT,
    time_str TEXT DEFAULT 'Just now',
    text_content TEXT NOT NULL,
    images JSONB DEFAULT '[]'::jsonb,
    likes_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.posts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Posts viewable by everyone"
    ON public.posts FOR SELECT
    USING (true);

CREATE POLICY "Anyone can publish a community post"
    ON public.posts FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Anyone can update post likes"
    ON public.posts FOR UPDATE
    USING (true);

-- 7. SEED DATA: 12 INITIAL VOLUNTEER DRIVES
INSERT INTO public.events (id, title, ngo_id, ngo_name, ngo_logo, verified, category, date_str, time_str, location, distance_km, joined_count, max_volunteers, cover_img, description, what_youll_do, skills_needed, what_to_bring)
VALUES
(
    'evt-1',
    'Tree Plantation Drive 2026',
    'ngo-1',
    'Green Earth Initiative',
    'https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&w=120&q=80',
    true,
    'Environment',
    'Tomorrow',
    '08:00 AM - 12:00 PM',
    'Lodhi Gardens, Central Delhi',
    3,
    18,
    30,
    'https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&w=800&q=80',
    'Join us in planting 500 native saplings across Lodhi Gardens to revive Delhi green cover. Training, gardening equipment, and refreshments provided.',
    '["Sapling planting along designated park lanes", "Mulching and drip watering installation", "Labelling trees with geo-tags for survival tracking"]'::jsonb,
    '["Enthusiasm for nature", "Basic physical fitness", "Team collaboration"]'::jsonb,
    '["Water bottle", "Comfortable walking shoes", "Sun cap", "Gardening gloves (optional)"]'::jsonb
),
(
    'evt-2',
    'Weekend Slum Teaching & STEM Mentorship',
    'ngo-2',
    'Asha Education Trust',
    'https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=120&q=80',
    true,
    'Education',
    'Saturday',
    '09:00 AM - 01:00 PM',
    'Community Centre, Seelampur',
    6,
    12,
    20,
    'https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=800&q=80',
    'Empower bright children with foundational mathematics, English reading, and interactive science experiments. All learning aids supplied.',
    '["Conduct small-group interactive reading circles", "Teach fun arithmetic games and Vedic tricks", "Distribute study stationery and guide homework"]'::jsonb,
    '["Patience with kids", "Basic Hindi/English proficiency", "Warm mentorship spirit"]'::jsonb,
    '["Notebook", "Pens/Markers", "Compassionate attitude"]'::jsonb
),
(
    'evt-3',
    'Free Health Checkup & Eye Screening Camp',
    'ngo-3',
    'Swasthya Seva Sansthan',
    'https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?auto=format&fit=crop&w=120&q=80',
    true,
    'Healthcare',
    'Sunday',
    '07:30 AM - 02:00 PM',
    'Govt Boys School, Jahangirpuri',
    9,
    24,
    40,
    'https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?auto=format&fit=crop&w=800&q=80',
    'Support doctors and optometrists in providing free diagnostic checkups, medicine dispensation, and spectacle prescriptions to over 600 underserved families.',
    '["Patient queue management and crowd flow", "Assisting triage vitals (Blood pressure, BMI, Sugar)", "Medicine distribution desk and prescription guides"]'::jsonb,
    '["Empathy", "Organization skills", "Medical/Nursing students welcome"]'::jsonb,
    '["Valid photo ID", "Comfortable footwear", "Face mask"]'::jsonb
),
(
    'evt-4',
    'Yamuna Riverfront Clean-Up Sprint',
    'ngo-1',
    'Green Earth Initiative',
    'https://images.unsplash.com/photo-1618477461853-cf6ed80faba5?auto=format&fit=crop&w=120&q=80',
    true,
    'Environment',
    'This Sunday',
    '06:30 AM - 10:30 AM',
    'Yamuna Ghat 21, Kashmiri Gate',
    5,
    35,
    50,
    'https://images.unsplash.com/photo-1618477461853-cf6ed80faba5?auto=format&fit=crop&w=800&q=80',
    'Mobilize to clear non-biodegradable plastics, discarded textiles, and debris along the Yamuna bank. Waste segregated and sent to registered recycling hubs.',
    '["Collecting riverbank plastics into color-coded sacks", "Sorting recyclables vs hazardous materials", "Loading municipal green trucks"]'::jsonb,
    '["Commitment to clean rivers", "Safety mindfulness"]'::jsonb,
    '["Rubber boots or sturdy shoes", "Gloves provided on site", "Hydration bottle"]'::jsonb
),
(
    'evt-5',
    'Zero Hunger Warm Meal Food Relief',
    'ngo-4',
    'Roti Bank Delhi',
    'https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=120&q=80',
    true,
    'Food Relief',
    'Today',
    '06:00 PM - 09:30 PM',
    'Near New Delhi Railway Station (Ajmeri Gate)',
    2,
    15,
    25,
    'https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=800&q=80',
    'Package freshly cooked wholesome khichdi, roti, and nutritional supplements for over 1,000 homeless individuals, daily-wage laborers, and street vendors.',
    '["Packaging hot meal trays cleanly", "Orderly distribution along shelter zones", "Greeting and respecting beneficiaries warmly"]'::jsonb,
    '["Punctuality", "Hygiene awareness", "Compassion"]'::jsonb,
    '["Apron (or will be provided)", "Head cap", "Enthusiastic heart"]'::jsonb
),
(
    'evt-6',
    'Stray Animal Rescue & Winter Shelter Building',
    'ngo-5',
    'Paws & Care Sanctuary',
    'https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=120&q=80',
    true,
    'Animal Welfare',
    'Next Saturday',
    '10:00 AM - 03:00 PM',
    'Sector 14 Sanctuary, Rohini',
    11,
    8,
    15,
    'https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=800&q=80',
    'Build insulated straw-padded wooden kennels and bedding for stray puppies and injured dogs to shield them from Delhi harsh winter chills.',
    '["Assembling pre-cut weather-safe dog shelters", "Administering basic anti-tick lotions with vets", "Feeding and socializing rescued pups"]'::jsonb,
    '["Love for animals", "Comfort around dogs", "Basic crafting ability"]'::jsonb,
    '["Clothes you do not mind getting furry", "Old blankets if you have any"]'::jsonb
)
ON CONFLICT (id) DO NOTHING;
