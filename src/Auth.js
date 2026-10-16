import { supabase } from './supabaseClient.js';

/**
 * Protects private pages by checking the current Supabase session.
 * - Calls supabase.auth.getSession()
 * - If no session exists or an error occurs, redirects the user to /login (or specified login path).
 * - Returns the active session if authenticated.
 * 
 * @param {string} [loginPath='/login'] - Destination route if unauthenticated
 * @returns {Promise<any|null>} The active session, or null if redirected
 */
export async function protectRoute(loginPath = '/login') {
  if (typeof window === 'undefined') return null;

  try {
    const { data: { session }, error } = await supabase.auth.getSession();
    if (error || !session) {
      window.location.href = loginPath;
      return null;
    }
    return session;
  } catch (err) {
    window.location.href = loginPath;
    return null;
  }
}

/**
 * Alias for protectRoute.
 */
export const requireAuth = protectRoute;

/**
 * Handles user Sign In with Supabase Auth:
 * - Calls supabase.auth.signInWithPassword({ email, password })
 * - Only redirects to "/" when a real session exists
 * - If Supabase returns an error, shows a small error message under the form
 * 
 * @param {string} email
 * @param {string} password
 * @param {string} [errorElementId='signin-error-message']
 * @returns {Promise<{success: boolean, session: any, data?: any, error?: string}>}
 */
export async function handleSignIn(email, password, errorElementId = 'signin-error-message') {
  const errorContainer = typeof document !== 'undefined' ? document.getElementById(errorElementId) : null;
  if (errorContainer) {
    errorContainer.innerText = '';
    errorContainer.style.display = 'none';
  }

  try {
    const { data, error } = await supabase.auth.signInWithPassword({
      email: email.trim(),
      password,
    });

    if (error) {
      if (errorContainer) {
        errorContainer.innerText = error.message;
        errorContainer.style.display = 'block';
      }
      return { success: false, session: null, error: error.message };
    }

    // Strictly check email confirmation: do not allow unverified users to enter the dashboard
    const isEmailConfirmed = Boolean(data?.user?.email_confirmed_at || data?.user?.confirmed_at);
    if (!isEmailConfirmed) {
      try {
        await supabase.auth.signOut();
      } catch (_) {}
      const msg = 'Please verify your email address before entering the dashboard. Check your inbox for the verification link.';
      if (errorContainer) {
        errorContainer.innerText = msg;
        errorContainer.style.display = 'block';
      }
      return { success: false, session: null, error: msg, data };
    }

    // Only redirect when a real session exists after login
    if (!data?.session) {
      const msg = 'No active session found. Please verify your email or credentials.';
      if (errorContainer) {
        errorContainer.innerText = msg;
        errorContainer.style.display = 'block';
      }
      return { success: false, session: null, error: msg, data };
    }

    // Redirect the user to the Home page ("/")
    if (typeof window !== 'undefined') {
      window.location.href = '/';
    }

    return { success: true, session: data.session, data };
  } catch (err) {
    const msg = err?.message || 'Login failed. Please check your credentials.';
    if (errorContainer) {
      errorContainer.innerText = msg;
      errorContainer.style.display = 'block';
    }
    return { success: false, session: null, error: msg };
  }
}

/**
 * Handles user Sign Up with Supabase Auth:
 * - Includes displayName in user_metadata so user display names are saved in Supabase
 * - Calls supabase.auth.signUp({ email, password, options: { data: { display_name, full_name, name } } })
 * - UX Improvement:
 *    - Does NOT auto-login (signs out any session auto-created by Supabase)
 *    - Redirects the user to the Sign In page/view
 *    - Pre-fills the Sign In email field with the registered email
 *    - Shows a clear success message on the Sign In page
 * 
 * @param {string} email
 * @param {string} password
 * @param {string} [displayName='']
 * @param {string} [errorElementId='signup-error-message']
 * @param {string} [infoElementId='signup-info-message']
 * @param {string} [signInSuccessElementId='signin-success-message']
 * @param {string} [signInEmailElementId='signin-email']
 * @returns {Promise<{success: boolean, session: any, needsEmailConfirmation: boolean, message?: string, error?: string}>}
 */
