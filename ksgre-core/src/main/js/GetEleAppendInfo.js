var formElements = ["input", "button", "checkbox", "textarea", "label"];

var getEleAppendInfo = function (element) {

    var parentTagName;
    var sameSiblingNum = 0;
    var locator;

    if (formElements.indexOf(element.tagName.toLowerCase()) != -1) {
        var parElement = element.parentElement;
        while (parElement.tagName.toLowerCase() != "form" && parElement.tagName.toLowerCase() != "body") {
            parElement = parElement.parentElement;
        }
        if (parElement.tagName.toLowerCase() == "body") {
            parentTagName = "";
            locator = "";
            var directPar = element.parentElement;
            for (var i=0; i<directPar.childElementCount; i++) {
                if (directPar.children[i].tagName.toLowerCase() == element.tagName.toLowerCase()) {
                    sameSiblingNum++;
                }
            }
            return [parentTagName, sameSiblingNum.toString(), locator];
        } else {
            parentTagName = parElement.tagName.toLowerCase();
            var directPar = element.parentElement;
            for (var i=0; i<directPar.childElementCount; i++) {
                if (directPar.children[i].tagName.toLowerCase() == element.tagName.toLowerCase()) {
                    sameSiblingNum++;
                }
            }
            locator = getElementXPath(parElement);
            return [parentTagName, sameSiblingNum.toString(), locator];
        }
    }

    if (element.tagName.toLowerCase() == "li") {
        var parElement = element.parentElement;
        while (parElement.tagName.toLowerCase() != "ul" && parElement.tagName.toLowerCase() != "ol" && parElement.tagName.toLowerCase() != "body") {
            parElement = parElement.parentElement;
        }
        if (parElement.tagName.toLowerCase() == "body") {
            parentTagName = "";
            locator = "";
            var directPar = element.parentElement;
            for (var i=0; i<directPar.childElementCount; i++) {
                if (directPar.children[i].tagName.toLowerCase() == element.tagName.toLowerCase()) {
                    sameSiblingNum++;
                }
            }
            return [parentTagName, sameSiblingNum.toString(), locator];
        } else {
            parentTagName = parElement.tagName.toLowerCase();
            var directPar = element.parentElement;
            for (var i=0; i<directPar.childElementCount; i++) {
                if (directPar.children[i].tagName.toLowerCase() == element.tagName.toLowerCase()) {
                    sameSiblingNum++;
                }
            }
            locator = getElementXPath(parElement);
            return [parentTagName, sameSiblingNum.toString(), locator];
        }
    }

    var parElement = element.parentElement;
    parentTagName = parElement.tagName.toLowerCase();
    locator = getElementXPath(parElement);
    var directPar = element.parentElement;
    for (var i=0; i<directPar.childElementCount; i++) {
        if (directPar.children[i].tagName.toLowerCase() == element.tagName.toLowerCase()) {
            sameSiblingNum++;
        }
    }
    return [parentTagName, sameSiblingNum.toString(), locator];

};

var getElementXPath = function (element) {
    var paths = [];
    for(; element.nodeType == 1; element = element.parentNode) {
        var index = 0;
        for(var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling) {
            if(sibling.nodeType == Node.DOCUMENT_TYPE_NODE) {
                continue;
            }
            if(sibling.nodeName == element.nodeName) {
                ++index;
            }
        }
        var tagName = element.nodeName.toLowerCase();
        var pathIndex = ("[" + (index + 1) + "]");
        paths.splice(0, 0, tagName + pathIndex);
    }
    return paths.length ? "/" + paths.join("/") : null;
};