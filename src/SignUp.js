import { supabase } from './supabaseClient.js';

/**
 * Signs up a new user using Supabase Auth signUp.
 * 
 * UX Behavior:
 * 1) Passes displayName in options.data so the user's name is saved in Supabase user_metadata.
 * 2) After successful signUp:
 *    - Does NOT auto-login (signs out active session if any was auto-created).
 *    - Redirects the user to the Sign In page (e.g. /login?email=...&registered=true).
 *    - Calls onConfirmationNotice / onSuccess with pre-fill details so the Sign In page
 *      shows a clear success message and has the email field pre-filled.
 * 
 * @param {string} email
 * @param {string} password
 * @param {string} [displayName] - Full name / display name of user
 * @param {function} [onError] - Callback to display error under the form
 * @param {function} [onConfirmationNotice] - Callback to display confirmation message
 * @param {function} [onSuccess] - Callback when sign up succeeds with { email, data }
 * @returns {Promise<{success: boolean, session: any, needsEmailConfirmation: boolean, message?: string, error?: string}>}
 */
export async function signUp(email, password, displayName = '', onError, onConfirmationNotice, onSuccess) {
  try {
    const trimmedEmail = email.trim();
    const trimmedName = typeof displayName === 'string' ? displayName.trim() : '';
    const signUpPayload = {
      email: trimmedEmail,
      password,
    };

    if (trimmedName.length > 0) {
      signUpPayload.options = {
        data: {
          display_name: trimmedName,
          full_name: trimmedName,
          name: trimmedName,
        },
      };
    }

    const { data, error } = await supabase.auth.signUp(signUpPayload);

    if (error) {
      if (typeof onError === 'function') {
        onError(error.message);
      }
      return { success: false, session: null, needsEmailConfirmation: false, error: error.message };
    }

    // Explicitly do NOT auto-login: sign out any session created by signUp
    try {
      await supabase.auth.signOut();
    } catch (_) {}

    const successMessage = "Account created successfully! Check your email to verify your account or sign in below.";

    if (typeof onConfirmationNotice === 'function') {
      onConfirmationNotice(successMessage);
    }

    // Notify caller with registered email for pre-filling the Sign In form
    if (typeof onSuccess === 'function') {
      onSuccess({ email: trimmedEmail, message: successMessage, data });
    } else if (typeof window !== 'undefined') {
      // Redirect to the Sign In page with email pre-fill and registered flag
      window.location.href = `/login?email=${encodeURIComponent(trimmedEmail)}&registered=true`;
    }

    return {
      success: true,
      session: null,
      needsEmailConfirmation: !data?.session,
      message: successMessage,
      data,
    };
  } catch (err) {
    const errorMsg = err?.message || 'An unexpected error occurred during sign up.';
    if (typeof onError === 'function') {
      onError(errorMsg);
    }
    return { success: false, session: null, needsEmailConfirmation: false, error: errorMsg };
  }
}

/**
 * Standard Sign Up Form Component with built-in state, submission,
 * redirects to Sign In with pre-filled email, and no auto-login.
 */
export default function SignUpComponent({ React, useState, onNavigateToSignIn } = {}) {
  const [name, setName] = useState ? useState('') : ['', () => {}];
  const [email, setEmail] = useState ? useState('') : ['', () => {}];
  const [password, setPassword] = useState ? useState('') : ['', () => {}];
  const [errorMessage, setErrorMessage] = useState ? useState(null) : [null, () => {}];
  const [infoMessage, setInfoMessage] = useState ? useState(null) : [null, () => {}];
  const [loading, setLoading] = useState ? useState(false) : [false, () => {}];

  const handleSubmit = async (e) => {
    e?.preventDefault?.();
    setErrorMessage(null);
    setInfoMessage(null);
    setLoading(true);

    const result = await signUp(
      email,
      password,
      name,
      (err) => setErrorMessage(err),
      (notice) => setInfoMessage(notice),
      ({ email: regEmail, message }) => {
        if (typeof onNavigateToSignIn === 'function') {
          onNavigateToSignIn({ email: regEmail, message });
        } else if (typeof window !== 'undefined') {
          window.location.href = `/login?email=${encodeURIComponent(regEmail)}&registered=true`;
        }
      }
    );

    setLoading(false);
    return result;
  };

  return {
    handleSubmit,
    name,
    setName,
    email,
    setEmail,
    password,
    setPassword,
    errorMessage,
    setErrorMessage,
    infoMessage,
    setInfoMessage,
    loading,
  };
}
