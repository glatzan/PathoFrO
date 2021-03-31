/*
 * Array of id;;classToFocus
 */

// TODO remove
function updateAndAutoScrollToSelectedElement(containerId, child, offset = 0) {
    console.log(containerId)
    console.log(child)

    var child_elemnts = $(child)
    var containerElement = $(containerId)

    console.log(child_elemnts)
    console.log(containerElement)

    if (child_elemnts.length > 0 && containerElement.length > 0) {
        if(!isElementInViewPort(child_elemnts[0])){
            if (child_elemnts[0].offsetTop + offset > 0)
                containerElement[0].scrollTop = child_elemnts[0].offsetTop + offset;
        }
    }
}

function isElementInViewPort(element) {
    let rect = element.getBoundingClientRect();

    return (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && /* or $(window).height() */
        rect.right <= (window.innerWidth || document.documentElement.clientWidth) /* or $(window).width() */
    );
}
