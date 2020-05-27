var isDisplayed = function(element) {
    if(window.getComputedStyle(element).visibility === "hidden") {
        return false;
    } else if(window.getComputedStyle(element).display === "none") {
        return false;
    } else {
        return true;
    }
};