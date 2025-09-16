let auth0Client = null;

/**
 * Configures and initializes the Auth0 client.
 */
export async function configureClient() {
    auth0Client = await auth0.createAuth0Client({
        domain: 'dev-ic7b0akl6h55q8wb.us.auth0.com', // <-- Replace with your Auth0 Domain
        clientId: 'o7rNf1sgd3uDLZ6arqAW5TYP8R2grb3K', // <-- Replace with your Auth0 Client ID
        authorizationParams: {
            redirect_uri: window.location.origin,
            audience: 'https://trading-platform-api'
        }
    });
}

/**
 * Handles the login process by redirecting the user to the Auth0 login page.
 */
export async function login() {
    await auth0Client.loginWithRedirect();
}

/**
 * Handles the logout process.
 */
export async function logout() {
    auth0Client.logout({
        logoutParams: {
            returnTo: window.location.origin
        }
    });
}

/**
 * Checks if the user is authenticated.
 * @returns {Promise<boolean>}
 */
export async function isAuthenticated() {
    return await auth0Client.isAuthenticated();
}

/**
 * Gets the access token for making secure API calls.
 * @returns {Promise<string>} The JWT access token.
 */
export async function getAccessToken() {
    return await auth0Client.getTokenSilently();
}

/**
 * This function should be called when the page loads to handle the
 * redirect back from Auth0 after login.
 */
export async function handleRedirectCallback() {
    const query = window.location.search;
    if (query.includes("code=") && query.includes("state=")) {
        await auth0Client.handleRedirectCallback();
        window.history.replaceState({}, document.title, "/"); // Clean the URL
    }
}