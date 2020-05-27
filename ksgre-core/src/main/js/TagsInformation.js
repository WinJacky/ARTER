function TagsInformation(element) {
    this.xpath = this.getElementXPath(element);
    this.tagName = element.nodeName.toLowerCase();
    this.id = element.id;
    this.classAttribute = element.className;
    this.nameAttribute = element.getAttribute('name');
    this.textualContent = this.getInnerTextOfElement(element);
    this.tagsInformation = new Array();
    this.valueAttribute = element.getAttribute('value');
}

TagsInformation.prototype = {
    getElementXPath(element) {
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
    },
    getInnerTextOfElement(element) {
        if(element.childNodes.length > 0) {
            try {
                return element.childNodes[0].nodeValue.trim();
            } catch (e) {
                return "";
            }

        }
    },
    addTagsInformation(tagsInformation) {
        this.tagsInformation.push(tagsInformation);
    },
};