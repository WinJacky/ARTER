var getClickElementsOfDOM = function (element) {

    var childElements = element.children;
    var clickElements = new Array();
    for(var i=0; i<childElements.length; i++) {
        if(excludeTages.indexOf(childElements[i].tagName.toLowerCase()) != -1) {
            continue;
        }
        if(isDisplayed(childElements[i])) {
            switch (childElements[i].tagName.toLowerCase()) {
                case "a":
                    var hrefOfA = childElements[i].getAttribute("href");
                    if(hrefOfA==null || hrefOfA=="" || hrefOfA=="#" || hrefOfA.indexOf(window.location.href)!=-1 || hrefOfA.indexOf("javascript")!=-1) {
                        if(childElements[i].text.indexOf("Log Out") == -1) {
                            clickElements.push(getElementXPath(childElements[i]));
                        }
                    }
                    break;

                case "input":
                    var typeOfInput = childElements[i].getAttribute("type");
                    if(typeOfInput=="button") {
                        clickElements.push(getElementXPath(childElements[i]));
                    }
                    break;

                case "button":
                    var typeOfButton = childElements[i].getAttribute("type");
                    if(typeOfButton=="button" || typeOfButton==null || typeOfButton=="" || childElements[i].text.toLowerCase().indexOf("submit") == -1) {
                        clickElements.push(getElementXPath(childElements[i]));
                    }
                    break;

                default:
                    break;
            }
            clickElements = clickElements.concat(getClickElementsOfDOM(childElements[i]));
        }
    }

    return clickElements;
};