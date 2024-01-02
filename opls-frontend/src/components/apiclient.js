import axios from "axios";
import base64url from "base64url";

import router from "../router";
import { routes } from "../router/routes";

var cookie = require("../utils/cookie");
var config = require("../../config");

var frontendUrl = "http://" + config.dev.host + ":" + config.dev.port;
var backendUrl = "http://" + config.dev.backendHost + ":" + config.dev.backendPort;

var defaultHeaders = {
  "Access-Control-Allow-Origin": frontendUrl,
  "Content-Type": "application/json",
  "Accept": "application/json"
};

var AXIOS = axios.create({
  baseURL: backendUrl,
  headers: defaultHeaders,
  responseType: "json",
  responseEncoding: "utf8"
});

const cookiesConfig = {
  username: {
    name: "opls-username",
  },
  accessToken: {
    name: "opls-a-token",
  },
  refreshToken: {
    name: "opls-r-token",
  },
  tokenExpiry: {
    name: "opls-a-expiry",
  },
};

const apiRequest = function(method, endpoint, headers, body) {
  // fill in default headers
  headers = { ...defaultHeaders, ...headers };
  let options = {
    headers: headers
  }

  switch (method) {
    case "GET": return AXIOS.get(endpoint, options);
    case "POST": return AXIOS.post(endpoint, body, options);
    case "PUT": return AXIOS.put(endpoint, body, options);
    case "PATCH": return AXIOS.patch(endpoint, body, options);
    case "DELETE": return AXIOS.delete(endpoint, options);
  }
};

const redirectToLogin = function(error) {
  router.push({
    path: routes.LOGIN(),
    query: {
      "reason": error
    }
  });
}

/**
 * Login a user to the application.
 * @param {string} username The username.
 * @param {string} password The password.
 * @returns [response status, response data].
 */
export const login = async function(username, password) {
  return new Promise((resolve, reject) => {
      authenticate("password", {
          "username": username,
          "password": password
        }, false)
        .then(([status, data]) => {
          cookie.setCookie(cookiesConfig.username.name, username, data["expires_on"]);
          resolve([status, data]);
        })
        .catch((error) => {
          reject(error);
        });
    });
}

/**
 * Authenticate the current session.
 * @param {"client_credentials" | "password" | "refresh_token"} grantType The grant type to request.
 * @param {object} body The request form.
 * @param {boolean} doRedirect Whether to redirect to login on failure.
 * @returns [response status, response data].
 */
const authenticate = async function(grantType, body, doRedirect = true) {
  console.log("authenticate", grantType);
  // construct request body
  let reqBody = {
    "grant_type": grantType
  };
  switch (grantType) {
    case "password":
      reqBody["username"] = body.username;
      reqBody["password"] = body.password;
      break;
    case "refresh_token":
      reqBody["refresh_token"] = body.refreshToken;
      break;
  };

  // make API request with client authorization
  let clientCredentials = config.dev.oauthClientId + ":" + config.dev.oauthClientSecret;
  return new Promise((resolve, reject) => {
    apiRequest("POST", "/token", {
      "Authorization": "basic " + base64url(clientCredentials),
      "Content-Type": "application/x-www-form-urlencoded"
    }, reqBody)
    .then((response) => {
      if (response) {
        let data = response.data;
        // set cookies
        cookie.setCookie(cookiesConfig.accessToken.name, data["access_token"], data["expires_on"]);
        if (!!data["refresh_token"]) {
          cookie.setCookie(cookiesConfig.refreshToken.name, data["refresh_token"]);
        }
        cookie.setCookie(cookiesConfig.tokenExpiry.name, data["expires_on"]);

        resolve([response.status, data]);
      }
      else {
        if (doRedirect) {
          redirectToLogin("No response found.");
        }
        else {
          reject("No response found.")
        }
      }
    })
    .catch((error) => {
      let response = error.response
      ? error.response.data.error_description
      : "Unauthorized";
      if (doRedirect) {
        redirectToLogin(response);
      }
      else {
        reject(response);
      }
    })
  });
};

const needRefreshToken = function() {
  var expiry = cookie.getCookie(cookiesConfig.tokenExpiry.name);
  var expiryMs = Number.parseInt(expiry);
  return !expiryMs || (expiryMs <= new Date().getTime() - 60 * 1000);
};

/** Setup authorization store. */
export const detectAuthStore = async function() {
  var username = cookie.getCookie(cookiesConfig.username.name);
  return new Promise((resolve) => {
    if (username) {
      // fetch account
      authenticatedRequest("GET", "/account")
        .then((data) => {
          resolve({
            loggedIn: true,
            userClaims: data.claims,
          });
        })
        .catch((err) => {
          resolve(null);
          logout();
        });
    }
    else {
      resolve(null);
    }
  })
};

/**
 * Logout the user by clearing cookies.
 */
export const logout = function() {
  cookie.deleteCookie(cookiesConfig.accessToken.name);
  cookie.deleteCookie(cookiesConfig.refreshToken.name);
  cookie.deleteCookie(cookiesConfig.tokenExpiry.name);
  cookie.deleteCookie(cookiesConfig.username.name);
}

const getAccessToken = async function() {
  // determine if has access token
  var accessToken = cookie.getCookie(cookiesConfig.accessToken.name);
  if (!accessToken) {
    // get client credentials grant
    let [_, data] = await authenticate("client_credentials");
    return data["access_token"];
  }

  // get expiry
  if (needRefreshToken()) {
    var rToken = cookie.getCookie(cookiesConfig.refreshToken.name);
    if (!!rToken) {
      // refresh the session
      let [_, data] = await authenticate("refresh_token", {
        refreshToken: rToken
      });
      return data["access_token"];
    }
    else {
      // client credentials
      let [_, data] = await authenticate("client_credentials");
      return data["access_token"];
    }
  }

  return accessToken;
};

/**
 * Make an authenticated request to the backend API.
 * @param {"GET" | "POST" | "PUT" | "PATCH" | "DELETE"} method The HTTP method.
 * @param {string} endpoint The endpoint.
 * @param {object} body The request body.
 * @returns The response data.
 */
export const authenticatedRequest = async function(method, endpoint, body) {
  const accessToken = await getAccessToken();

  return new Promise((resolve, reject) => {
    apiRequest(method, endpoint, {
      "Authorization": "bearer " + accessToken,
      }, body)
      .then((response) => {
        resolve(response.data);
      })
      .catch((error) => {
        if (error.response.status === 401) {
          redirectToLogin(error.response
            ? error.response.data.error_description
            : "Unauthorized");
        } else {
          reject(error.response);
        }
      });
  });
};
