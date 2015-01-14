// If the children are visible then hide them, and vice-versa
function treeClickHandler(e, thisElement) {
    var children = $(thisElement).parent('li.parent_li').find(' > ul > li');
    if (children.is(':visible')) {
        children.hide('fast');
        $(thisElement).find(' > i').addClass('icon-plus-sign').removeClass('icon-minus-sign');
    } else {
        children.show('fast');
        $(thisElement).find(' > i').addClass('icon-minus-sign').removeClass('icon-plus-sign');
    }
    e.stopPropagation();
}

// Attach the handler for when users click a node in the tree.
// The handler will expand or collapse the node as appropriate
function attachClickHandlers() {
    $('.tree > ul').attr('role', 'tree').find('ul').attr('role', 'group');
    // Find element class tree, find all sub elements li that "has(ul)",
    // add class parent_li to those elements, looks for attribute role
    // 'treeitem' find direct children sub-elements 'span' add click
    // handler which is the function inside { ... }
    $('.tree').find('li:has(ul)').addClass('parent_li').attr('role', 'treeitem').find(' > span').on('click', function (e) {
        treeClickHandler(e, this);
    });
}



$(document).ready(function () {
    attachClickHandlers();
});