export async function handleSignUp(
  email,
  password,
  displayName = '',
  errorElementId = 'signup-error-message',
  infoElementId = 'signup-info-message',
  signInSuccessElementId = 'signin-success-message',
  signInEmailElementId = 'signin-email'
) {
  const errorContainer = typeof document !== 'undefined' ? document.getElementById(errorElementId) : null;
  const infoContainer = typeof document !== 'undefined' ? document.getElementById(infoElementId) : null;

  if (errorContainer) {
    errorContainer.innerText = '';
    errorContainer.style.display = 'none';
  }
  if (infoContainer) {
    infoContainer.innerText = '';
    infoContainer.style.display = 'none';
  }

  try {
    const trimmedEmail = email.trim();
    const trimmedName = typeof displayName === 'string' ? displayName.trim() : '';
    const payload = {
      email: trimmedEmail,
      password,
    };

    // Pass display name in metadata to Supabase
    if (trimmedName.length > 0) {
      payload.options = {
        data: {
          display_name: trimmedName,
          full_name: trimmedName,
          name: trimmedName,
        },
      };
    }

    const { data, error } = await supabase.auth.signUp(payload);

    if (error) {
      if (errorContainer) {
        errorContainer.innerText = error.message;
        errorContainer.style.display = 'block';
      }
      return { success: false, session: null, needsEmailConfirmation: false, error: error.message };
    }

    // Explicitly do NOT auto-login
    try {
      await supabase.auth.signOut();
    } catch (_) {}

    const successNotice = 'Account created successfully! Check your email to verify your account or enter your password to sign in.';

    // If running in browser DOM with tabs:
    if (typeof document !== 'undefined') {
      const signInEmailInput = document.getElementById(signInEmailElementId);
      const signInSuccessNotice = document.getElementById(signInSuccessElementId);

      // Pre-fill the Sign In email field
      if (signInEmailInput) {
        signInEmailInput.value = trimmedEmail;
      }

      // Show success message on the Sign In page
      if (signInSuccessNotice) {
        signInSuccessNotice.innerText = successNotice;
        signInSuccessNotice.style.display = 'block';
      }

      // Switch to Sign In tab if tab function exists
      if (typeof window.switchTab === 'function') {
        window.switchTab('signin');
      }
    }

    return {
      success: true,
      session: null,
      needsEmailConfirmation: !data?.session,
      message: successNotice,
      data,
    };
  } catch (err) {
    const msg = err?.message || 'Registration failed. Please check your details.';
    if (errorContainer) {
      errorContainer.innerText = msg;
      errorContainer.style.display = 'block';
    }
    return { success: false, session: null, needsEmailConfirmation: false, error: msg };
  }
}

/**
 * Handles user Forgot Password recovery link dispatch with Supabase Auth:
 * - Calls supabase.auth.resetPasswordForEmail(email)
 * 
 * @param {string} email
 * @param {string} [statusElementId='reset-success-message']
 * @param {string} [errorElementId='reset-error-message']
 * @returns {Promise<{success: boolean, message?: string, error?: string}>}
 */
export async function handleForgotPassword(
  email,
  statusElementId = 'reset-success-message',
  errorElementId = 'reset-error-message'
) {
  const statusContainer = typeof document !== 'undefined' ? document.getElementById(statusElementId) : null;
  const errorContainer = typeof document !== 'undefined' ? document.getElementById(errorElementId) : null;

  if (statusContainer) {
    statusContainer.innerText = '';
    statusContainer.style.display = 'none';
  }
  if (errorContainer) {
    errorContainer.innerText = '';
    errorContainer.style.display = 'none';
  }

  try {
    const trimmedEmail = email ? email.trim() : '';
    if (!trimmedEmail) {
      const err = 'Please enter your email address.';
      if (errorContainer) {
        errorContainer.innerText = err;
        errorContainer.style.display = 'block';
      }
      return { success: false, error: err };
    }

    const { data, error } = await supabase.auth.resetPasswordForEmail(trimmedEmail);

    if (error) {
      if (errorContainer) {
        errorContainer.innerText = error.message;
        errorContainer.style.display = 'block';
      }
      return { success: false, error: error.message };
    }

    const msg = 'Password recovery instructions have been dispatched to your email! Please check your inbox.';
    if (statusContainer) {
      statusContainer.innerText = msg;
      statusContainer.style.display = 'block';
    }
    return { success: true, message: msg, data };
  } catch (err) {
    const errorMsg = err?.message || 'Failed to dispatch recovery email.';
    if (errorContainer) {
      errorContainer.innerText = errorMsg;
      errorContainer.style.display = 'block';
    }
    return { success: false, error: errorMsg };
  }
}

/**
 * Handles Sign In / Sign Up with Google OAuth
 */
export async function signInWithGoogle() {
  if (typeof window === 'undefined') return;
  return await supabase.auth.signInWithOAuth({
    provider: 'google',
    options: {
      redirectTo: window.location.origin
    }
  });
}

export { supabase };
export default {
  supabase,
  handleSignIn,
  handleSignUp,
  handleForgotPassword,
  signInWithGoogle,
  protectRoute,
  requireAuth,
};
