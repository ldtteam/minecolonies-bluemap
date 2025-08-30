function stopEvent(e) {
    e.stopPropagation();
}

function updateMinecoloniesMarkers(e) {
    const elements = document.getElementsByClassName("minecolonies-marker");
    for (const element of elements) {
        element.addEventListener("click", stopEvent);
    }
}

(function loop() {
    updateMinecoloniesMarkers();
    requestAnimationFrame(loop);
})();
