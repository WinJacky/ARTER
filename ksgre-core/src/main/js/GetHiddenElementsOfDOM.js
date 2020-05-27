var getHiddenElementsOfDOM = function(element) {
    var hiddenElements = new Array();
    var childElements = element.children;
    for(var i=0; i<childElements.length; i++) {
        if(excludeTages.indexOf(childElements[i].tagName.toLowerCase()) != -1) {
            continue;
        }
        if(!isDisplayed(childElements[i])) {
            hiddenElements.push(getElementXPath(childElements[i]));
        } else {
            hiddenElements = hiddenElements.concat(getHiddenElementsOfDOM(childElements[i]));
        }
    }
    return hiddenElements;
};