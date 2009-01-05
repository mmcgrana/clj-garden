var liveRegexUpdate = function() {
  var patternAndStringField = $("#pattern, #string");
  var patternField = $("#pattern");
  var stringField =  $("#string");
  var resultField =  $("#result");
  
  var postURL = "/match";
  
  var responder = function(data) {
    if (data.status == "syntax-error") {
      resultField.val(data.message);
    } else if (data.status == "no-match") {
      resultField.val("nil");
    } else if (data.status == "match") {
      resultField.val(data.result);
    }
  };
  
  var blankVal = function(field) {
    return jQuery.trim(field.val()) == "";
  };
  
  var updater = function(keyupE) {
    if (!blankVal(patternField) && !blankVal(stringField)) {
      $.post(postURL, patternAndStringField.serialize(), responder, "json");
    } else {
      resultField.val("");
    }
  };
    
  stringField.bind("keyup", function(e)  { updater(e); });
  patternField.bind("keyup", function(e) { updater(e); });
}


$(document).ready(function() {
  liveRegexUpdate();
})