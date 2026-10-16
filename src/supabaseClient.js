import { createClient } from '@supabase/supabase-js';

const SUPABASE_URL = "https://rrxsynkopobniyzqzveu.supabase.co";
const SUPABASE_PUBLIC_KEY = "sb_publishable_L0u894yZ3rzGzU3MAtlrqw_0NhGiUU_";

export const supabase = createClient(SUPABASE_URL, SUPABASE_PUBLIC_KEY);

export default supabase;
