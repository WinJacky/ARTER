

//rootElement = document.getElementsByTagName("body");

/* IsDisPlayed.js */
isDisplayed = function(element) {
    if(window.getComputedStyle(element).visibility === "hidden") {
        return false;
    } else if(window.getComputedStyle(element).display === "none") {
        return false;
    } else {
        return true;
    }
}

/* ExcludeTags.js */
var excludeTages = ["script", "style", "meta", "html", "head", "link"];

/* GetHiddenElementsOfDOM.js */
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

/* GetElementXPath.js */
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

/* GetClickElementOfDOM.js */
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
                    if(hrefOfA==null || hrefOfA=="" || hrefOfA=="#" || hrefOfA.indexOf(window.location.href)!=-1 || hrefOfA.indexOf("javascript")) {
                        clickElements.push(getElementXPath(childElements[i]));
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
                    if(typeOfButton=="button" || typeOfButton==null || typeOfButton=="") {
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

/* GetTagsInformation.js */
var getTagsInformation = function (rootElement) {

    var rootTagInfo =  new TagsInformation(rootElement);
    rootTagInfo.setTagsInformation(getChildsTagsInformation(rootElement));

    var formattedStr = JSON.stringify(rootTagInfo);
    var path = "E:/learn/paper-tools/rtbwarte/output/test/TagsJSON.json";

    var fso;
    try {
        fso = new ActiveXObject("Scripting.FileSystemObject");
    } catch (e) {
        console.log(" Current browser is not supported. ");
        return;
    }

    var file = fso.createtextfile(path, true);
    file.write(formattedStr);
}

var getChildsTagsInformation = function (rootElement) {

    var childElements = root.children;
    var tagsInformation = new Array();

    for(var i=0; i<childElements.length; i++) {
        if(excludeTages.indexOf(childElements[i].tagName.toLowerCase()) == -1) {
            var tagInfo = new TagsInformation(childElements[i]);
            tagsInformation.put(tagInfo);
            tagInfo.setTagsInformation(getChildsTagsInformation(childElements[i]));
        }
    }

    return tagsInformation;
}

/* TagsInformation.js */
var TagsInformation = (function () {

    var xpath;
    var tagName;
    var id;
    var classAttribute;
    var nameAttribute;
    var textualcontent;
    var tagsInformation = new Array();

    function getElementXPath(element) {
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
    }

    function _tagsInformation(element) {

        var _this = this;

        if(_this instanceof _tagsInformation) {
            _this.xpath = getElementXPath(element);
            _this.tagName = element.nodeName.toLowerCase();
            _this.id = element.id;
            _this.classAttribute = element.className;
            _this.nameAttribute = element.getAttribute('name');
            _this.textualcontent = element.childNodes[0].nodeValue;
        } else {
            return new _tagsInformation(element);
        }
    }

    _tagsInformation.prototype = {
        constructor: _tagsInformation,
        setXpath: function (xpath) {
            this.xpath = xpath;
        },
        getXpath: function () {
            return this.xpath;
        },
        setTagName: function (tagName) {
            this.tagName = tagName;
        },
        getTagName: function () {
            return this.tagName;
        },
        setId: function (id) {
            this.id = id;
        },
        getId: function () {
            return this.id;
        },
        setClassAttribute: function (classAttribute) {
            this.classAttribute = classAttribute;
        },
        getClassAttribute: function () {
            return this.classAttribute;
        },
        setNameAttribute: function (nameAttribute) {
            this.nameAttribute = nameAttribute;
        },
        getNameAttribute: function () {
            return this.nameAttribute;
        },
        setTextualContent: function (textualcontent) {
            this.textualcontent = textualcontent;
        },
        getTextualContent: function () {
            return this.textualcontent;
        },
        setTagsInformation: function (tagsInformation) {
            this.tagsInformation = tagsInformation;
        },
        getTagsInformation: function () {
            return this.tagsInformation;
        },
        addTagsInformation: function (tagsInformation) {
            this.tagsInformation.push(tagsInformation);
        }
    }

    return _tagsInformation();
})();
