/**
 * Remove all the children of a given node.
 * Most useful for dynamic tables where you clearChildNodes() on the tbody element.
 *
 * @param id The id of the element
 */
function clearChildNodes(id) {
    var ele = $(id);

    if (ele == null) {
        alert("clearChildNodes() can't find an element with id: " + id + ".");
        throw id;
    }

    while (ele.childNodes.length > 0) {
        ele.removeChild(ele.firstChild);
    }
}

function useCustomLoadingMessage() {
    DWREngine.setPreHook(function() {
        var disabledZone = $('disabledZone');
        if (!disabledZone) {
            disabledZone = document.createElement('div');
            disabledZone.setAttribute('id', 'disabledZone');
            disabledZone.style.position = "absolute";
            disabledZone.style.zIndex = "1000";
            disabledZone.style.left = "0px";
            disabledZone.style.top = "0px";
            disabledZone.style.width = "100%";
            disabledZone.style.height = "100%";
            disabledZone.style.textAlign = "center";
            document.body.appendChild(disabledZone);
            var messageZone = document.createElement('div');
            messageZone.setAttribute('id', 'messageZone');
            messageZone.style.border = "1px solid black";
            messageZone.style.margin = "250px";
            messageZone.style.marginLeft = "350px";
            messageZone.style.marginRight = "350px";
            messageZone.style.background = "#dcedad";
            messageZone.style.color = "white";
            messageZone.style.fontFamily = "Arial,Helvetica,sans-serif";
            messageZone.style.padding = "4px";
            messageZone.innerHTML = "<img src='/images/progress_animated.gif'/>";
            disabledZone.appendChild(messageZone);
        } else {
            $('messageZone').innerHTML = "<img src='/images/progress_animated.gif'/>";
            disabledZone.style.visibility = 'visible';
        }
    });

    DWREngine.setPostHook(function() {
        $('disabledZone').style.visibility = 'hidden';
    });
}