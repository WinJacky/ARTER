var getTagsInformation = function (rootElement) {

    console.log('start...');


    var rootTagInfo =  new TagsInformation(rootElement);

    rootTagInfo.tagsInformation = getChildsTagsInformation(rootElement);

    var formattedStr = JSON.stringify(rootTagInfo);

    console.log(formattedStr);

    return formattedStr;

};

var getChildsTagsInformation = function (rootElement) {

    var childElements = rootElement.children;
    var tagsInformation = new Array();

    for(var i=0; i<childElements.length; i++) {
        if(excludeTages.indexOf(childElements[i].tagName.toLowerCase()) == -1) {
            var tagInfo = new TagsInformation(childElements[i]);
            tagsInformation.push(tagInfo);
            tagInfo.tagsInformation = getChildsTagsInformation(childElements[i]);
        }
    }

    return tagsInformation;
};