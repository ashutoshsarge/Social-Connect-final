import { supabase } from './supabaseClient.js';

/**
 * Signs in a user using Supabase Auth signInWithPassword.
 * - Only redirects to "/" when a real session exists.
 * - If data.session is null/missing, prevents redirect and surfaces message.
 * - On error, returns the error message and triggers the error callback.
 * 
 * @param {string} email
 * @param {string} password
 * @param {function} [onError] - Callback to display error under the form
 * @param {function} [onSuccess] - Optional custom success callback
 * @returns {Promise<{success: boolean, session: any, data?: any, error?: string}>}
 */
export async function signIn(email, password, onError, onSuccess) {
  try {
    const { data, error } = await supabase.auth.signInWithPassword({
      email: email.trim(),
      password,
    });

    if (error) {
      if (typeof onError === 'function') {
        onError(error.message);
      }
      return { success: false, session: null, error: error.message };
    }

    // Only redirect when a real session exists after login
    if (!data?.session) {
      const msg = 'No active session found. Please verify your email or credentials.';
      if (typeof onError === 'function') {
        onError(msg);
      }
      return { success: false, session: null, error: msg, data };
    }

    // Real session confirmed
    if (typeof onSuccess === 'function') {
      onSuccess(data);
    } else if (typeof window !== 'undefined') {
      // Redirect the user to the Home page ("/")
      window.location.href = '/';
    }

    return { success: true, session: data.session, data };
  } catch (err) {
    const errorMsg = err?.message || 'An unexpected error occurred during sign in.';
    if (typeof onError === 'function') {
      onError(errorMsg);
    }
    return { success: false, session: null, error: errorMsg };
  }
}

/**
 * Standard Sign In Form Component with pre-filled email support,
 * success banner from previous registration, and validation.
 */
export default function SignInComponent({ React, useState, initialEmail = '', initialNotice = null } = {}) {
  // Read possible URL params (e.g. /login?email=foo@example.com&registered=true)
  let urlEmail = initialEmail;
  let urlNotice = initialNotice;

  if (typeof window !== 'undefined' && window.location) {
    const params = new URLSearchParams(window.location.search);
    if (!urlEmail && params.get('email')) {
      urlEmail = params.get('email');
    }
    if (!urlNotice && params.get('registered') === 'true') {
      urlNotice = 'Account created successfully! Check your email to verify your account or sign in below.';
    }
  }

  const [email, setEmail] = useState ? useState(urlEmail) : [urlEmail, () => {}];
  const [password, setPassword] = useState ? useState('') : ['', () => {}];
  const [errorMessage, setErrorMessage] = useState ? useState(null) : [null, () => {}];
  const [successMessage, setSuccessMessage] = useState ? useState(urlNotice) : [urlNotice, () => {}];
  const [loading, setLoading] = useState ? useState(false) : [false, () => {}];

  const handleSubmit = async (e) => {
    e?.preventDefault?.();
    setErrorMessage(null);
    setLoading(true);

    const result = await signIn(email, password, (err) => {
      setErrorMessage(err);
    });

    setLoading(false);
    return result;
  };

  return {
    handleSubmit,
    email,
    setEmail,
    password,
    setPassword,
    errorMessage,
    setErrorMessage,
    successMessage,
    setSuccessMessage,
    loading,
  };
}
