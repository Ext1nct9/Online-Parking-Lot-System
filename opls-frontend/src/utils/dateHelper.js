
/** Convert a date to a string for the API request. */
export const toDateString = function(date = new Date()) {
  return date.toISOString().replace("T", " ").replace("Z", "");
}

/** Get a new local date. */
export const getDate = function() {
  var date = new Date();
  var utcTime = date.getTime();
  var localOffset = date.getTimezoneOffset() * 60 * 1000;
  return new Date(utcTime - localOffset);
}
