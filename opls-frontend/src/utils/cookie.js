
/** Get a cookie. */
export const getCookie = function(name) {
  var match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
  return !!match ? match[2] : undefined;
};

/** Delete a cookie. */
export const deleteCookie = function(name) {
  document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=None; Secure`;
}

/** Set a cookie. */
export const setCookie = function(name, value, expMs) {
  let cookieStr = name + "=" + value;
  if (!!expMs) {
    let expiry = new Date(expMs);
    cookieStr += `; expires=${expiry.toUTCString()}`;
  }
  cookieStr += "; path=/; SameSite=None; Secure";
  document.cookie = cookieStr;
};
