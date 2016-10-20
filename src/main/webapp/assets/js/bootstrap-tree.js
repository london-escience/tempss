(function($, window, document) {

	// Attach the handler for when users click a node in the tree.
	// The handler will expand or collapse the node as appropriate
//	function attachClickHandlers() {
//	    $('.tree > ul').attr('role', 'tree').find('ul').attr('role', 'group');
//	    // Find element class tree, find all sub elements li that "has(ul)",
//	    // add class parent_li to those elements, looks for attribute role
//	    // 'treeitem' find direct children sub-elements 'span' add click
//	    // handler which is the function inside { ... }
//	    $('.tree').find('li:has(ul)').addClass('parent_li').attr('role', 'treeitem').find(' > span').on('click', function (e) {
//	        treeClickHandler(e, this);
//	    });
//	}

	// Attach the handler for when users click a node in the tree.
	// The handler will expand or collapse the node as appropriate
	window.attachClickHandlers = function() {
		var _tree = $('ul[role="tree"]');
		_tree.on('click', 'li.parent_li > span.badge', function(e) {
			treeExpandClickHandler(e, this);
		});
	}
	
	/**
	 * Expand or collapse branch. The element passed in is a span element
	 * belonging to a li.
	 */
	window.treeExpandClickHandler = function(event, element) {
		// Only expand branches which are not disabled
		if ($(element).closest('ul').data('disabled') !== true) {
			var children = $(element).siblings('ul').children('li');
			if (children.is(':visible')) {
				children.hide('fast');
				// $(element).find(' > i').addClass('icon-plus-sign').removeClass('icon-minus-sign');
			} else {
				children.show('fast');
				//$(element).find(' > i').addClass('icon-minus-sign').removeClass('icon-plus-sign');
			}
		}
		event.stopPropagation();
	}
	
	// document.ready
	$(function() {
		console.log('bootstrap-tree initialised...');
    });
	
//	return {
//		attachClickHandlers: attachClickHandlers,
//		treeExpandClickHandler: treeExpandClickHandler
//	}
	
} (window.jQuery, window, document));
