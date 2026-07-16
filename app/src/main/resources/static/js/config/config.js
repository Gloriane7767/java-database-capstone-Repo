// config.js
/**
 * Configuration file for defining global constants and environment-specific settings.
 *
 * API_BASE_URL:
 * - Base URL for all API requests made from the frontend.
 * - Uses the browser's current origin so it works correctly whether the app
 *   is accessed via localhost or through a proxied cloud URL, since the
 *   Spring Boot server serves both the frontend and the API from the same
 *   host/port.
 *
 * Example usage:
 *   fetch(`${API_BASE_URL}/api/appointments`)
 */
export const API_BASE_URL = window.location.origin;